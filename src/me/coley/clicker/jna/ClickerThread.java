package me.coley.clicker.jna;

import java.math.BigDecimal;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import me.coley.clicker.Values;
import me.coley.clicker.ui.MainGUI;
import me.coley.clicker.util.NumberUtil;
import me.coley.simplejna.Mouse;
import me.coley.simplejna.Windows;
import me.coley.clicker.jna.SleepStats;

public class ClickerThread extends Thread {
	public static final Logger log = Logger.getLogger("Clicker-Init");
	private final Random r = new Random();
	private final MainGUI gui;
	private long currentTime = 0;

	public ClickerThread(MainGUI clicker) {
		this.gui = clicker;
	}

	@Override
	public void run() {
		while (gui.clicker.getStatus()) {
			try {
				SleepStats stats = gui.clicker.getGaussianSleep();
				if (!stats.valid()) {
					continue;
				}
				//gui.log.log(Level.INFO, "sleep: "+stats.milliseconds+", nanosleep: "+stats.nanoseconds);
				Thread.sleep(stats.milliseconds, stats.nanoseconds);
				gui.clicker.currentTime += stats.milliseconds * 1000000;
				gui.clicker.currentTime += stats.nanoseconds;
				Mouse.mouseLeftClick(-1, -1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Checks if a target window needs to be active.
	 * 
	 * @return
	 */
	private boolean canClick() {
		String target = gui.clicker.getTarget();
		if (gui.settings.getBooleanSetting(Values.SET_WINDOW_TARGET).getCurrent() && target != null) {
			if (Windows.getCurrentWindowTitle().equals(target)) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
}
