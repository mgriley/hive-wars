/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package GameEntities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import Players.Player;
import Positioning.Grid;
import Positioning.Pt;
import Utility.Debugging;
import Utility.Settings;
import Utility.Timer;


public class Larva extends GameEntity {

	// Computer-generated images
	private static BufferedImage selectedMarkerImg;
	private static BufferedImage overCreepMarkerImg;
	private static BufferedImage myImg;
	private static BufferedImage enemyImg;
	private static BufferedImage allyImg;
	
	// For collision avoidance / steering
	public static final int INNER_INFLUENCE_R = 10;
	private static final int OUTER_INFLUENCE_R = 30;
	
	// The Larvae gets bonus speed and dps over creep
	private boolean overCreep = false;
	private final int REG_SPEED = 300;
	private final int BONUS_SPEED = REG_SPEED + 15;
	private final int REG_DPS = 30;
	private final int BONUS_DPS = REG_DPS + 20;
	
	private final int MAX_SPEED = BONUS_SPEED + 15; // a general speed-cap for the larvae
	
	// For movement
	public Pt movementDest;
	private Timer moveSpeedTimer = new Timer();
	private int movementSpeed = REG_SPEED; // speed in units / sec
	private boolean enRoute = false; // false if at/near destination, true otherwise
	private static final int MOVE_MIN_DIST = 10;
	
	// For Health
	static final int MAX_HEALTH = 100;
	
	// For combat
	private int dps = REG_DPS; // damage / sec
	private Timer dpsTimer = new Timer();
	private static final int ATTACK_RANGE = 15;
	
	// For physics
	private ArrayList<Pt> forces = new ArrayList<>();
	private ArrayList<Pt> allRepulsions = new ArrayList<>();
	private Pt movement = new Pt();
	private Pt aggression = new Pt();
	
	// Relevant lists
	private List<Larva> nearbyLarvae;
	private List<Hive> nearbyHives;
	private List<GameEntity> nearbyFoes;
	
	// For repulsion
	private static final float MIN_SCALED_INTERSECT = 0f; 
	
	// For aggression
	private static final int AGGRESSION_TOLERANCE = 0;
	private static final int AGGRESSION_RANGE = 200;
	
	public Larva(float x, float y, Player myPlayer) {
		
		super(myPlayer, x, y, INNER_INFLUENCE_R, MAX_HEALTH);
		
		movementDest = new Pt(x, y);
		
		moveSpeedTimer.restart();
		
		// Init the relevant lists
		nearbyLarvae = new LinkedList<>();
		nearbyHives = new LinkedList<>();
		nearbyFoes = new LinkedList<>();
	}
	
	/*
	 * General Updates
	 */
	
	@Override
	public void update() {
		
		// Resaturate the lists of all of the lists of neighbours
		nearbyLarvae.clear();
		nearbyHives.clear();
		nearbyFoes.clear();
		Grid.getInstance().saturateNeighbourLists(this, nearbyLarvae, nearbyHives, nearbyFoes);
		
		// Apply creep bonuses, if applicable
		overCreep = isOverMyCreep();
		if(!overCreep) {
			movementSpeed = REG_SPEED;
			dps = REG_DPS;
		}
		else {
			movementSpeed = BONUS_SPEED;
			dps = BONUS_DPS;
		}
		
		// Update the physics forces
		updateMovement();
		updateSolidRepulsion();
		updateAggression();
		
		reconcileForces();
		
		// Use the physics forces to update the Larvae's position
		updatePosition();
		
		// Constrain position (aka collision detection with walls and hives)
		constrainPosition();
		
		// Reset the speed timer
		moveSpeedTimer.restart();
	}
	
	private void updatePosition() {
		
		// Compute the resultant vector of all of the applicable forces
		Pt resultant = new Pt(0, 0);
		
		for(Pt f : forces) {
			resultant.add(f);
		}
		
		// If the movement is significant:
		if(resultant.getMagnitude() > 0) {
			
			// Ensure that the larvae cannot exceed maximum speed
			if(resultant.getMagnitude() > MAX_SPEED) {
				resultant.normalize();
				resultant.multiply(MAX_SPEED);
			}
			
			// Compute the new position
			Pt newPos = new Pt(pos().x() + resultant.x(), pos().y() + resultant.y());
			
			// Update the larva's grid location
			Grid g = Grid.getInstance();
			g.updateTile(this, newPos);
			
			// Move the larva to the new position
			pos().set(newPos);
		}
	}
	
	private void constrainPosition() {
		
		// Constrain the larvae's position to within the four field walls
		int r = INNER_INFLUENCE_R;
		pos().constrain(r, r, Settings.FIELD_WIDTH - r, Settings.FIELD_HEIGHT - r);
		
		// Prevent the larvae from contacting any hives
		for(Hive h : nearbyHives) {
			
			Pt hivePos = h.pos();
			
			// Move the larvae to a position just outside the hive, if intersecting
			boolean intersecting = pos().distSqToPt(hivePos) < Math.pow(INNER_INFLUENCE_R + Hive.R, 2);
			if(intersecting) {
				
				Pt hiveToLarvae = new Pt(pos().x() - hivePos.x(), pos().y() - hivePos.y());
				hiveToLarvae.normalize();
				hiveToLarvae.multiply(INNER_INFLUENCE_R + Hive.R);
				
				hiveToLarvae.add(hivePos);
				pos().set(hiveToLarvae);
			}
		}
	}
	
	/*
	 * Physics Updates
	 */
	
	private void reconcileForces() {
		
		forces.clear();
		
		// Only consider the movement vector if en-route
		if(enRoute) {
			forces.add(movement);
			
		}
		else {
			
			forces.add(aggression);
		}	
		forces.addAll(allRepulsions);
	}
	
	private void updateMovement() {
		
		if(enRoute) {
			
			Pt deltaPos = new Pt(pos(), movementDest);
			
			float mag = deltaPos.getMagnitude();
			
			if(mag > MOVE_MIN_DIST) {
				
				// Normalize and apply speed to the direction vector
				float multFactor = Math.min(mag, movementSpeed * moveSpeedTimer.secsElapsed());
				deltaPos.normalize();
				deltaPos.multiply(multFactor);
				movement.set(deltaPos);
			}
			else {
			
				// If close to the destination, cease movement
				movement.set(0, 0);
				
				// No longer en route, destination reached
				enRoute = false;
			}
		}
		else {
			movement.set(0, 0);
		}
	}
	
	private void updateSolidRepulsion() {
		
		allRepulsions.clear();
		
		// Compute the repulsions with nearby larvae
		for(Larva lar : nearbyLarvae) {
			
			float scaledIntersectDist = this.getScaledDist(lar.pos());
			
			// If there is intersection:
			if(scaledIntersectDist > MIN_SCALED_INTERSECT) {
				
				// Get the normal force from the offending larvae to this larvae
				Pt deltaPos = new Pt(pos().x() - lar.pos().x(), pos().y() - lar.pos().y());
				deltaPos.normalize();
				int fudge = 15;
				deltaPos.multiply(fudge * scaledIntersectDist);
				
				// Provide a random repulsion for units that are stuck together
				if(deltaPos.getMagnitude() < fudge && scaledIntersectDist > 0.9f) {
					
					Random rand = new Random();
					float angle = rand.nextFloat() * 2 * (float) Math.PI;
					float x = fudge * (float) Math.cos(angle);
					float y = fudge * (float) Math.sin(angle);
					deltaPos = new Pt(x, y);
				}
				
				// Save the repulsive force
				allRepulsions.add(deltaPos);
			}	
		}
	}
	
	public void updateAggression() {
		
		aggression.set(0, 0);
		
		// Get the nearest enemy entity
		
		GameEntity nearestFoe = getNearest(pos(), nearbyFoes);
		if(nearestFoe != null) {
			
			float dist = nearestFoe.pos().distToPt(pos());
			boolean inAggressionRange = AGGRESSION_TOLERANCE < dist && dist < AGGRESSION_RANGE;
			
			if(inAggressionRange) {
				
				// Compute the target pt
				// The target is the pt outside the enemy (where we will go to attack the enemy)
				
				Pt foeToMe = new Pt(pos().x() - nearestFoe.pos().x(), pos().y() - nearestFoe.pos().y());
				foeToMe.normalize();
				foeToMe.multiply(INNER_INFLUENCE_R + nearestFoe.radius());
				Pt target = new Pt(nearestFoe.pos());
				target.add(foeToMe);
				
				// Compute the vector to the target pt
				
				Pt meToTarget = new Pt(target.x() - pos().x(), target.y() - pos().y());
				float mag = meToTarget.getMagnitude();
				meToTarget.normalize();
				float multFactor = Math.min(mag, movementSpeed * moveSpeedTimer.secsElapsed());
				meToTarget.multiply(multFactor);

				// Set the aggression vector
				aggression.set(meToTarget);	
				
				// If the foe is in attack range, attack the foe
				boolean inAttackRange = dist - radius() - nearestFoe.radius() < ATTACK_RANGE;
				if(inAttackRange) {
					
					if(dpsTimer.secsElapsed() > 1) {
						nearestFoe.damage(dps);
						dpsTimer.restart();
					}
				}
			}
		}
	}
	
	public boolean isOverMyCreep() {
		
		for(Hive h : nearbyHives) {
			
			// If the hive is an ally:
			if(Player.getRelation(this.player(), h.player()) == Player.ALLIES) {
				
				// If the larva is over the hive's mucus:
				if(h.pos().distSqToPt(pos()) < Math.pow(h.mucusRadius(), 2)) {
					return true;
				}	
			}
		}
		
		return false;
	}
	
	/*
	 * Intersection
	 */
	
	// Get the insersection between the outer areas of influence
	// Returns a float ( [0, 1] if intersecting ), where 0 is minimal intersection, and 1 is maximum intersection 
	private float getScaledDist(Pt p) {
		
		float rawDist =  this.pos().distToPt(p);
		float unscaledDist = OUTER_INFLUENCE_R - rawDist;
		float scaledDist = unscaledDist / OUTER_INFLUENCE_R;
		
		return scaledDist;
	}
	
	/*
	 * Movement
	 */
	
	public static void setColonyDest(int newX, int newY, LinkedList<Larva> colony) {
		
		// Move the selected larvae towards the specified destination
		for(Larva l : colony) {
			
			if(l.selected()) {
				l.setDest(new Pt(newX, newY));
			}
		}
	}
	
	/*
	 * On right click event
	 */
	
	@Override
	public void onRightClick(float x, float y) {
		
		setDest(new Pt(x, y));
	}
	
	/*
	 * Drawing
	 */
	
	public static void initImages() {
		
		// Init all images
		myImg = new BufferedImage(INNER_INFLUENCE_R * 2, INNER_INFLUENCE_R * 2, BufferedImage.TRANSLUCENT);
		allyImg = new BufferedImage(INNER_INFLUENCE_R * 2, INNER_INFLUENCE_R * 2, BufferedImage.TRANSLUCENT);
		enemyImg = new BufferedImage(INNER_INFLUENCE_R * 2, INNER_INFLUENCE_R * 2, BufferedImage.TRANSLUCENT);
		selectedMarkerImg = new BufferedImage(OUTER_INFLUENCE_R * 2, OUTER_INFLUENCE_R * 2, BufferedImage.TRANSLUCENT);
		overCreepMarkerImg = new BufferedImage(INNER_INFLUENCE_R * 2, INNER_INFLUENCE_R * 2, BufferedImage.TRANSLUCENT);
		
		// Generate all img graphics
		generateBodyImg(myImg, Color.GREEN);
		generateBodyImg(allyImg, Color.ORANGE);
		generateBodyImg(enemyImg, Color.RED);
		generateSelectedMarkerImg();
		generateOverCreepMarkerImg();
	}
	
	private static void generateBodyImg(BufferedImage img, Color color) {
		
		Graphics2D g2d = img.createGraphics();
		
		g2d.setColor(color);
		int r = INNER_INFLUENCE_R;
		g2d.fillOval(img.getWidth() / 2 - r, img.getHeight() / 2 - r, r * 2, r * 2);
		//g2d.fillRect(img.getWidth() / 2 - r, img.getHeight() / 2 - r, r * 2, r * 2);
	}
	
	private static void generateSelectedMarkerImg() {
		
		BufferedImage img = selectedMarkerImg;
		Graphics2D g2d = img.createGraphics();
		
		g2d.setColor(Color.LIGHT_GRAY);
		int r = INNER_INFLUENCE_R + 5;
		g2d.fillOval(img.getWidth() / 2 - r, img.getHeight() / 2 - r, r * 2, r * 2);
	}
	
	private static void generateOverCreepMarkerImg() {
		
		BufferedImage img = overCreepMarkerImg;
		Graphics2D g2d = img.createGraphics();
		
		Color color = new Color(255, 0, 0, 50);	
		g2d.setColor(color);
		int r = INNER_INFLUENCE_R - 2;
		g2d.fillOval(img.getWidth() / 2 - r, img.getHeight() / 2 - r, r * 2, r * 2);
	}
			
	public void draw(Graphics2D g2d) {
		
		// Draw the circle of outer influence
		if(Debugging.drawOuterInfluence) {
			g2d.setColor(Color.BLACK);
			g2d.drawOval(pos().intX() - OUTER_INFLUENCE_R, pos().intY() - OUTER_INFLUENCE_R, OUTER_INFLUENCE_R * 2, OUTER_INFLUENCE_R * 2);
		}
		
		// Draw the circle of inner influence
		if(Debugging.drawInnerInfluence) {
			g2d.setColor(Color.BLACK);
			int x = pos().intX() - INNER_INFLUENCE_R;
			int y = pos().intY() - INNER_INFLUENCE_R;
			g2d.drawOval(x, y, INNER_INFLUENCE_R * 2, INNER_INFLUENCE_R * 2);
		}
		
		// Draw the circle of inner influence
		
		switch (Player.getHumanPlayer().getRelation(this.player())) {
		
		case Player.SAME:
			
			if(selected()) {
				drawImgAtPos(g2d, selectedMarkerImg, pos());
			}
			
			drawImgAtPos(g2d, myImg, pos());
			
			if(overCreep) {
				drawImgAtPos(g2d, overCreepMarkerImg, pos());
			}
			
			break;

		case Player.ALLIES:
			
			drawImgAtPos(g2d, allyImg, pos());
			break;
			
		case Player.FOES:
			
			drawImgAtPos(g2d, enemyImg, pos());
			break;
			
		default:
			break;
		}
	}
	
	/*
	 * Setters
	 */
	
	public void setDest(Pt newDest) {
		movementDest.set(newDest);
		enRoute = true; // the larvae is en route to its destination
	}
}
