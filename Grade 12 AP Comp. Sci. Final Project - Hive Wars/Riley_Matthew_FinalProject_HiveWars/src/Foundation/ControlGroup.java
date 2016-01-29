/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package Foundation;

import java.awt.event.KeyEvent;
import java.util.LinkedList;

import GameEntities.GameEntity;
import Players.HumanPlayer;
import Positioning.Pt;
import States.GameState;
import Utility.KeyTracker;
import Utility.Timer;

public class ControlGroup {
	
	private static GameState gameState;
	private static HumanPlayer myPlayer;
	
	// List of all of the ten groups
	private static ControlGroup[] allGroups = new ControlGroup[10];
	
	// List of the game entities contained within a group
	private LinkedList<GameEntity> group = new LinkedList<>();
	
	// For distinguishing single and double clicks:
	private Timer clickTimer; 
	private final int MIN_MILLIS = 500;
	
	public ControlGroup() {
	}
	
	public void clickEvent() {

		// Single-click event
		if(clickTimer == null || clickTimer.millisElapsed() > MIN_MILLIS) {
			
			// Redefine the control group
			if(KeyTracker.isPressed(KeyEvent.VK_CONTROL)) {
				
				synchronized(group) {
					
					group.clear();
					group.addAll(myPlayer.selectedEntities());
				}
			}
			
			// Add to the control group
			else if(KeyTracker.isPressed(KeyEvent.VK_SHIFT)) {

				synchronized(myPlayer.selectedEntities()) {
					
					for(GameEntity e : myPlayer.selectedEntities()) {
						
						synchronized(group) {
							
							if(!group.contains(e)) {
								group.add(e);
							}
						}
					}
				}
			}
			
			// Select the members of the group
			else {
				
				myPlayer.deselectMyEntities();
				myPlayer.selectThese(group);
			}
			
			// Init the timer
			if(clickTimer == null) {
				clickTimer = new Timer();
			}
			
		}
		
		// Double-click event
		else {
			
			// Determine the center of the positions of the entities in the group
			Pt groupCentre = new Pt(0, 0);
			
			for(GameEntity e : group) {
				groupCentre.add(e.pos());
			}
			
			groupCentre.multiply(1.0f / group.size());
			
			// Centre the camera on this point
			gameState.centreCamOnPt(groupCentre);
		}
		
		// Reset the timer
		clickTimer.restart();
	}
	
	// Create all of the groups
	public static void initGroups(GameState argGameState, HumanPlayer argMyPlayer) {
		
		gameState = argGameState;
		myPlayer = argMyPlayer;
		
		for(int i = 0; i < allGroups.length; i++) {
			allGroups[i] = new ControlGroup();
		}
	}
	
	// Precondition: 0 <= index <= 9
	public static void accessGroup(int index) {
		
		// Access the relevant control group
		ControlGroup c = allGroups[index];
		c.clickEvent();
	}
	
	/*
	 * Accessors
	 */
	
	public static int numGroups() {
		return allGroups.length;
	}
	
	public static int sizeOfGroup(int index) {
		return allGroups[index].group.size();
	}
}