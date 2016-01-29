/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package Utility;

// Utility class for checking if a particular key is pressed
public class KeyTracker {
	
	// Make a map of key states
	private static boolean[] pressMap = new boolean[500];
	
	public KeyTracker() {
		
	}
	
	public synchronized static void press(int keyCode) {
		pressMap[keyCode] = true;
	}
	
	public synchronized static void release(int keyCode) {
		pressMap[keyCode] = false;
	}
	
	public synchronized static boolean isPressed(int keyCode) {
		return pressMap[keyCode];
	}
}
