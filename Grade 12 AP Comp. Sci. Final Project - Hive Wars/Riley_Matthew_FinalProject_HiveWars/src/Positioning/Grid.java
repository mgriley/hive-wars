/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package Positioning;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import GameEntities.GameEntity;
import GameEntities.Hive;
import GameEntities.Larva;
import Players.Player;


public class Grid {
	
	public static Grid instance;
	
	// The width and height of the grid
	private int width, height;
	
	// The width and height of each tile in this grid
	private int tileWidth, tileHeight;
	
	// 2D array to store all of the grid data
	private GridTile[][] gridArr;
	
	public Grid() {
		
	}
	
	public static Grid initGrid(int argWidth, int argHeight, int numRows, int numCols) {
		
		Grid grid = Grid.getInstance();
		
		grid.width = argWidth;
		grid.height = argHeight;
		
		grid.tileWidth = grid.width / numRows;
		grid.tileHeight = grid.height / numCols;
		
		grid.gridArr = new GridTile[numRows][numCols];
		
		for(int row = 0; row < grid.gridArr.length; row++) {
			for(int col = 0; col < grid.gridArr[row].length; col++) {
				
				grid.gridArr[row][col] = new GridTile(row, col);
			}
		}
		
		return grid;
	}
	
	public static Grid getInstance() {
		
		if(instance == null) {
			instance = new Grid();
		}
		return instance;
	}
	
	/*
	 * Tile validation
	 */
	
	public boolean validPos(int row, int col) {
		
		boolean validRow = 0 < row && row < gridArr.length;
		boolean validCol = 0 < col && col < gridArr[0].length;
		
		return validRow && validCol;
	}
	
	/*
	 * Adjacent tile access
	 */
	
	public GridTile getRelative(GridTile current, int rowOffset, int colOffset) {
		
		int newRow = current.row() + rowOffset;
		int newCol = current.col() + colOffset;
		
		if(validPos(newRow, newCol)) {
			return gridArr[newRow][newCol];
		}
		else {
			return null;
		}
	}
	
	/*
	 * Neighbours/nearby list accessors
	 */
	
	public void saturateNeighbourLists(GameEntity e, List<Larva> nearbyLarvae, List<Hive> nearbyHives, List<GameEntity> nearbyFoes) {
		
		// Get the adjacent tiles (including adjacent diagonals)
		
		GridTile current = e.tile();
		
		GridTile left = getRelative(current, 0, -1);
		GridTile right = getRelative(current, 0, 1);
		GridTile upper = getRelative(current, -1, 0);
		GridTile lower = getRelative(current, 1, 0);
		
		GridTile upperLeft = getRelative(current, -1, -1);
		GridTile upperRight = getRelative(current, -1, 1);
		GridTile botLeft = getRelative(current, 1, -1);
		GridTile botRight = getRelative(current, 1, 1);
		
		// Make a list of these tiles, which is essentially the tile neighbourhood
		List<GridTile> neighbourhood = new LinkedList<>();
		neighbourhood.add(current);
		neighbourhood.add(left);
		neighbourhood.add(right);
		neighbourhood.add(upper);
		neighbourhood.add(lower);
		neighbourhood.add(upperLeft);
		neighbourhood.add(upperRight);
		neighbourhood.add(botLeft);
		neighbourhood.add(botRight);
		
		// Iterate through all of the entities in the neighbourhood, and adding them to the relevant lists
		for(GridTile tile : neighbourhood) {
			
			// Skip null tiles
			if(tile == null) {
				continue;
			}
			
			Iterator<GameEntity> it = tile.units().listIterator();
			while(it.hasNext()) {
				
				GameEntity neighbour = it.next();
				
				// Skip the given entity
				if(neighbour == e) {
					continue;
				}
				
				// Skip and remove dead entities
				if(!neighbour.alive()) {
					//System.out.println("A dead neighbour! of class " + e.getClass().toString());
					it.remove(); // remove the dead neighbour
					continue;
				}
					
				// Add the neighbour to the relevant lists
				
				if(neighbour instanceof Larva) {
					nearbyLarvae.add((Larva) neighbour);
				}
				else {
					nearbyHives.add((Hive) neighbour);
				}
				
				if(Player.getRelation(e.player(), neighbour.player()) == Player.FOES) {
					nearbyFoes.add(neighbour);
				}
			}		
		}
	}
	
	// Add a new entity to the necessary tile
	// Return the tile that the entity is added to
	public void addToTile(GameEntity e) {
		
		GridTile tile = getTile(e.pos());
		tile.add(e);
	}
	
	public void updateTile(Larva lar, Pt newPos) {
			
		// Get the tile that the unit will move on to
		GridTile newTile = getTile(newPos);	
		
		// If the entity changed tiles:
		if(lar.tile() != newTile) {
			
			// Remove the entity from its current tile
			lar.tile().remove(lar);
			
			// Add the entity to its new tile
			// This method also reset's the larva's tile reference
			newTile.add(lar);	
		}
	}
	
	private GridTile getTile(Pt pos) {
		
		int rowNum = pos.intY() / tileHeight;
		int colNum = pos.intX() / tileWidth;
		
		// Constrain the row and col #s to prevent array out of index exceptions
		rowNum = Math.min(rowNum, gridArr.length - 1);
		rowNum = Math.max(rowNum, 0);
		colNum = Math.min(colNum, gridArr[0].length - 1);
		colNum = Math.max(colNum, 0);	
		
		return gridArr[rowNum][colNum];
	}
	
	// Draw the grid-lines
	public void draw(Graphics2D g2d) {
		
		g2d.setColor(Color.BLACK);
		
		for(int x = tileWidth; x < width; x += tileWidth) {
		
			Pt a = new Pt(x, 0);
			Pt b = new Pt(x, height);
			a.connect(g2d, b);
		}
		
		for(int y = tileHeight; y < height; y += tileHeight) {
			
			Pt a = new Pt(0, y);
			Pt b = new Pt(width, y);
			a.connect(g2d, b);
		}
	}
}
