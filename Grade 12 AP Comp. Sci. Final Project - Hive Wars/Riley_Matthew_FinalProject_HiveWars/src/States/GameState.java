/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package States;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import Foundation.ControlGroup;
import Foundation.GamePanel;
import GameEntities.Hive;
import GameEntities.Larva;
import GameEntities.Pod;
import Players.AIPlayer;
import Players.HumanPlayer;
import Players.Player;
import Positioning.Grid;
import Positioning.Pt;
import Utility.Debugging;
import Utility.KeyTracker;
import Utility.Settings;
import Utility.Timer;
import Utility.Util;

public class GameState extends State {
	
	// The grid that overlays the field
	private Grid grid;
	
	// Player references
	private List<Player> activePlayers, defeatedPlayers;
	private HumanPlayer myPlayer;
	private AIPlayer ai1, ai2, ai3, ai4;
	
	// For match ranking
	private boolean checkForGameOverState = true;
	
	// For match timer
	private Timer matchTimer;
	
	// Images
	private BufferedImage fullFieldImg;
	private BufferedImage fieldBackgroundImg;
	
	// For unit selections
	private Pt startSelect = new Pt();
	
	// For mouse tracking
	private boolean leftMousePressed = false;
	private Pt mouseLocal = new Pt(0, 0);
	private Pt mouseGlobal = new Pt(0, 0);
	private Pt globalOffset = new Pt(0, 0);
	
	// For HUD elements
	private boolean showMiniMap = true;
	private boolean showControlGroupDock = true;
	private Rectangle hudPanel = new Rectangle();
	
	// For mini-map
	private Rectangle miniMapRect = new Rectangle();
	private boolean startedInMiniMap = false;
	
	// For hive creation
	private boolean placingHive = false;
	private boolean startedInHiveCreation = false;
	
	// For screen movement
	private Timer screenMovementTimer;
	
	public GameState() {
		super("Game State");
		
		// Init the players and establish player relations
		
		myPlayer = new HumanPlayer("Human");
		
		ai1 = new AIPlayer("foo 1.0");
		ai2 = new AIPlayer("foo 2.0");
		ai3 = new AIPlayer("foo 3.0");
		ai4 = new AIPlayer("foo 4.0");
		
		myPlayer.addEnemies(ai1, ai2, ai3, ai4);
		ai1.addEnemies(ai2, ai3, ai4);
		ai2.addEnemies(ai1, ai3, ai4);
		ai3.addEnemies(ai1, ai2, ai4);
		ai4.addEnemies(ai1, ai2, ai3);
		
		ai1.addEnemy(myPlayer);
		
		activePlayers = new LinkedList<Player>();
		defeatedPlayers = new ArrayList<Player>();
		
		activePlayers.add(myPlayer);
		activePlayers.add(ai1);
		activePlayers.add(ai2);
		activePlayers.add(ai3);
		activePlayers.add(ai4);
		
		// Set the movement margins
		GamePanel panel = GamePanel.getInstance();
		int panelWidth = panel.getWidth();
		int panelHeight = panel.getHeight();
		
		// Prepare the HUD rectangle
		hudPanel.setBounds(0, panelHeight - Settings.MINI_MAP_HEIGHT, Settings.FIELD_WIDTH, panelHeight);
		miniMapRect.setBounds(panelWidth - Settings.MINI_MAP_WIDTH, panelHeight - Settings.MINI_MAP_HEIGHT, Settings.MINI_MAP_WIDTH, Settings.MINI_MAP_HEIGHT);
		
		// Init the control groups, and link them to my player
		ControlGroup.initGroups(this, myPlayer);
		 
		// Initialize the grid
		int width = Settings.FIELD_WIDTH;
		int height = Settings.FIELD_HEIGHT;
		int numRows = 10;
		int numCols = 10;
		grid = Grid.initGrid(width, height, numRows, numCols);
		
		// Generate the images
		initImages(grid); 
		
		/*
		 *  Create the initial units
		 *  
		 *  Each player starts with 1 hive and 10 larva
		 *  The hives are evenly spaced around the circumference of a circle centred in the field centre
		 */
		
		boolean createInitialUnits = true;
		
		if(createInitialUnits) {
			
			int numStartingLarvae = 10;
			
			float angle = 0;
			float deltaAngle = 2 * (float) Math.PI / activePlayers.size();
			
			int centreX = Settings.FIELD_WIDTH / 2;
			int centreY = Settings.FIELD_HEIGHT / 2;
			
			float radius = Settings.FIELD_WIDTH / 2 - 100;
			
			for(Player p : activePlayers) {
				
				int x = (int) (Math.cos(angle) * radius + centreX);
				int y = (int) (Math.sin(angle) * radius + centreY);
				
				p.spawnHive(x, y, true);
				
				for(int i = 0; i < numStartingLarvae; i++) {
					p.spawnLarvae(x + i, y + i);
				}
				
				angle += deltaAngle;
			}
		}
	}
	
	@Override
	public void enter() {
		
		// Nothing to do here...
	}

	@Override
	public void exit() {
		
		/*
		 * TODO: Perform any cleanup necessary
		 */
	}
	
	private void initImages(Grid grid) {
		
		// Initialize all images
		
		fullFieldImg = new BufferedImage(Settings.FIELD_WIDTH, Settings.FIELD_HEIGHT, BufferedImage.TYPE_INT_RGB);
		fieldBackgroundImg = new BufferedImage(Settings.FIELD_WIDTH, Settings.FIELD_HEIGHT, BufferedImage.TYPE_INT_RGB);
		
		// Generate image graphics
		
		Graphics2D g2dBackground = fieldBackgroundImg.createGraphics();
		generateFieldBackground(g2dBackground, grid);
		
		Larva.initImages();
		Hive.initImages();
	}
	
	private void generateFieldBackground(Graphics2D g2d, Grid grid) {
		
		// Fill the background with a base color
		g2d.setColor(Color.DARK_GRAY);
		g2d.fillRect(0, 0, fieldBackgroundImg.getWidth(), fieldBackgroundImg.getHeight());
		
		g2d.setColor(Color.LIGHT_GRAY);
		
		Stroke originalStroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(1.0f));
		
		// Draw grid lines over the background
		if(Debugging.drawGridLines) {
			grid.draw(g2d);	
		}
		
		// Reset the graphics to the original stroke
		g2d.setStroke(originalStroke);
	}
	
	@Override
	public void update() {
		
		// Start the match timer (if it hasn't been started alread(
		if(matchTimer == null) {
			matchTimer = new Timer();
		}
		
		// Update the global mouse position
		updateGlobalMouse();
		
		// Move the view, based on the mouse position
		moveView();
		
		// Update all players
		for(Player p : activePlayers) {
			p.update();
		}
		
		// Check for game-over state and match rankings
		if(checkForGameOverState) {
			
			Iterator<Player> it = activePlayers.listIterator();
			while(it.hasNext()) {
				Player p = it.next();
				
				if(p.isDefeated()) {
					
					it.remove(); // remove defeated players (so that they aren't updated)
					defeatedPlayers.add(0, p);
				}
				
				// If there is only one active player left:
				if(activePlayers.size() == 1) {
					
					// Add the last player to the list of defeated players (for ranking purposes)
					defeatedPlayers.addAll(0, activePlayers);
					
					// Transition to the Game Over Screen
					GameOverState nextState = new GameOverState(defeatedPlayers);
					GamePanel.getInstance().changeToScreen(nextState);
				}
			}	
		}
	}

	@Override
	public void draw(Graphics g) {
		
		GamePanel myPanel = GamePanel.getInstance();
		
		// Get the 2D graphics
		Graphics2D g2dPanel = (Graphics2D) g;
		Graphics2D g2dField = (Graphics2D) fullFieldImg.getGraphics();
		
		if(Settings.ANTI_ALIASING) {
			Util.optimizeGraphics(g2dField);
		}
		
		// Draw the background of the panel
		g2dPanel.setColor(Color.GRAY);
		g2dPanel.fillRect(0, 0, myPanel.getWidth(), myPanel.getHeight());
		
		// Draw the background of the field
		g2dField.drawImage(fieldBackgroundImg, 0, 0, null);
		
		/*
		 * Draw all game objects/entities
		 */
		
		// Based on player vs. AI
		
		// Draw all hive mucus
		for(Player p : activePlayers) {
			p.drawAllHiveMucus(g2dField);
		}
		
		// Draw all hive main bases
		for(Player p : activePlayers) {
			p.drawAllHiveMains(g2dField);
		}
		
		// Draw the spawn destinations for my hives
		if(activePlayers.contains(myPlayer)) {
			myPlayer.drawAllHiveSpawnDests(g2dField);
		}
		
		// Draw all larvae
		for(Player p : activePlayers) {
			p.drawAllLarvae(g2dField);
		}
		
		// If my player isn't yet defeated:
		if(activePlayers.contains(myPlayer)) {
			
			// Draw all active pods
			myPlayer.drawAllPods(g2dField);
			
			// Draw a mock hive, if in hive placement mode
			if(placingHive) {
				Hive.drawMock(g2dField, mouseGlobal);
			}
		}
		
		/*
		 * Draw the selection box and hive placement, if applicable
		 */
		
		// Draw the rectangular selection box
		drawSelectionBox(g2dField);
		
		/*
		 * Draw the visible portion of the full field image to the JPanel's graphics 
		 */
		
		globalOffset.constrain(-Settings.FIELD_WIDTH + myPanel.getWidth(), -Settings.FIELD_HEIGHT - Settings.MINI_MAP_HEIGHT + myPanel.getHeight(), 0, 0);
		BufferedImage visibleField = fullFieldImg.getSubimage(-globalOffset.intX(), -globalOffset.intY(), myPanel.getWidth(), myPanel.getHeight() - Settings.MINI_MAP_HEIGHT);
		g2dPanel.drawImage(visibleField, 0, 0, myPanel);
		
		/*
		 * Draw the HUD
		 */
		
		// Draw the mini-map
		if(showMiniMap) {
			
			// Draw an border around the current view area 
			Stroke oldStroke = g2dField.getStroke();
			g2dField.setStroke(new BasicStroke(10));
			g2dField.setColor(Color.RED);
			g2dField.drawRect( -globalOffset.intX(), -globalOffset.intY(), myPanel.getWidth(), myPanel.getHeight() - Settings.MINI_MAP_HEIGHT);
			g2dField.setStroke(oldStroke);
			
			// Draw the mini-map itself
			int topLeftX = miniMapRect.x;
			int topLeftY = miniMapRect.y;
			int botRightX = topLeftX + miniMapRect.width;
			int botRightY = topLeftY + miniMapRect.height;
			g2dPanel.drawImage(fullFieldImg, topLeftX, topLeftY, botRightX, botRightY, 0, 0, fullFieldImg.getWidth(), fullFieldImg.getHeight(), myPanel);
			
			// Draw a border around the minimap
			Stroke original = g2dPanel.getStroke();
			g2dPanel.setStroke(new BasicStroke(3));
			g2dPanel.setColor(Color.GREEN);
			g2dPanel.drawRect(topLeftX, topLeftY, Settings.MINI_MAP_WIDTH, Settings.MINI_MAP_HEIGHT);
			g2dPanel.setStroke(original);
		}
		
		// Draw the control group dock
		if(showControlGroupDock) {
			
			// Draw the background
			g2dPanel.setColor(Color.LIGHT_GRAY);
			int width = myPanel.getWidth() - Settings.MINI_MAP_WIDTH;
			Rectangle dockRect = new Rectangle(0, myPanel.getHeight() - Settings.MINI_MAP_HEIGHT, width,  Settings.MINI_MAP_HEIGHT);
			g2dPanel.fill(dockRect);
			
			// Draw a border around the dock
			Stroke oldStroke = g2dPanel.getStroke();
			g2dPanel.setStroke(new BasicStroke(3));
			g2dPanel.setColor(Color.GREEN);
			g2dPanel.draw(dockRect);
			g2dPanel.setStroke(oldStroke);
			
			// Draw an indicator for each control group

			g2dPanel.setColor(Color.BLACK);
			
			int widthOffset = 200;
			int numGroups = ControlGroup.numGroups();
			int interval = (width - widthOffset) / numGroups * 2;
			for(int i = 0; i < numGroups; i++) {
				
				int y = myPanel.getHeight() - 2 * Settings.MINI_MAP_HEIGHT / 3;
				if(i > 4) {
					y += Settings.MINI_MAP_HEIGHT / 3;
				}
				
				int x = (i % 5) * interval + interval / 3 + widthOffset;
				
				g2dPanel.drawString("Group: " + i, x, y);
				g2dPanel.drawString("# Units: " + ControlGroup.sizeOfGroup(i), x, y + 20);
			}
			
			// Print various utility messages into the dock
			
			// Print the current elapsed game time (in mins)
			g2dPanel.setColor(Color.BLACK);
			float xAlign = widthOffset / 2 - 60;
			drawElapsedTime(g2dPanel, xAlign, myPanel.getHeight() - Settings.MINI_MAP_HEIGHT + Settings.MINI_MAP_HEIGHT / 2);
			
			// Tell the player how to exit the game
			AttributedString abortMsg = new AttributedString("Press ESC to abort");
			abortMsg.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			g2dPanel.drawString(abortMsg.getIterator(), xAlign,  myPanel.getHeight() - 160);
			
			// Print whether or not the human player is placing a hive
			if(Debugging.printHivePlacement) {
				g2dPanel.drawString("Placing Hive: " + placingHive, xAlign, myPanel.getHeight() - 20);
			}
			
			// Print the number of larvae the human player has
			if(Debugging.printLarvaeCount) {
				g2dPanel.drawString("Larvae Count: " + myPlayer.getNumLarvae() + " / " + Player.MAX_NUM_LARVAE, xAlign, myPanel.getHeight() - 50);
			}
		}
	}
	
	/*
	 * Drawing helper methods
	 */
	
	private void drawElapsedTime(Graphics2D g2d, float x, float y) {
		
		int totalSecsElapsed = (int) matchTimer.secsElapsed();

		int minsElapsed = totalSecsElapsed / 60;
		int secsElapsed = totalSecsElapsed % 60;
		
		g2d.setColor(Color.BLACK);
		g2d.drawString("mins: " + minsElapsed, x, y);
		g2d.drawString("secs: " + secsElapsed, x, y + 15);
	}
	
	private void drawSelectionBox(Graphics2D g2d) {
		
		// Draw the rectangular selection box
		if(leftMousePressed && !startedInMiniMap && !startedInHiveCreation) {
			
			Pt topLeft = new Pt();
			topLeft.setX(Math.min(mouseGlobal.x(), startSelect.x()));
			topLeft.setY(Math.min(mouseGlobal.y(), startSelect.y()));
			
			int width = (int) Math.abs(mouseGlobal.x() - startSelect.x());
			int height = (int) Math.abs(mouseGlobal.y() - startSelect.y());
			
			g2d.setColor(Color.GREEN);
			Stroke original = g2d.getStroke();
			int dashOneLength = 3;
			int dashInterval = 5;
			int dashTwoLength = dashOneLength * 5;
			g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{dashOneLength, dashInterval, dashTwoLength, dashInterval}, 0));
			g2d.drawRect(topLeft.intX(), topLeft.intY(), width, height);
			g2d.setStroke(original);
		}
	}
	
	/*
	 * Movement and positioning helper methods
	 */
	
	public void updateGlobalMouse() {
		
		// Update the global mouse position
		mouseGlobal.set(mouseLocal.x() - globalOffset.x(), mouseLocal.y() - globalOffset.y());
	}
	
	// Use the movement margins to move the view window
	public void moveView() {
		
		// Init the screen movement timer
		if(screenMovementTimer == null) {
			screenMovementTimer = new Timer();
		}
		
		// The x and y displacement of the margin
		int deltaX = 0; 
		int deltaY = 0;
		
		GamePanel gp = GamePanel.getInstance();
		int marWidth = Settings.MOVEMENT_MARGIN_WIDTH;
		float moveDist = Settings.SCREEN_MOVEMENT_SPEED * screenMovementTimer.secsElapsed();
		screenMovementTimer.restart();
		
		// Top margin
		if(mouseLocal.y() < marWidth || KeyTracker.isPressed(KeyEvent.VK_UP)) {
			deltaY += moveDist;
		}
		
		// Bottom margin
		if(gp.getHeight() - marWidth < mouseLocal.y() || KeyTracker.isPressed(KeyEvent.VK_DOWN)) {
			deltaY -= moveDist;
		}
		
		// Left margin
		if(mouseLocal.x() < marWidth || KeyTracker.isPressed(KeyEvent.VK_LEFT)) {
			deltaX += moveDist;
		}
		
		// Right margin
		if(gp.getWidth() - marWidth < mouseLocal.x() || KeyTracker.isPressed(KeyEvent.VK_RIGHT)) {
			deltaX -= moveDist;
		}
		
		// Change the location of the j-panel
		globalOffset.add(new Pt(deltaX, deltaY));
		
		// Limit the position of the global offset
		GamePanel myPanel = GamePanel.getInstance();
		globalOffset.constrain(-Settings.FIELD_WIDTH + myPanel.getWidth(), -Settings.FIELD_HEIGHT - Settings.MINI_MAP_HEIGHT + myPanel.getHeight(), 0, 0);
	}
	
	// Centre the camera to the given pt on the field
	public void centreCamOnPt(Pt p) {
		GamePanel myPanel = GamePanel.getInstance();
		globalOffset.set(-(p.x() - myPanel.getWidth() / 2), -(p.y() - myPanel.getHeight() / 2));
		globalOffset.constrain(-Settings.FIELD_WIDTH + myPanel.getWidth(), -Settings.FIELD_HEIGHT - Settings.MINI_MAP_HEIGHT + myPanel.getHeight(), 0, 0);
	}
	
	// Precondition: (x, y) is contained in the mini-map rect
	public Pt getFieldPtFromMiniMapClick(int x, int y) {
		
		// Get the field position of the mini-map click
		Pt miniMapPos = new Pt(x - miniMapRect.x, y - miniMapRect.y);
		Pt fieldPos = new Pt();
		fieldPos.setX(Settings.FIELD_WIDTH * miniMapPos.x() / Settings.MINI_MAP_WIDTH);
		fieldPos.setY(Settings.FIELD_HEIGHT * miniMapPos.y() / Settings.MINI_MAP_HEIGHT);
		
		return fieldPos;
	}
	
	/*
	 * Key Handling
	 */
	
	public void keyPressed(KeyEvent e) {
		
		int keyCode = e.getKeyCode();
		KeyTracker.press(keyCode);
		
		// Check for control group access
		if(KeyEvent.VK_0 <= keyCode && keyCode <= KeyEvent.VK_9) {
			int index = keyCode - KeyEvent.VK_0;
			ControlGroup.accessGroup(index);
		}
		
		switch (keyCode) {
		
		// For debugging:
		
		case KeyEvent.VK_F:
			
			if(KeyTracker.isPressed(KeyEvent.VK_SHIFT)) {
				
				myPlayer.spawnHive(mouseGlobal.x(), mouseGlobal.y(), false);
			}
			else {
				myPlayer.spawnLarvae(mouseGlobal.x(), mouseGlobal.y());
			}
			break;
		
		case KeyEvent.VK_E:
			
			if(KeyTracker.isPressed(KeyEvent.VK_SHIFT)) {
				
				ai1.spawnHive(mouseGlobal.x(), mouseGlobal.y(), false);
			}
			else {
				ai1.spawnLarvae(mouseGlobal.x(), mouseGlobal.y());
			}
			break;
		
		// For actual use
		case KeyEvent.VK_A:
			
			myPlayer.selectMyEntities();
			break;

		case KeyEvent.VK_D:
			
			myPlayer.deselectMyEntities();
			break;
			
		case KeyEvent.VK_H:
			
			if(myPlayer.getNumLarvaeSelected() >= Pod.SIZE) {
				
				placingHive = !placingHive;
				
				// TODO
				// placeHive = !placeHive;
				// Show a mock hive to allow the player to place it somewhere
				// The colour of the mock hive indicates whether or not it is in a valid location
				// Run those larvae towards the mock hive location
				// once they all get there, create a hive at the location (if it is still a valid location)
			}
			
			break;
			
		case KeyEvent.VK_ESCAPE:
			
			// TODO: use escape to escape from hive creation, or other actions
			
			// NB: returning 0 indicates a normal exit (any other number indicates an error)
			GamePanel.getInstance().changeToScreen(new MenuState());
			break;
		
		default:
			break;
		}	
	}
	
	public void keyReleased(KeyEvent e) {
	
		int keyCode = e.getKeyCode();
		KeyTracker.release(keyCode);
	}
	
	/*
	 * Mouse Handling
	 */
	
	public void mousePressed(MouseEvent e) {
		
		updateGlobalMouse();
		
		int mX = e.getX();
		int mY = e.getY();
		
		if(SwingUtilities.isLeftMouseButton(e)) {
			
			leftMousePressed = true;
			
			// Check for HUD panel presses
			if(hudPanel.contains(mX, mY) && showMiniMap) {
				
				if(miniMapRect.contains(mX, mY)) {
					
					startedInMiniMap = true;
					
					// Centre the camera to the field position of the mini-map click
					centreCamOnPt(getFieldPtFromMiniMapClick(mX, mY));
				}
				
				return;
			}
			
			// Check for Hive creation
			if(placingHive) {
				
				// TODO: check if the placement location is valid
				
				// Create a larvae pod out of the selected larvae 
				myPlayer.spawnPod(mouseGlobal, myPlayer.getSelectedLarvae());
				
				placingHive = false;
				startedInHiveCreation = true;
				
				return;
			}
			
			// Default
			
			// Save the start point of the selection rectangle
			startSelect.set(mouseGlobal);
			
			startedInMiniMap = false;
			startedInHiveCreation = false;
			
		}
		else if(SwingUtilities.isRightMouseButton(e)) {
			
			if(!miniMapRect.contains(mX, mY) || !showMiniMap) {
				myPlayer.handleRightClick(mouseGlobal.x(), mouseGlobal.y());
			}
			else {
				Pt fieldPos = getFieldPtFromMiniMapClick(mX, mY);
				myPlayer.handleRightClick(fieldPos.x(), fieldPos.y());
			}
		}	
	}
	
	public void mouseReleased(MouseEvent e) {
		
		updateGlobalMouse();
		
		if(SwingUtilities.isLeftMouseButton(e)) {
			
			leftMousePressed = false;
			
			// Perform a selection
			if(!startedInMiniMap && !startedInHiveCreation) {
				myPlayer.performSelection(startSelect, mouseGlobal, KeyTracker.isPressed(KeyEvent.VK_SHIFT));
			}
		}	
	}
	
	public void mouseDragged(MouseEvent e) {
		
		mouseLocal.set(e.getX(), e.getY());
	}
	
	public void mouseMoved(MouseEvent e) {
		
		mouseLocal.set(e.getX(), e.getY());
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
