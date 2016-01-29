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
import java.util.List;

import Foundation.GamePanel;
import Players.Player;

public class GameOverState extends State {

	private List<Player> players;
	
	public GameOverState(List<Player> argPlayers) {
		super("Game Over State");
		
		// NB: the players are already sorted in order of descending match rank
		players = argPlayers;
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
		
		
		g2d.setColor(Color.DARK_GRAY);
		g2d.fillRect(0, 0, width, height);
		
		g2d.setFont(g2d.getFont().deriveFont(60.0f));
		g2d.setColor(Color.WHITE);
		g2d.drawString("Game Over", width / 2 - 100, 150);
		
		g2d.setFont(g2d.getFont().deriveFont(20.0f));
		g2d.drawString("Press space to return to the menu", width / 2 - 100,  200);
		
		g2d.setFont(g2d.getFont().deriveFont(20.0f));
		for(int i = 0; i < players.size(); i++) {
			
			Player p = players.get(i); 
			g2d.drawString(p.name() + " ranked " + (i + 1), width / 2 - 100, 300 + i * 50);
		}
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
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
		int keyCode = e.getKeyCode();
		
		// Return to the menu
		if(keyCode == KeyEvent.VK_SPACE) {
			
			MenuState nextState = new MenuState();
			GamePanel.getInstance().changeToScreen(nextState);
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
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
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
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

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
