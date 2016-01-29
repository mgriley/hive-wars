/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package Positioning;

import java.util.LinkedList;
import java.util.List;

import GameEntities.GameEntity;

public class GridTile {
	
	// List of units contained in this single grid tile
	private LinkedList<GameEntity> units;
	
	// The position of the tile in the grid array
	private int row, col;
	
	public GridTile(int argRow, int argCol) {
		
		row = argRow;
		col = argCol;
		
		units = new LinkedList<GameEntity>();
	}
	
	public List<GameEntity> units() {
		return units;
	}
	
	public int row() {
		return row;
	}
	
	public int col() {
		return col;
	}
	
	public void remove(GameEntity e) {
		units.remove(e);
	}
	
	public void add(GameEntity e) {
		units.add(e);
		e.setTile(this);
	}
}
