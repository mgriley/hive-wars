/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package GameEntities;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import Players.Player;
import Positioning.Grid;
import Positioning.GridTile;
import Positioning.Pt;


public abstract class GameEntity {
	
	// References for unit tracking
	private Player myPlayer;
	
	// Reference to the grid tile containing this game entity
	private GridTile tile;
	
	// Physical Attributes
	private Pt pos;
	private int radius;
	
	// Combat
	private boolean alive;
	private int health;
	
	// Selection
	private boolean selected;
	
	public GameEntity(Player myPlayer, float x, float y, int radius, int maxHealth) {
		
		this.myPlayer = myPlayer;
		this.pos = new Pt(x, y);
		this.radius = radius;
		health = maxHealth;
		
		// Add this entity to the grid
		Grid grid = Grid.getInstance();
		grid.addToTile(this);
		
		alive = true;
	}
	
	/*
	 * Drawing
	 */
	
	public abstract void draw(Graphics2D g2d);
	
	public static void drawImgAtPos(Graphics2D g2d, BufferedImage img, Pt p) {
		
		g2d.drawImage(img, p.intX() - img.getWidth() / 2, p.intY() - img.getHeight() / 2, null);
	}
	
	/*
	 * Updating
	 */
	
	public abstract void update();
	
	/*
	 * Combat
	 */
	
	public void damage(float damage) {
		health -= damage;
		
		if(health < 1) {
			kill();
		}
	}
	
	/*
	 * Right click event
	 */
	
	public abstract void onRightClick(float x, float y);
	
	/*
	 * Utilities
	 */
	
	// Return the entity nearest to the given position
	public static GameEntity getNearest(Pt pos, List<? extends GameEntity> entities) {
		
		GameEntity nearest = null;
		float nearestDist = Integer.MAX_VALUE;
		
		synchronized(entities) {
			
			for(GameEntity e : entities) {
				
				float dist = e.pos.distSqToPt(pos);
				
				if(dist < nearestDist) {
					nearest = e;
					nearestDist = dist;
				}
			}
		}
		
		return nearest;
	}
	
	/*
	 * Accessors
	 */
	
	public Pt pos() {
		return pos;
	}
	
	public boolean selected() {
		return selected;
	}
	
	public int health() {
		return health;
	}
	
	public int radius() {
		return radius;
	}
	
	public Player player() {
		return myPlayer;
	}
	
	public boolean alive() {
		return alive;
	}
	
	public GridTile tile() {
		return tile;
	}
	
	/*
	 * Setters
	 */
	
	public void kill() {
		
		tile.remove(this);
		
		alive = false;
	}
	
	public void setTile(GridTile argTile) {
		tile = argTile;
	}
	
	/*
	 * Selection setters
	 */
	
	public void select() {
		selected = true;
	}
	
	public void deselect() {
		selected = false;
	}
	
	/*
	 * Utility
	 */
	
	public String toString() {
		
		return "Type: " + this.getClass().getName() + ", Pos: " + pos.toString() + ", Alive: " + alive; 
	}
}