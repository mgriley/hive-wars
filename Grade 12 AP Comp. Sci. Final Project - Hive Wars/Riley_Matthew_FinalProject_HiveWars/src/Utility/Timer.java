/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package Utility;

public class Timer {
	
	private long startTime;
	
	public Timer() {
		startTime = System.currentTimeMillis();
	}
	
	public long millisElapsed() {
		return System.currentTimeMillis() - startTime;
	}
	
	public float secsElapsed() {
		return millisElapsed() / 1000.0f;
	}
	
	public void restart() {
		startTime = System.currentTimeMillis();
	}
}
