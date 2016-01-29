/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package Foundation;
import java.awt.Graphics;

import javax.swing.JPanel;
import States.State;

public class GamePanel extends JPanel {

	 //Had to add this to remove an error with the key listener (kind of strange)
	private static final long serialVersionUID = 1L;
	
	private static GamePanel instance;
	
	private State currentState;
	
	public GamePanel(State initialState) {
		
		currentState = initialState;
		currentState.enter();
		addListeningState(currentState);
		
		instance = this;
	}
	
	public static GamePanel getInstance()
	{
		return instance;
	}
	
	public String getStateName() {
		return currentState.getName();
	}
	
	public void changeToScreen(State newScreen) {
		
		// Exit the current screen
		currentState.exit();
		
		// Switch input listeners
		removeListeningState(currentState);
		addListeningState(newScreen);
		
		// Enter the new screen, and change the current screen
		newScreen.enter();
		currentState = newScreen;
	}
	
	private void removeListeningState(State state) {
		
		this.removeKeyListener(state);
		this.removeMouseListener(state);
		this.removeMouseMotionListener(state);
	}
	
	private void addListeningState(State state) {
		
		this.addKeyListener(state);
		this.addMouseListener(state);
		this.addMouseMotionListener(state);
	}
	
	public void update() {
		
		currentState.update();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		render(g);
		g.dispose();
	}
	
	public void render(Graphics g) {
		currentState.draw(g);
	}
}
