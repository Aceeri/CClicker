package me.coley.clicker;

import java.util.logging.Level;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import me.coley.clicker.jna.SleepStats;
import me.coley.clicker.jna.StatRecorder;
import me.coley.clicker.ui.MainGUI;
import me.coley.simplejna.hook.mouse.MouseEventReceiver;

/**
 * Handler for the recording mouse inputs.
 * 
 * @author Matt
 *
 */
public class Stats implements Togglable {
	private final MainGUI gui;
	private boolean status;
	private DescriptiveStatistics frequency;
	private StatRecorder statRecorder;
	
	public Stats(MainGUI gui){
		this.gui = gui;
		this.frequency = new DescriptiveStatistics();
	}

	@Override
	public void onEnable() {
		// Start new stats
		MainGUI.log.log(Level.INFO, "Beginning recording of mouse input.");
		
		//frequency = new DescriptiveStatistics();
		MainGUI.log.log(Level.INFO, "Creating keybind-listener...");
		statRecorder = new StatRecorder(gui.mouseHooks, this, gui.graph);
		gui.mouseHooks.hook(statRecorder);
	}

	@Override
	public void onDisable() {
		// Finished, save stats
		MainGUI.log.log(Level.INFO, "Finished recording.");
		if (statRecorder != null) {
			gui.mouseHooks.unhook(statRecorder);
		}
		
		gui.onRecordingFinished();
	}
	
	public void generateData(int amount) {
		for (int i = 0; i < amount; i++) {
			SleepStats stats = gui.clicker.getGaussianSleep();
			if (!stats.valid()) {
				continue;
			}
			gui.graph.addValue((int) stats.milliseconds);
		}
	}

	/**
	 * Retrieve the statistics about click frequency. Measured in milliseconds.
	 * 
	 * @return
	 */
	public DescriptiveStatistics getFrequencyData() {
		return frequency;
	}

	@Override
	public void setStatus(boolean value) {
		this.status = value;
	}

	@Override
	public boolean getStatus() {
		return status;
	}
}
