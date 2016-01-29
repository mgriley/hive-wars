/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package Players;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.LinkedList;

import GameEntities.GameEntity;
import GameEntities.Hive;
import GameEntities.Larva;
import GameEntities.Pod;
import Positioning.Pt;

public class HumanPlayer extends Player {
	
	// For selection
	private static final int MOUSE_MIN_DIST = 5;
	
	public HumanPlayer(String name) {
		super(name);
		
		super.setHumanPlayer(this);
	}
	
	/*
	 * Updates
	 */
	
	@Override
	public void update() {
		
		
		updateAllEntities();
		updateAllPods();
	}
	
	/*
	 * Right-click management
	 */
	
	public void handleRightClick(float x, float y) {
		
		LinkedList<GameEntity> selectedEntities = selectedEntities();
		
		for(GameEntity e : selectedEntities) {
			e.onRightClick(x, y);
		}
	}
	
	/*
	 * Selection Utilities
	 */
	
	public void performSelection(Pt startCorner, Pt endCorner, boolean additive) {
		
		// Find the distance between the start and end points
		float dist = startCorner.distToPt(endCorner);
		
		if(dist > MOUSE_MIN_DIST) {
			rectSelect(startCorner, endCorner, additive);
		}
		else {
			singleSelect(startCorner, additive);
		}
	}
	
	private void singleSelect(Pt clickPos, boolean additive) {
		
		// First, deselect all, unless additive
		if(!additive) {
			deselectMyEntities();
		}
		
		// Select the nearest larvae within selection distance
		GameEntity nearest = GameEntity.getNearest(clickPos, allEntities());
		if(nearest != null && nearest.pos().distToPt(clickPos) < nearest.radius()) {
			
			nearest.select();
		}
	}
	
	private void rectSelect(Pt startCorner, Pt endCorner, boolean additive) {
		
		Pt topLeft = new Pt();
		topLeft.setX(Math.min(endCorner.x(), startCorner.x()));
		topLeft.setY(Math.min(endCorner.y(), startCorner.y()));
		
		int width = (int) Math.abs(endCorner.x() - startCorner.x());
		int height = (int) Math.abs(endCorner.y() - startCorner.y());
		
		Rectangle rect = new Rectangle(topLeft.intX(), topLeft.intY(), width, height);
		
		LinkedList<GameEntity> allMyEntities = allEntities();
		synchronized(allMyEntities) {
			
			for(GameEntity e : allMyEntities) {
				
				if(rect.contains(e.pos().x(), e.pos().y())) {
					e.select();
				}
				else {
					
					if(!additive) {
						e.deselect();
					}
				}
			}
		}
	}
	
	/*
	 * Selection getters and setters
	 */
	
	public void selectMyEntities() {
		
		synchronized(allEntities()) {
			
			for(GameEntity e : allEntities()) {
				e.select();
			}
		}
	}
	
	public void selectThese(LinkedList<GameEntity> list) {
		
		deselectMyEntities();
		
		synchronized(list) {
			
			for(GameEntity e : list) {
				e.select();
			}
		}
	}
	
	public void deselectMyEntities() {
		
		synchronized(allEntities()) {
			
			for(GameEntity e : allEntities()) {
				e.deselect();
			}
		}
	}
	
	public int getNumLarvaeSelected() {
		int numLarvae = 0;
		
		LinkedList<Larva> allMyLarvae = allLarvae();
		
		synchronized(allMyLarvae) {
			
			for(Larva l : allMyLarvae) {
				if(l.selected()) {
					numLarvae++;
				}
			}
			return numLarvae;
		}
	}
	
	public LinkedList<Larva> getSelectedLarvae() {
		
		LinkedList<Larva> selectedLarvae = new LinkedList<>();
		LinkedList<Larva> allMyLarvae = allLarvae();
		
		synchronized(allMyLarvae) {
			
			for(Larva l : allMyLarvae) {
				if(l.selected()) {
					selectedLarvae.add(l);
				}
			}
		}
		
		return selectedLarvae;
	}
	
	/*
	 * Drawing
	 */
	
	public void drawAllHiveSpawnDests(Graphics2D g2d) {
	
		LinkedList<Hive> allMyHives = allHives();
		
		synchronized(allMyHives) {
			
			for(Hive h : allMyHives) {
				
				h.drawSpawnDest(g2d);
			}
		}
	}
	
	public void drawAllPods(Graphics2D g2d) {
		
		LinkedList<Pod> allMyPods = allPods();
		
		synchronized(allMyPods) {
			
			for(Pod p : allMyPods) {
				
				p.draw(g2d);
			}
		}
	}
	
	/*
	 * Utilities
	 */
	
	public LinkedList<GameEntity> selectedEntities() {
		
		LinkedList<GameEntity> selectedEntities = new LinkedList<>();
		LinkedList<GameEntity> allMyEntities = allEntities();;
		
		synchronized(allMyEntities) {
			
			for(GameEntity e : allMyEntities) {
				if(e.selected()) {
					selectedEntities.add(e);
				}
			}
		}
		
		return selectedEntities;
	}
}
