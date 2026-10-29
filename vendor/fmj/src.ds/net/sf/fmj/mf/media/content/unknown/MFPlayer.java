package net.sf.fmj.mf.media.content.unknown;

import java.util.ArrayList;
import java.util.List;

import javax.media.IncompatibleSourceException;

import net.sf.fmj.utility.ExceptionUtils;

//TODO: attach class to IMFPlayer pointer so that each instance has its own player.
class MFPlayer {
	
	static interface MFPlayerListener {
		public void statusChanged(int status);
	}

	//THESE CONSTANTS ARE USED IN NATIVE CODE.  DO NOT RENAME THEM.
	static final int ERROR = -1;
	static final int LOADED = 0;
	static final int STARTED = 1;
	static final int ENDED = 2;
		
	private volatile boolean statusUpdated = false;
	
	private final Object lock = new Object();
	private volatile int lastStatus;
	//Guarded by this
	private final List<MFPlayerListener> listeners = new ArrayList<MFPlayerListener>();
	
	private static MFPlayer instance;
	
	public synchronized static MFPlayer getInstance() throws IncompatibleSourceException {
		if(instance == null){
			
			try {
				System.loadLibrary("JMediaFoundation");
			} catch (Throwable e) {
				throw new IncompatibleSourceException(e.toString() + " \n" + ExceptionUtils.getStackTrace(e));
			}
			
			instance = new MFPlayer();
			try {
				int hr = instance.initialize();
				if(hr < 0){
					instance = null;
					throw new IncompatibleSourceException("MFPlayer native initialization failed");
				}
			} catch (Throwable e) {
				instance = null;
				throw new IncompatibleSourceException(e.toString() + " \n" + ExceptionUtils.getStackTrace(e));
			}
		}
		return instance;
	}	
	
	/**
	 * Private constructor to enforce singleton.
	 */
	private MFPlayer() {}


	/**
	 * Asynchronously starts video playback of the url file in the window hwnd. If a file is
	 * currently playing, the current player will be destroyed.
	 */
	public native int startPlayer(String url, long hWnd);
	


	/**
	 * Synchronously starts video playback of the url file in the window hwnd. If a file is
	 * currently playing, the current player will be destroyed.
	 */
	public int synchStartPlayer(String url, long hWnd) {
		synchronized (lock) {
			statusUpdated = false;
			lastStatus = ERROR;
			int hr = startPlayer(url, hWnd);
			// most unplayable files will be caught by the return value
			if (hr < 0) {
				return hr;
			}

			try {
				// for the remaining ones we wait for the callback
				while (!statusUpdated) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				// do nothing
			}
			
			if (lastStatus == ERROR){
				killPlayer();
				return -1;
			}
			
			return 0;
		}
	}

	public native void killPlayer();
	
	/**
	 * Gets the native size of the video.
	 * 
	 * @param size an array consisting of {width, height} for the video's native size
	 * @return HRESULT
	 */
	public native int getNativeSize(long[] size);

	/**
	 * Calling update ensures that the video is rendered at the proper size.
	 * This is sometimes necessary when a video is first displayed.
	 */
	public native void update();

	public native int pause();

	public native int resume();
	
	/**
	 * 
	 * @param time Position in nanoseconds/100
	 * @return HRESULT
	 */
	public native int setPosition(long time);

	/**
	 * 
	 * @param time pointer to postion time in nanoseconds/100
	 * @return HRESULT
	 */
	public native int getPosition(long[] time);
	/**
	 * 
	 * @param duration pointer to the duration in nanoseconds/100
	 * @return HRESULT
	 */
	public native int getDuration(long[] duration);

	public native int setVolume(float volume);
	
	public native int getVolume(float[] volume);

	private native int initialize();
	
	//DO NOT CHANGE THIS METHOD NAME.  It is called by native code.
	private void statusChanged(int status){
		//synch start might be waiting for a status update - lock and notify
		synchronized (lock) {
			statusUpdated = true;
			lastStatus = status;
			lock.notifyAll();			
		}
		fireStatusChanged(status);
	}
	

	private synchronized void fireStatusChanged(int status) {
		for(MFPlayerListener listener :listeners){
			listener.statusChanged(status);
		}
	}

	public synchronized void addMFPlayerListener(MFPlayerListener listener) {
		listeners.add(listener);
	}
	public synchronized void removeMFPlayerListener(MFPlayerListener listener) {
		listeners.remove(listener);		
	}	
		
}
