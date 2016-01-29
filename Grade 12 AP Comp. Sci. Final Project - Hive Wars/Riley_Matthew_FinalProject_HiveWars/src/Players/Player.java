/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package Players;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.ListIterator;

import GameEntities.GameEntity;
import GameEntities.Hive;
import GameEntities.Larva;
import GameEntities.Pod;
import Positioning.Grid;
import Positioning.Pt;


public abstract class Player {
	
	public static final int MAX_NUM_LARVAE = 150;
	
	// Keep a reference to the human player (aka the user)
	private static Player humanPlayer;
	
	// Each player is given a unique signature
	private static int sigCount = 0;
	private int sig;
	
	// The relations between all of the players (stored as the allies or foes)
	private static int[][] playerRelations = new int[10][10];
	public static final int ALLIES = 0, FOES = 1, SAME = 2;
	
	// The player name
	private String name;
	
	// General-purpose Lists
	private LinkedList<GameEntity> allEntities = new LinkedList<>();
	private LinkedList<Player> enemyPlayers = new LinkedList<>();
	
	// Sublists of allEntities
	private LinkedList<Larva> allLarvae = new LinkedList<>();
	private LinkedList<Hive> allHives = new LinkedList<>();
	
	// List of active pods
	private LinkedList<Pod> allPods = new LinkedList<>();
	
	public Player(String argName) {
		
		name = argName;
		
		// Assign the player a unique signature
		sig = sigCount;
		sigCount++;
		
		// Establish this player's relation with itself
		playerRelations[sig][sig] = SAME;
	}
	
	public void addEnemy(Player enemy) {
		
		playerRelations[sig][enemy.sig] = FOES;
		playerRelations[enemy.sig][sig] = FOES;
		
		synchronized(enemyPlayers) {
			enemyPlayers.add(enemy);
		}
	}
	
	public void addEnemies(Player... enemies) {
		
		for(Player e : enemies) {
			addEnemy(e);
		}
	}
	
	/*
	 * Unit creation
	 */
	
	public Larva spawnLarvae(float x, float y) {
			
		Larva l = new Larva(x, y, this);
		
		synchronized(allEntities) {
			allEntities.add(l);
		}
		
		synchronized(allLarvae) {
			allLarvae.add(l);
		}
		
		// Add the larva to the grid
		Grid grid = Grid.getInstance();
		grid.addToTile(l);
		
		return l;
	}
	
	public Hive spawnHive(float x, float y, boolean born) {
		
		Hive h = new Hive(x, y, born, this);
		synchronized(allEntities) {
			allEntities.add(h);
		}
		
		synchronized(allHives) {
			allHives.add(h);
		}
		
		// Add the hive to the grid
		Grid grid = Grid.getInstance();
		grid.addToTile(h);
		
		return h;
	}
	
	public Pod spawnPod(Pt dest, LinkedList<Larva> availableLarva) {
		
		Pod p = new Pod(dest, availableLarva, this);
		
		synchronized(allPods) {
			allPods.add(p);
		}
		
		return p;
	}
	
	/*
	 * Updates
	 */
	
	public abstract void update();
	
	public void updateAllPods() {
		
		synchronized(allPods) {
					
			ListIterator<Pod> it = allPods.listIterator();
			while(it.hasNext()) {
				Pod p = it.next();
				
				if(p.active()) {
					p.update();
				}
				else {
					it.remove();
				}
			}
		}	
	}
	
	public void updateAllEntities() {
		
		/*
		 * Update all of my entities, and remove dead entities along the way
		 */
		
		// Update all larvae
		
		synchronized(allLarvae) {
			
			ListIterator<Larva> itMyLarvae = allLarvae.listIterator();
			while(itMyLarvae.hasNext()) {
				Larva l = itMyLarvae.next();
				
				if(l.alive()) {
					l.update();
				}
				else {
					itMyLarvae.remove();
				}
			}
		}
		
		// Update all hives
		
		synchronized(allHives) {
			
			ListIterator<Hive> itMyHives = allHives.listIterator();
			while(itMyHives.hasNext()) {
				Hive h = itMyHives.next();
				
				if(h.alive()) {
					h.update();
				}
				else {
					itMyHives.remove();
				}
			}
		}
		
		// Remove the dead from myEntities
		
		synchronized(allEntities) {
		
			ListIterator<GameEntity> itMyEntities = allEntities.listIterator();
			while(itMyEntities.hasNext()) {
				GameEntity e = itMyEntities.next();
				
				if(!e.alive()) {
					itMyEntities.remove();
				}
			}
		}
	}
	
	/*
	 * Drawing
	 */
	
	public void drawAllLarvae(Graphics2D g2d) {
		
		LinkedList<Larva> allMyLarvae = allLarvae();
		
		synchronized(allMyLarvae) {
			
			for(Larva l : allMyLarvae) {
				
				l.draw(g2d);
			}
		}
	}
	
	public void drawAllHiveMains(Graphics2D g2d) {
		
		LinkedList<Hive> allMyHives = allHives();
		
		synchronized(allMyHives) {
			
			for(Hive h : allMyHives) {
				
				h.draw(g2d);
			}
		}
	}
	
	public void drawAllHiveMucus(Graphics2D g2d) {
		
		LinkedList<Hive> allMyHives = allHives();
		
		synchronized(allMyHives) {
			
			for(Hive h : allMyHives) {
				
				h.drawMucus(g2d);
			}
		}
	}
	
	/*
	 * Accessors
	 */
	
	public String name() {
		return name;
	}
	
	public LinkedList<GameEntity> allEntities() {
		return allEntities;
	}
	
	public LinkedList<Larva> allLarvae() {
		return allLarvae;
	}
	
	public LinkedList<Hive> allHives() {
		return allHives;
	}
	
	public LinkedList<Pod> allPods() {
		return allPods;
	}
	
	public LinkedList<Player> enemyPlayers() {
		return enemyPlayers;
	}
	
	public LinkedList<Hive> allEnemyHives() {
		
		LinkedList<Hive> allEnemyHives = new LinkedList<Hive>();
		
		synchronized(enemyPlayers) {
			
			for(Player e : enemyPlayers) {
				
				allEnemyHives.addAll(e.allHives);
			}
		}
		
		return allEnemyHives;
	}
	
	public LinkedList<Larva> allEnemyLarvae() {
		
		LinkedList<Larva> allEnemyLarvae = new LinkedList<Larva>();
		
		synchronized(enemyPlayers) {
			
			for(Player e : enemyPlayers) {
				
				allEnemyLarvae.addAll(e.allLarvae);
			}
		}
		
		return allEnemyLarvae;	
	}
	
	public LinkedList<Pt> getAllHivePositions() {
		
		LinkedList<Pt> hivePositions = new LinkedList<Pt>();
		
		synchronized(allHives) {
			
			for(Hive h : allHives) {
				hivePositions.add(h.pos());
			}
		}
		
		synchronized(enemyPlayers) {
			
			for(Player enemy : enemyPlayers) {
				
				synchronized(enemy.allHives) {
					
					for(Hive h : enemy.allHives) {
						
						hivePositions.add(h.pos());
					}
				}
			}
		}
		
		return hivePositions;
	}
	
	public LinkedList<GameEntity> getAllEnemyEntities() {
		
		LinkedList<GameEntity> allEnemyEntities = new LinkedList<>();
		
		synchronized(enemyPlayers) {
			
			for(Player enemy : enemyPlayers) {
				allEnemyEntities.addAll(enemy.allEntities);
			}
		}
		
		return allEnemyEntities;
	}
	
	public int getNumLarvae() {
		synchronized(allLarvae) {
			return allLarvae.size();
		}
	}
	
	public boolean ableToSpawn() {
		
		return getNumLarvae() < MAX_NUM_LARVAE;
	}
	
	public int sig() {
		return sig;
	}
	
	public void setHumanPlayer(Player argHumanPlayer) {
		humanPlayer = argHumanPlayer;
	}
	
	public static Player getHumanPlayer() {
		return humanPlayer;
	}
	
	public int getRelation(Player other) {
		
		return getRelation(humanPlayer, other);
	}
	
	public static int getRelation(Player a, Player b) {
		
		return playerRelations[a.sig][b.sig];
	}
	
	public boolean isDefeated() {
		return allHives.size() == 0;
	}
}
