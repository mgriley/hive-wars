/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package GameEntities;
import java.awt.Graphics2D;
import java.util.LinkedList;

import Players.Player;
import Positioning.Pt;


// TODO: make this thread-safe

public class Pod {
	
	public final static int SIZE = 5;
	public final static int RANGE = 2;
	
	private Player myPlayer;
	private LinkedList<Larva> larvaePod = new LinkedList<>();
	private Pt dest; // the destination of the new hive
	private boolean active; // true if the pod still has to be created, false if the pod should be disposed of
	
	public Pod(Pt dest, LinkedList<Larva> listOfAvailable, Player argMyPlayer) {
		
		this.dest = new Pt(dest); // the destination of the new hive
		myPlayer = argMyPlayer; // the player who owns this larvae pod
		
		// If there are enough larvae to make a pod:
		if(listOfAvailable.size() >= Pod.SIZE) {
			
			// Activate the pod
			active = true;
			
			for(int i = 0; i < SIZE; i++) {
				
				// Find the larva in the list that is nearest to the destination
				Larva nearestLarvae = null;
				float nearestDistSq = 0;
				for(Larva lar : listOfAvailable) {
					
					if(! larvaePod.contains(lar)) {
						
						// Set the first valid larva to the nearest (until another one nearer is found)
						if(nearestLarvae == null) {
							nearestLarvae = lar;
							nearestDistSq = lar.pos().distSqToPt(dest);
						}
						
						// Compare and update the nearest larvae and nearest distance squared
						float distSq = lar.pos().distSqToPt(dest);
						if(distSq < nearestDistSq) {
							nearestLarvae = lar;
							nearestDistSq = distSq;
						}
					}
				}
				
				// Set the larva's dest to the site of the new hive, deselect it, and add it to the pod
				if(nearestLarvae != null) {
					
					nearestLarvae.setDest(dest);
					nearestLarvae.deselect();
					
					synchronized(larvaePod) { 
						larvaePod.add(nearestLarvae);
					}
				}
				else {
					
					// If there is a null larvae error, deactivate the pod
					System.out.println("Null larva for pod");
					active = false;
				}
			}
		}
		
		// There are not enough larvae, so don't make the pod
		else {
			
			System.out.println("Not enough larvae to make a pod");
			active = false;
		}
	}
	
	public void draw(Graphics2D g2d) {
		
		// Draw a mock hive at the target location
		Hive.drawMock(g2d, dest);
	}
	
	public void update() {
		
		// If all of the larvae are within a set distance of the hive destination, create the hive
		boolean createHive = true;
		
		synchronized(larvaePod) {
			
			for(Larva l : larvaePod) {
				
				// If the larva is still alive and on track:
				if(l.alive() && l.movementDest.equals(dest)) {
					
					// If the larvae are close enough to create the hive:
					if(l.pos().distSqToPt(dest) > Math.pow(Hive.R + Pod.SIZE * RANGE, 2)) {
						
						// If one larvae in the pod is too far away, cannot create the hive
						createHive = false;
						
						break;
					}
				}
				else {
					
					// Deactivate the pod b/c a larvae is dead or off track
					active = false;
					return;
				}
			}
		}
		
		if(createHive) {
			
			// TODO: check that the location is clear and still valid
			
			// Create a new, unborn hive
			myPlayer.spawnHive(dest.x(), dest.y(), false);
			
			// Deactivate all of the larvae in the pod
			synchronized(larvaePod) {
				
				for(Larva l : larvaePod) {
					l.kill();
				}
			}
			
			// Deactivate the pod
			active = false;
		}
	}
	
	/*
	 * Accessors
	 */
	
	public Pt dest() {
		return dest;
	}
	
	public LinkedList<Larva> larvaePod() {
		return larvaePod;
	}
	
	public boolean active() {
		return active;
	}
}
