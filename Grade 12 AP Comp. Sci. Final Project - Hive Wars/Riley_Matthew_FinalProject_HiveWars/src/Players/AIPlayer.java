/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package Players;

import java.util.LinkedList;
import java.util.Random;

import GameEntities.Hive;
import GameEntities.Larva;
import GameEntities.Pod;
import Positioning.Pt;
import Utility.Settings;
import Utility.Timer;

public class AIPlayer extends Player{
	
	/*
	 * AI TODO:
	 * 
	 * After testing the grid in further detail, use the grid for detecting nearby entities.
	 * 
	 * AI Issues:
	 * Disable auto-aggression for the AI (make an "aggresive" boolean and set it to false for all AI larva)
	 * Reason that mag is so jerky is that larva will consider itself "atDestination" unless mag is greater than
	 * 10 units away --> change this for enemy AI larva?
	 * 
	 * For later: should probably cap the number of hives, too. 
	 * 
	 * Rethink the grav pt. method, in the following ways
	 * A larva shouldn't be stuck between hives it is attracted to
	 * - Use the magnitude values to choose the what hive to focus on, and only create a grav for that hive
	 * - Incorporate distance? 
	 * -- for enemy hives -> no (what if they hide a hive in the corner of the field?)
	 * -- for my hives -> no (what if there is a far away hive that needs support?)
	 * 
	 * Instead of using the fraction of health for hives, use the # of enemies within a certain radius of it
	 * - expensive though : must calc. distance for each larva for each hive (don't do this...)
	 * - by the time it is being damaged, it's probably too late
	 * - this is like a radar approach
	 * 
	 * Larva-to-larva is untested, but it accounts for distance (so not as much of an issue as the hive)
	 */
	
	// The minimum distance between hives (and walls)
	private static final int BUILD_TOL = Larva.INNER_INFLUENCE_R + 30; 
	
	// The radius within which a larva is considered to be supported another larva
	private static final int SUPPORT_R = 100;
	
	// The radius within which an enemy larva is considered a threat and is assigned a grav vector
	private static final int MIN_E_DIST = 500;
	
	private Timer spawnTimer;
	private int numHivesIntended;
	
	public AIPlayer(String name) {
		super(name);
		
		numHivesIntended = 0;
	}
	
	@Override
	public void update() {
		
		// On the first run:
		if(spawnTimer == null) {
			
			numHivesIntended = allHives().size();
			
			// Init the timer
			spawnTimer = new Timer();
		}
		
		// Update all of the AI's entities and pods
		updateAllEntities();
		updateAllPods();
		
		if(allLarvae().size() > 0) {
			
			// Get the centre of all of the larvae
			// This is the centre of the AI's network, so to speak.
			Pt larvaeCentre = getLarvaeCentre(allLarvae());
			
			// Manage the hive waypoints
			setWayPointsTo(larvaeCentre.x(), larvaeCentre.y());
			
			// Manage hive creation
			int numHivesToSpawn = getNumHivesToSpawn();
			for(int i = 0; i < numHivesToSpawn; i++) {
				
				if(getAvailableLarvae().size() > Pod.SIZE) {
					
					Pt nextHivePos = null;
					int maxDistFromCentre = 100;
					while(nextHivePos == null) {
						
						// Get a random hive position near the larvaeCentre
						Pt randPos = getRandPosAboutPt(larvaeCentre, maxDistFromCentre);
						
						// If the position is valid, choose it as the position of the next hive
						if(isValidHivePos(randPos)) {
							nextHivePos = randPos;
						}
						else {
							
							// Become more liberal in the hive placement if a position doesn't work
							maxDistFromCentre += 20;
						}
					}
					
					// Create a hive at the position obtained
					createHiveAt(nextHivePos); 
				}
			}
			
			// Manage larvae 
			for(Larva lar : getAvailableLarvae()) {
						
				setGravDest(lar, larvaeCentre);
			}
		}
	}
	
	/*
	 * Method 1 methods
	 */
	
	private void setGravDest(Larva lar, Pt larvaeCentre) {
		
		Pt grav = new Pt(0, 0);
		
		/*
		 * Generate a grav vector for each hive (friendly and foe)
		 * 
		 * Grav is proportional to maxhealth / currenthealth
		 * - More attracted to low-health hives
		 * -- Defend my ones, and attack the enemy ones
		 * 
		 * Grav is inversely proportional to the number of hives left
		 * - Rush to defend my hive if there aren't many left
		 * - Rush to kill the enemy hive if there aren't many left
		 */
		
		// For each of my hives:
		for(Hive h : allHives()) {
			
			float fudge = 20.0f;
			float mag = 1;
			
			mag *= fudge;
			mag *= (float) Math.pow(Hive.MAX_HEALTH / h.health(), 2);
			mag /= (allHives().size() + allPods().size());
			
			Pt vector = new Pt(lar.pos(), h.pos());
			vector.setMagnitude(mag);
			
			grav.add(vector);
		}
		
		// For each of the enemy hives
		for(Hive h : allEnemyHives()) {
			
			float fudge = 20.0f;
			float mag = 1;
			
			mag *= fudge;
			mag *= (float) Hive.MAX_HEALTH / h.health();
			mag /= allEnemyHives().size();
					
			Pt vector = new Pt(lar.pos(), h.pos());
			vector.setMagnitude(mag);
			
			grav.add(vector);		
		}
		
		/*
		 * Generate a grav vector for each enemy larva 
		 * 
		 * Attraction to enemy larva is directly proportional to (my estimated support) - (enemy estimated support)
		 * - "support" is the number of friendly larva nearby
		 * 
		 * Repulsion to enemy larva is inversely proportional to enemy's distance
		 * - Simply, larva experience more attraction to closer enemies
		 * 
		 * Use squared distances to avoid the expensive sqrt() operation
		 */
		
		int mySupport = getSupport(lar, allLarvae(), SUPPORT_R);
		LinkedList<Larva> allEnemyLarvae = allEnemyLarvae();
		
		for(Larva eLar : allEnemyLarvae) {
			
			// Only consider enemy larva within a certain range
			if(Pt.isDistLessThan(lar.pos(), eLar.pos(), MIN_E_DIST)) {
				
				/*
				 * Advantage:
				 * if > 0: the AI thinks it has the advantage (will be attracted to a unit of the same support)
				 * if < 0: the AI thinks it has the disadvantage (will avoid a unit of the same support)
				 */
				int advantage = 0;
				int enemySupport = getSupport(eLar, allEnemyLarvae, SUPPORT_R);
				int supportDiff = mySupport - enemySupport + advantage;
				//System.out.println("Support diff: " + supportDiff);
				
				float fudge = 500000.0f;
				float mag = 1;
				
				mag *= fudge;
				mag *= Math.pow(supportDiff, 1);
				mag /= eLar.pos().distSqToPt(lar.pos());
				//System.out.println("Mag: " + mag);
				
				Pt vector = new Pt(lar.pos(), eLar.pos());
				vector.setMagnitude(mag);
				
				grav.add(vector);	
			}
			
			
		}
		
		// Move the larva in the position of the gravity vector
		Pt dest = new Pt(lar.pos());
		dest.add(grav);
		lar.setDest(dest);
	}
	
	private int getSupport(Larva unit, LinkedList<Larva> army, int radius) {
		
		int support = 0;
		
		for(Larva lar : army) {
			
			if(lar != unit && Pt.isDistLessThan(unit.pos(), lar.pos(), radius)) {
				
				support++;
			}
		}
		
		return support;
	}
	
	private int getNumHivesToSpawn() {
		
		int numToSpawn = 0;
		
		/*
		 * Create a new hive to replace each one that was destroyed.
		 */
		
		numToSpawn += numHivesIntended - (allHives().size() + allPods().size());
		
		/*
		 * Determine if it is time to spawn a new hive, based on the following expression
		 * 
		 * It was determined by differentiating the formula for population 
		 * twice with respect to time, and then solving for the local maxima of 
		 * the change in larva population.
		 */
		
		boolean timeToSpawn = spawnTimer.secsElapsed() > Pod.SIZE * Hive.SPAWN_FREQ;
		if(timeToSpawn) {
			
			if(allLarvae().size() > Pod.SIZE) {
				
				numHivesIntended++;
				
				numToSpawn++;
				spawnTimer.restart();
			}
		}
		
		/*
		 * Evaluate if any of my hives are at risk of death.
		 * 
		 * If it is at risk, it will likely die, so spawn a new hive.
		 * Be more conservative with the minimum allowed health if only 
		 * a few hives remain.
		 */
		
		float minAcceptableHealthFraction = 0.95f / (allHives().size() + allPods().size());
		for(Hive h : allHives()) {
			
			float fractionOfMaxHealth = (float) h.health() / Hive.MAX_HEALTH;
			
			if(fractionOfMaxHealth < minAcceptableHealthFraction) {
				
				//System.out.println("Min: " + minAcceptableHealthFraction + " , Fraction: " + fractionOfMaxHealth);
				numToSpawn++;
			}
		}
		
		return numToSpawn;
	}
	
	// TODO: make this private later
	public Pt getLarvaeCentre(LinkedList<Larva> list) {
		
		Pt larvaeCentre = new Pt(0, 0);
		
		if(list.size() > 0) {
			
			// Set the waypoints to the "centre" of all the larvae
			larvaeCentre = new Pt(0, 0);
			for(Larva l : list) {
				larvaeCentre.add(l.pos());
			}
			larvaeCentre.multiply(1.0f / list.size());
		}
		else {
			
			larvaeCentre = null;
		}
		
		
		return larvaeCentre;
	}
	
	/*
	 * AI Helper Methods
	 */
	
	/**
	 * 
	 * @param pos
	 * @param maxDistFromPt
	 * @return a random pt that is within the given maximum distance from the given pt
	 */
	public Pt getRandPosAboutPt(Pt pos, int maxDistFromPt) {
		
		Random rand = new Random();
		
		float randAngle = rand.nextFloat() * 2 * (float) Math.PI;
		float randRadius = rand.nextFloat() * maxDistFromPt;
		
		float randX = pos.x() + (float) Math.cos(randAngle) * randRadius;
		float randY = pos.y() + (float) Math.sin(randAngle) * randRadius;
		
		return new Pt(randX, randY);
	}
	
	/**
	 * 
	 * @param 
	 * checkPos: the position to be checked (aka the intended position of the new hive)
	 * 
	 * @return 
	 * True if checkPos does not overlap with a current hive or pod destination, and is within the field. 
	 * \nFalse otherwise. 
	 */
	private boolean isValidHivePos(Pt checkPos) {
		
		// Check for overlap with existing hives
		for(Hive h : allHives()) {
			
			if(h.pos().distSqToPt(checkPos) < Math.pow(Hive.R * 2 + BUILD_TOL, 2)) {
				
				return false;
			}
		}
		
		// Check for overlap with pods (aka future hives)
		for(Pod p : allPods()) {
			
			if(p.dest().distSqToPt(checkPos) < Math.pow(Hive.R * 2 + BUILD_TOL, 2)) {
				
				return false;
			}
		}
		
		// Check that the hive will be fully contains within the field
		int wallOffset = Hive.R + BUILD_TOL;
		if(! checkPos.isContainedIn(wallOffset, wallOffset, Settings.FIELD_WIDTH - wallOffset, Settings.FIELD_HEIGHT - wallOffset)) {
			return false;
		}
		
		return true;
	}
	
	private LinkedList<Larva> getAvailableLarvae() {
		
		LinkedList<Larva> available = new LinkedList<Larva>();
		
		synchronized (allLarvae()) {
			
			for(Larva lar : allLarvae()) {
				
				if(isAvailable(lar)) {
					available.add(lar);
				}
			}	
		}
		
		return available;
	}
	
	private boolean isAvailable(Larva lar) {
		
		for(Pod p : allPods()) {
			
			if(p.larvaePod().contains(lar)) {
				return false;
			}
		}
		return true;
	}
	
	private void createHiveAt(Pt pos) {
		
		spawnPod(pos, getAvailableLarvae());
	}
	
	private void setWayPointsTo(float x, float y) {
		
		for(Hive h : allHives()) {
			h.setSpawnDest(x, y);
		}
	}
}
