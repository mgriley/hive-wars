/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package States;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import Foundation.Game;
import Foundation.GamePanel;

public class MenuState extends State {
	
	public MenuState() {
		super("Menu State");
	}
	
	// Triggered when the player enters a game
	private void enterGameplay() {
		
		/*
		 * TODO:
		 * read the selected options/parameters that the player selects from the menus
		 * pass these options into the constructor of GameState
		 * ^ that way, the game is created with specified players, field size, options, etc.
		 * 
		 * Option examples:
		 * 
		 * AI or wifi connection
		 * Field size
		 * Time limit
		 * Player name
		 */
		
		GameState newGame = new GameState();
		GamePanel.getInstance().changeToScreen(newGame);
	}
	
	@Override
	public void update() {
	
	}

	@Override
	public void draw(Graphics g) {
		
		Graphics2D g2d = (Graphics2D) g;
		
		GamePanel panel = GamePanel.getInstance();
		int width = panel.getWidth();
		int height = panel.getHeight();
		
		// Draw the background
		g2d.setColor(Color.DARK_GRAY);
		g2d.fillRect(0, 0, width, height);
		
		// Draw the screen title

		//g2d.setFont(g2d.getFont().deriveFont(30.0f));
		g2d.setFont(g2d.getFont().deriveFont(60.0f));
		g2d.setColor(Color.WHITE);
		g2d.drawString("Hive Wars", width / 2 - 130, height / 4);
		
		g2d.setFont(g2d.getFont().deriveFont(30.0f));
		g2d.drawString("Challenge foo AIs: SPACE BAR", width/ 2 - 200, height - 400);
		g2d.drawString("(Please see \"Hive Wars Instructions\" in the .zip file for guidance.)", 30, height - 100);
		g2d.drawString("Exit Game: ESC", 30, height - 30);

		// Draw a series of buttons / key commands 
	}

	@Override
	public void enter() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
		int keyCode = e.getKeyCode();
		
		// Return to the menu
		if(keyCode == KeyEvent.VK_SPACE) {
			
			enterGameplay();
		}
		else if(keyCode == KeyEvent.VK_ESCAPE) {
			
			Game.getInstance().exit();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
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