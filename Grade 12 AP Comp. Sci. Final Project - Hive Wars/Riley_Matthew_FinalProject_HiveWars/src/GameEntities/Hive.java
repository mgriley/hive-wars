/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package GameEntities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import Players.Player;
import Positioning.Pt;
import Utility.Debugging;
import Utility.Timer;


public class Hive extends GameEntity {
	
	// Images
	private static BufferedImage myImg;
	private static BufferedImage enemyImg;
	private static BufferedImage selectedMarker;
	
	// General 
	public static final int R = 50;
	private static final int MUCUS_GROWTH_RATE = 1;
	
	// For birth
	private boolean born;
	private Timer birthTimer = new Timer();
	public static final int BIRTH_TIME = 5; // in secs
	public static final int MAX_HEALTH = 300;
	
	// For spawning
	private Pt spawnDest;
	private Timer spawnTimer;
	public static final int SPAWN_FREQ = 3; // a hive spawns a larvae each SPAWN_FREQ seconds
	
	// For mucus spread
	private int mucusRadius = R;
	private Timer mucusTimer;
	private static final float MUCUS_FREQ = 0.25f;
	private final int MUCUS_MAX_RAD = 200;
	
	public Hive(float x, float y, boolean born, Player myPlayer) {
		
		super(myPlayer, x, y, R, MAX_HEALTH);
		
		spawnDest = new Pt(x, y + R + Larva.INNER_INFLUENCE_R);
		
		spawnTimer = new Timer();
		mucusTimer = new Timer();
		
		// Start the birth phase
		this.born = born;
		birthTimer.restart();
	}
	
	/*
	 * Update
	 */
	
	@Override
	public void update() {
		
		if(!born) {
			
			if(birthTimer.secsElapsed() > BIRTH_TIME) {
				born = true;
			}
		}
		else {
			
			// Spawn new larvae
			if(player().ableToSpawn() && spawnTimer.secsElapsed() > SPAWN_FREQ) {
				spawnLarvae();
				spawnTimer.restart();
			}
			
			// Secrete mucus
			if(mucusRadius< MUCUS_MAX_RAD && mucusTimer.secsElapsed() > MUCUS_FREQ) {
				
				mucusRadius += MUCUS_GROWTH_RATE;
				mucusTimer.restart();
			}
		}
	}
	
	/*
	 * Right click event
	 */
	
	@Override
	public void onRightClick(float x, float y) {
			
		// Set the spawn destination
		setSpawnDest(x, y);
	}
	
	/*
	 * Drawing
	 */
	
	public static void initImages() {
		
		myImg = new BufferedImage(R * 2, R * 2, BufferedImage.TRANSLUCENT); 
		enemyImg = new BufferedImage(R * 2, R * 2, BufferedImage.TRANSLUCENT); 
		
		int markerR = R + 10;
		selectedMarker = new BufferedImage(markerR * 2, markerR * 2, BufferedImage.TRANSLUCENT);
		
		generateBodyImg(myImg, Color.GREEN);
		generateBodyImg(enemyImg, Color.RED);
		generateSelectedMarker(selectedMarker, markerR);
	}
	
	private static void generateBodyImg(BufferedImage img, Color color) {
		
		Graphics2D g2d = img.createGraphics();
		
		g2d.setColor(color);
		g2d.fillOval(img.getWidth() / 2 - R, img.getHeight() / 2 - R, R * 2, R * 2);
	}
	
	private static void generateSelectedMarker(BufferedImage img, int r) {
		
		Graphics2D g2d = img.createGraphics();
		
		g2d.setColor(Color.GRAY);
		g2d.fillOval(img.getWidth() / 2 - r, img.getHeight() / 2 - r, r * 2, r * 2);
	}
	
	public void drawMucus(Graphics2D g2d) {
		
		// Draw the hive mucus
		
		g2d.setColor(Color.LIGHT_GRAY);
		
		g2d.drawOval(pos().intX() - mucusRadius, pos().intY() - mucusRadius, mucusRadius * 2, mucusRadius * 2);
	}
	
	public void drawSpawnDest(Graphics2D g2d) {
		
		// Draw a line to the spawn destination
		
		Stroke original = g2d.getStroke();
		
		g2d.setColor(Color.PINK);
		int dashOneLength = 3;
		int dashInterval = 5;
		int dashTwoLength = dashOneLength * 5;
		g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{dashOneLength, dashInterval, dashTwoLength, dashInterval}, 0));
		g2d.drawLine(pos().intX(), pos().intY(), spawnDest.intX(), spawnDest.intY()); 
		
		// Reset the stroke to the original
		g2d.setStroke(original);
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		
		// Draw the main hive
		
		switch (Player.getHumanPlayer().getRelation(this.player())) {
		
		case Player.SAME:
			
			if(selected()) {
				drawImgAtPos(g2d, selectedMarker, pos());
			}
			drawImgAtPos(g2d, myImg, pos());
			
			break;

		case Player.ALLIES:
			
			break;
			
		case Player.FOES:
			
			drawImgAtPos(g2d, enemyImg, pos());
			break;
			
		default:
			break;
		}			
		
		if(Debugging.printHiveHealth) {
			g2d.setColor(Color.BLACK);
			g2d.drawString(health() + "", pos().intX(), pos().intY());
		}
	}
	
	public static void drawMock(Graphics2D g2d, Pt pos) {
		
		Stroke original = g2d.getStroke();
		g2d.setColor(Color.MAGENTA);
		int dashOneLength = 3;
		int dashInterval = 5;
		int dashTwoLength = dashOneLength * 5;
		g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{dashOneLength, dashInterval, dashTwoLength, dashInterval}, 0));
		g2d.drawOval(pos.intX() - R, pos.intY() - R, R * 2, R * 2);
		g2d.setStroke(original);
	}
	
	/*
	 * Spawning
	 */
	
	private void spawnLarvae() {
		
		Pt spawnLoc = spawnDest.getNormalized();
		spawnLoc.multiply(R + Larva.INNER_INFLUENCE_R);
		spawnLoc.add(pos());
		
		Larva l = player().spawnLarvae(spawnLoc.x(), spawnLoc.y());
		if(l != null) {		
			l.setDest(spawnDest);		
		}
	}
	
	public void setSpawnDest(float x, float y) {
		spawnDest.set(x, y);
	}
	
	/*
	 * Accessors
	 */
	
	public int mucusRadius() {
		return mucusRadius;
	}
}