package me.coley.clicker.jna;

public class SleepStats {
	public long milliseconds;
	public int nanoseconds;
	
	public SleepStats() {}
	
	public SleepStats(long milliseconds, int nanoseconds) {
		this.milliseconds = milliseconds;
		this.nanoseconds = nanoseconds;
	}
	
	public boolean valid() {
		return this.milliseconds != -1 && this.nanoseconds != -1;
	}
}