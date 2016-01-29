/**
 * Matthew Riley
 * ICS4U-1
 * Final Project - Hive Wars
 * May 27, 2015
 */

package Utility;
import java.awt.Graphics2D;
import java.awt.RenderingHints;


public class Util {

	/*
	 *  Utility methods
	 */

	public static void optimizeGraphics(Graphics2D g2d) {
		
		// Turn on anti-aliasing (reduces roughness on the edge of shapes)
		g2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// Optimize rendering algorithms for QUALITY, SPEED, or DEFAULT
		g2d.setRenderingHint(
				RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
	}

	public static void threadMessage(String message) {
		
		// Display a message, preceded by the name of the current thread
		String threadName = Thread.currentThread().getName();
		System.out.println("Thread: " + threadName + ", Msg: " + message);
	}
}