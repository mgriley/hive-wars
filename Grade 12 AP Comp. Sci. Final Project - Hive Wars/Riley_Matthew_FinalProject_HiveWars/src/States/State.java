/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package States;

import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class State implements KeyListener, ActionListener, FocusListener, MouseListener, MouseMotionListener {
	
	private String stateName;
	
	public State(String argStateName) {
		stateName = argStateName;
	}
	
	public String getName() {
		return stateName;
	}
	
	public abstract void update();
	public abstract void draw(Graphics g);
	
	// Screen transitions
	public abstract void enter();
	public abstract void exit();
}
