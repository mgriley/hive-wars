/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package Foundation;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

import States.MenuState;
import Utility.Debugging;
import Utility.Settings;
import Utility.Timer;

public class Game extends JFrame implements Runnable {
	
	private static final long serialVersionUID = 1L;
	
	public boolean running;
	
	private static Game instance;
	private GamePanel myGamePanel;
	
	// For full screen mode
	private GraphicsDevice myDevice;
	private DisplayMode newDisplayMode;
	private DisplayMode oldDisplayMode;
	private BufferStrategy bufferStrategy;
	
	public Game() {
		
		/*
		 * Set up the frame and game panel
		 */
		
		// NB: the game panel starts on the menu screen
		myGamePanel = new GamePanel(new MenuState());
		
		// Set up the frame
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Hive Wars (V4)");
		
		// Revert to absolute positioning (so that the panel can be sized and positioned)
		setLayout(null);
		getContentPane().setLayout(null);
		
		setUndecorated(true);
		setResizable(false);
		
		/*
		 * TODO: for now, all full screen to be toggled (for debugging)
		 * later, always use full screen unless it is unavailable
		 */
		
		if (Settings.FULL_SCREEN) {
			
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			myDevice = ge.getDefaultScreenDevice();
			
			// Attempt to set up full screen mode
			try {
				
				// Attempt to change to full-screen mode
				if (myDevice.isFullScreenSupported()) {
					myDevice.setFullScreenWindow(this);
				}
				else {
					System.out.println("Full screen not supported");
				}
				
				// Attempt to change the display mode
				oldDisplayMode = myDevice.getDisplayMode();
				if (myDevice.isDisplayChangeSupported()) {
					
					// Check all of the display modes
					for (DisplayMode dm : myDevice.getDisplayModes()) {
						System.out.println(dm);
					}
				}
				else {
					System.out.println("Display mode change not supported");
				}
				
				// Disable Event Dispatch Thread calls to the repaint method
				// Instead, render on demand
				this.setIgnoreRepaint(true);
				myGamePanel.setIgnoreRepaint(true);
				
				// Set up double-buffering
				this.createBufferStrategy(2);
				bufferStrategy = this.getBufferStrategy();
			}
			catch (Exception e) {
				
				// Perform clean-up if an error occured
				myDevice.setDisplayMode(oldDisplayMode);
				myDevice.setFullScreenWindow(null);
				
				e.printStackTrace();
			}
		}
		else {
			
			setSize(Settings.FRAME_WIDTH, Settings.FRAME_HEIGHT);
		}
		
		add(myGamePanel);
		
		requestFocus();
		
		setBackground(Color.LIGHT_GRAY);
		
		// Set up the game panel
		
		myGamePanel.setBounds(0, 0, this.getWidth(), this.getHeight());
		myGamePanel.setFocusable(true);
		myGamePanel.requestFocusInWindow();
		
		// Make the frame visible
		setVisible(true);
		
		/*
		 * All done, so start running
		 */
		
		// The game is ready to be run
		running = true;
	}
	
	@Override
	public void run() {
		
		// Variables for tracking / maintaining ups and fps
		
		Timer runTimer = new Timer(); // timer for maintaining 60fps
		Timer secTimer = new Timer(); // timer for printing the fps and ups every 1s
		
		int ticks = 0;
		int frames = 0;
		
		while (running) {
			
			// Update and render at 60 fps
			if (runTimer.secsElapsed() >= 1 / 60.0f) {
				
				tick();
				ticks++;
				
				render();
				frames++;
				
				runTimer.restart();
			}
			
			// Give the system a break so it isn't overloaded
			try {
				Thread.sleep(2);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Print the ups (updates per second) and the fps (frames per second)
			if (secTimer.secsElapsed() > 1) {
				
				if (Debugging.printFpsAndUps) {
					System.out.println(myGamePanel.getStateName() + ": Ticks/s: " + ticks + ", Frames/s: " + frames);
				}
				
				// Reset
				frames = 0;
				ticks = 0;
				secTimer.restart();
			}
		}
	}
	
	public void exit() {
		
		if (Settings.FULL_SCREEN) {
			
			myDevice.setDisplayMode(oldDisplayMode); // return the original display mode
			myDevice.setFullScreenWindow(null); // exit full-screen mode
		}
		
		System.exit(0);
	}
	
	public void tick() {
		
		// Update the game panel
		myGamePanel.update();
	}
	
	public void render() {
		
		if (Settings.FULL_SCREEN) {
			
			/*
			Graphics g = null;
			try {
				
				g = bufferStrategy.getDrawGraphics();
				myGamePanel.render(g);
			}
			finally {
				g.dispose();
			}
			bufferStrategy.show(); // show the graphics
			*/
			
			myGamePanel.repaint();
		}
		else {
			
			// Make a request to repaint the game panel
			myGamePanel.repaint();
		}
	}
	
	public static Game getInstance() {
		
		if (instance == null) {
			instance = new Game();
		}
		return instance;
	}
	
	public static void main(String[] args) {
		
		Game myGame = Game.getInstance();
		
		try {
			new Thread(myGame).run();
		}
		catch (Exception e) {
			
			e.printStackTrace();
		}
		finally {
			
			// Exit the game cleanly in case of error
			myGame.exit();
		}
	}
}
