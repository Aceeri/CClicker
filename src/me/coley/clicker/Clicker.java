package me.coley.clicker;

import java.math.BigDecimal;
import java.util.Random;
import java.util.logging.Level;

import me.coley.clicker.jna.ClickerThread;
import me.coley.clicker.jna.SleepStats;
import me.coley.clicker.ui.MainGUI;

/**
 * Handler for the clicking process.
 * 
 * @author Matt
 *
 */
public class Clicker implements Togglable {
	private final MainGUI gui;
	private boolean status;
	private String target;
	public long currentTime = 0;
	private int mutateTime = 0;
	private int mutateShift = 0;
	private int mutateDeviation = 0;
	private int trance = 0;
	private final Random r = new Random();

	public Clicker(MainGUI gui) {
		this.gui = gui;
	}

	@Override
	public void onEnable() {
		MainGUI.log.log(Level.INFO, "Launching clicker thread...");
		new ClickerThread(gui).start();
	}

	@Override
	public void onDisable() {
		MainGUI.log.log(Level.INFO, "Stopping clicker thread...");
	}

	/**
	 * Sets the clicker's target window.
	 * 
	 * @param target
	 *            window title
	 */
	public void setTargetWindow(String target) {
		this.target = target;
	}

	/**
	 * Returns the title of the target window.
	 * 
	 * @return window title
	 */
	public String getTarget() {
		return target;
	}

	@Override
	public boolean getStatus() {
		return status;
	}

	@Override
	public void setStatus(boolean value) {
		this.status = value;
	}
	
	public SleepStats getGaussianSleep() {
		double deviation = gui.settings.getNumericSetting(Values.SET_DEV_DELAY).getCurrent();
		double average = gui.settings.getNumericSetting(Values.SET_AVG_DELAY).getCurrent();
		int min = gui.settings.getNumericSetting(Values.SET_MIN_DELAY).getCurrent();
		int max = gui.settings.getNumericSetting(Values.SET_MAX_DELAY).getCurrent();
		
		if (trance == 0 && r.nextDouble() > 0.9) {
			trance += r.nextInt(40) + 10;
		} else if (trance == 0 && mutateTime == 0 && r.nextDouble() > 0.95) {
			mutateTime = r.nextInt(50)+25;
			mutateShift = r.nextInt((int) (average*1.25)) - (int) (average * 0.5);
			int range = (int) (deviation*0.8);
			if (range <= 0) {
				mutateDeviation = 0;
			} else {
				mutateDeviation = r.nextInt(range);
			}
			//gui.log.log(Level.INFO, "mutated: "+mutateTime+", mutateShift: "+mutateShift+", dev: "+mutateDeviation);
		} else if (trance == 0 && r.nextDouble() > 0.99) {
			average += Math.abs(r.nextGaussian() * max* 0.5) + (double) max;
			gui.log.log(Level.INFO, "distracted average: "+average);
		}
		
		if (trance > 0) {
			deviation = r.nextDouble()*15.0 + 5.0;
			trance--;
		}
		
		if (mutateTime > 0) {
			deviation += (double) mutateDeviation;
			if (deviation < 0.0) {
				deviation = 0.0;
			}
			
			average += (double) mutateShift;
			if (average < (double) min) {
				average = (double) min;
			}
			mutateTime--;
		}
		
		// Gaussian random sleep. Tends to sleep with times
		// around the mean. Times near the bounds (min/max) are
		// less common.
		long nextTime = System.nanoTime();
		if (currentTime == 0) {
			currentTime = nextTime;
		}
		
		long elapsed = nextTime - currentTime;
		currentTime = System.nanoTime();
		
		int elapsednanos = (int) (elapsed % 1000000);
		long elapsedmillis = elapsed / 1000000;
		
		double gaussian = r.nextGaussian();
		BigDecimal sleep = BigDecimal.valueOf(gaussian)
				.multiply(BigDecimal.valueOf(deviation))
				.add(BigDecimal.valueOf(average))
				.subtract(BigDecimal.valueOf(elapsedmillis));
		BigDecimal nanoseconds = sleep.divideAndRemainder(BigDecimal.ONE)[1]
				.multiply(BigDecimal.valueOf(1000000))
				.subtract(BigDecimal.valueOf(elapsednanos));
		
		BigDecimal million = BigDecimal.valueOf(1000000);
		if (nanoseconds.compareTo(BigDecimal.ZERO) < 0 || nanoseconds.compareTo(million) > 0) {
			BigDecimal divisor = nanoseconds.abs().divide(million).setScale(0, BigDecimal.ROUND_UP);
			sleep = sleep.subtract(divisor);
			nanoseconds = million.subtract(nanoseconds.abs().remainder(million));
		}
		
		//gui.log.log(Level.INFO, "min: "+(long) min);
		BigDecimal minimum = BigDecimal.valueOf((long) min).subtract(BigDecimal.ONE);
		if (sleep.compareTo(minimum) < 0) {
			//sleep = minimum;
			//nanoseconds = BigDecimal.ZERO;
			return new SleepStats(-1, -1);
		}
		
		//gui.log.log(Level.INFO, sleep.longValue()+"ms, "+nanoseconds.intValue()+"ns");
		return new SleepStats(sleep.longValue(), nanoseconds.intValue());
	}
}
