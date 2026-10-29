package com.limewire.translations;

/**
 * Instances of {@link Progress} that just print to {@link System#out}.
 */
final class TextProgress implements Progress {
	
	private int max;
	private int cur;

	public void inc() {
		cur++;
	}

	public void reset() {
		setMax(0);
	}
	
	public void setMax(int val) {
		this.max = val;
	}

	public void update(String msg, int inc) {
		Util.note(this, (cur += inc) + ":" + msg);
	}

	public void setTitle(String title) {
		// do nothing
	}

	public void close() {
		// do nothing
	}

}
