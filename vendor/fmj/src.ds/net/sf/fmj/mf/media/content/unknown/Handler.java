package net.sf.fmj.mf.media.content.unknown;


import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.IncompatibleSourceException;
import javax.media.Time;
import javax.media.protocol.DataSource;
import javax.swing.SwingUtilities;

import net.sf.fmj.concurrent.ExecutorServiceManager;
import net.sf.fmj.ejmf.toolkit.media.AbstractPlayer;
import net.sf.fmj.gui.controlpanelfactory.ControlPanelFactorySingleton;
import net.sf.fmj.media.AbstractGainControl;
import net.sf.fmj.mf.media.content.unknown.MFPlayer.MFPlayerListener;
import net.sf.fmj.utility.ExceptionUtils;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.URLUtils;
import net.sf.jdshow.ComException;

import com.sun.jna.Native;

/**
 * 
 * Handler for MediaFoundation video loosely based on Ken Larson's DirectShow Handler
 *
 */
public class Handler extends AbstractPlayer
{
	/**
	 * ExecutorService for all native calls.  This ensures that all native methods are called on the same thread.
	 */
	private static final ExecutorService executorService = ExecutorServiceManager.getExecutorService();

	private static final Logger logger = LoggerSingleton.logger;
	
	private static final boolean TRACE = true;
	
	private volatile String fileUrl;	
		
	private MFPlayer mfPlayer;
	private MFPlayerListener mfPlayerListener = new MediaFoundationListener();
	
	private final Canvas renderer;
	
	private ComponentListener componentListener;
	
	public Handler(Canvas renderer){
		this.renderer = renderer;
	}
	
	
    public void setSource(final DataSource source) throws IncompatibleSourceException
	{
		Future future = executorService.submit(new Callable<Void>() {
			public Void call()throws Exception {
				if (TRACE)
					logger.fine("DataSource: " + source);

				//TODO: this patch checking logic should be shared with the DS handler
				if (!source.getLocator().getProtocol().equals("file"))
					throw new IncompatibleSourceException(
							"Only file URLs supported: " + source);

				String path = URLUtils.extractValidPathFromFileUrl(source.getLocator().toExternalForm());
				if (path == null)
					throw new IncompatibleSourceException(
							"Unable to extract valid file path from URL: "
									+ source.getLocator().toExternalForm());

				try {
					path = new File(path).getCanonicalPath();
				} catch (IOException e1) {
					final String msg = "Unable to get canonical path from "
							+ path + ": " + e1;
					logger.log(Level.WARNING, msg, e1);
					throw new IncompatibleSourceException(e1.toString() + " \n" + ExceptionUtils.getStackTrace(e1));
				}

				logger.info("Path: " + path);
				File file = new File(path);
				if(!file.exists()){
					throw new IncompatibleSourceException("File must exist to be played");
				}
				
				fileUrl = URLUtils.extractValidPathFromFileUrl(URLUtils.createUrlStr(file));
				
				mfPlayer = MFPlayer.getInstance();
			
				long hwnd = 0;
				
				try {
					hwnd = Native.getComponentID(renderer);
				} catch (UnsatisfiedLinkError e) {	
					//this is kind of ugly but it prevents the executor from eating the runtime exception
					throw new IncompatibleSourceException(e.toString() + " \n" + ExceptionUtils.getStackTrace(e));
				}
				
				int result = mfPlayer.synchStartPlayer(fileUrl, hwnd);
				if(failed(result)){
					throw new IncompatibleSourceException("File not MediaFoundation playable: " + fileUrl);
				}
				
				long[] prefSize = new long[2];
				mfPlayer.getNativeSize(prefSize);

				final int width = (int)prefSize[0];
				final int height = (int)prefSize[1];
				
				SwingUtilities.invokeLater(new Runnable() {					
					public void run() {
						if(width != 0 && height != 0){
							renderer.setPreferredSize(new Dimension(width, height));
						}
						componentListener = new MFComponentListener();
						renderer.addComponentListener(componentListener);
					}
				});
				
				mfPlayer.addMFPlayerListener(mfPlayerListener);

				Handler.super.setSource(source);
				return null;
			}
		});
		
		try {
			future.get();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "" + e, e);
			throw new IncompatibleSourceException(e.toString() + " \n" + ExceptionUtils.getStackTrace(e));
		} catch (ExecutionException e) {
			if (e.getCause() instanceof IncompatibleSourceException) {
				throw (IncompatibleSourceException) e.getCause();
			} else {
				logger.log(Level.WARNING, "" + e, e);
				throw new IncompatibleSourceException(e.toString() + " \n" + ExceptionUtils.getStackTrace(e));
			}
		}

		setGainControl(new MFGainControl());
		postStartEvent();
	}

	//@Override
	public void doPlayerClose()
	{
	}

	//@Override
	public boolean doPlayerDeallocate()
	{
		SwingUtilities.invokeLater(new Runnable() {					
			public void run() {
				if(componentListener != null){
				renderer.removeComponentListener(componentListener);
				}
			}
		});
		Future future = executorService.submit(new Runnable(){
			public void run() {
				mfPlayer.removeMFPlayerListener(mfPlayerListener);
				mfPlayer.killPlayer();
			}			
		});

		try {
			future.get();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e);
			//we still want everything else to be deallocated even if something goes wrong here
			return true;
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e.getCause());
			//we still want everything else to be deallocated even if something goes wrong here
			return true;
		}
		return true;
	}

	//@Override
	public boolean doPlayerPrefetch()
	{ 
		return true;
	}

	//@Override
	public boolean doPlayerRealize()
	{
	    
		return true;
	}
	



	//@Override
	public void doPlayerSetMediaTime(final Time t)
	{
		logger.info("Handler.doPlayerSetMediaTime");
		
		Future future = executorService.submit(new Runnable(){
			public void run() {
				try {
					long mfTime = t.getNanoseconds() / 100; 

					int hr = mfPlayer.setPosition(mfTime);
					if (failed(hr))
						throw new ComException(hr);

				} catch (ComException e) {
					logger.log(Level.WARNING, "" + e, e);
					// TODO: handle
				}
			}
		});
		try {
			future.get();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e);
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e.getCause());
		}
	}

	//@Override
	public float doPlayerSetRate(final float rate)
	{
		Future<Float> future = executorService.submit(new Callable<Float>(){
			public Float call() {
		logger.fine("Handler.doPlayerSetRate " + rate);

			//TODO setRate
		return rate;
			}
		});
		
		try {
			return future.get().floatValue();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e);
			return getRate();
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e.getCause());
			return getRate();
		}
	}

	//@Override
	public boolean doPlayerStop()
	{
		Future<Boolean> future = executorService.submit(new Callable<Boolean>() {					
			public Boolean call() {
				logger.info("Handler.doPlayerStop");
				int hr = mfPlayer.pause();
				if (failed(hr)) {
					return false;
				}
				return true;	
			}
		});
		
		try {
			return future.get();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e);
			return false;
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e.getCause());
			return false;
		}
	}

	//@Override
	public boolean doPlayerSyncStart(final Time t)
	{
		Future<Boolean> future = executorService.submit(new Callable<Boolean>() {					
					public Boolean call() {
						logger.info("Handler.doPlayerSyncStart" + t);

						int hr = mfPlayer.resume();

						if (failed(hr)) {
							return false;
						}
						return true;

					}				
		});
		
		try {
			return future.get();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e);
			return false;
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e.getCause());
			return false;
		}
	}

	// @Override
	public Time getPlayerDuration() {
		Future<Time> future = executorService.submit(new Callable<Time>() {
			public Time call() {
				try {
					long[] duration = new long[1];

					int hr = mfPlayer.getDuration(duration);
					if (failed(hr))
						throw new ComException(hr);

					return new Time(duration[0] * 100);
				} catch (ComException e) {
					logger.log(Level.WARNING, "" + e, e);
					return DURATION_UNKNOWN;
				}
			}
		});

		try {
			return future.get();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e);
			return DURATION_UNKNOWN;
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e.getCause());
			return DURATION_UNKNOWN;
		}
	}

	// @Override
	public Time getMediaTime() {
		Future<Time> future = executorService.submit(new Callable<Time>() {
			public Time call() {
				try {
					long[] position = new long[1];

					int hr = mfPlayer.getPosition(position);
					if (failed(hr))
						throw new ComException(hr);			

					return new Time(position[0] * 100);
				} catch (ComException e) {
					logger.log(Level.WARNING, "" + e, e);
					return DURATION_UNKNOWN;
				}
			}
		});

		try {
			return future.get();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e);
			return DURATION_UNKNOWN;
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e.getCause());
			return DURATION_UNKNOWN;
		}
	}

	// @Override
	public Time getPlayerStartLatency()
	{
		return new Time(0);
	}

	
	//@Override
	public Component getVisualComponent()
	{
		return renderer;
	}
	
    public Component getControlPanelComponent() {
        Component c = super.getControlPanelComponent();

        if( c == null ) {
            c = ControlPanelFactorySingleton.getInstance().getControlPanelComponent(this);
            setControlPanelComponent(c);
        }

        return c;
    }
    
	private boolean failed(int hr) {
		return hr < 0;
	}	
    
    private class MFGainControl extends AbstractGainControl {
    	private static final long DS_MINIMUM_VOLUME = -10000;

		public float getLevel() {
			Future<Float> future = executorService.submit(new Callable<Float>() {
						
				public Float call() throws ComException{							
					float[] vol = new float[1];
					int hr = mfPlayer.getVolume(vol);
					if(failed(hr)){
						throw new ComException(hr);
					}
					return vol[0];
				}
			});
			
			try {
				return future.get();
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, "basicAudio.get_Volume failed", e);
				ExceptionUtils.reportOrReturn(e);
				//TODO: what to return here?
				return 0;
			} catch (ExecutionException e) {
				logger.log(Level.WARNING, "basicAudio.get_Volume failed", e);
				ExceptionUtils.reportOrReturn(e.getCause());
				//TODO: what to return here?
				return 0;
			}
		}

		public float setLevel(final float level) {
			Future<Float> future = executorService.submit(new Callable<Float>() {
				public Float call() throws ComException {			
					int hr = mfPlayer.setVolume(level);	
					if(failed(hr)){
						throw new ComException(hr);
					}
					return level;
				}				
			});
			
			try {
				future.get();
				notifyListenersGainChangeEvent();
				return level;
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, "basicAudio.put_Volume failed", e);
				ExceptionUtils.reportOrReturn(e);
				//TODO: what to return here?
				return level;
			} catch (ExecutionException e) {
				logger.log(Level.WARNING, "basicAudio.put_Volume failed", e);
				ExceptionUtils.reportOrReturn(e.getCause());
				//TODO: what to return here?
				return level;
			}
		}
	
    }
    
    private class MediaFoundationListener implements MFPlayerListener {

		public void statusChanged(final int status) {
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					switch (status) {
					case MFPlayer.ERROR:
						break;
					case MFPlayer.LOADED:
						setState(Prefetched);
						break;
					case MFPlayer.STARTED:
						setState(Started);
						break;
					case MFPlayer.ENDED:
						postEndOfMediaEvent();
						break;
					}
				}
			});
		}    	
    }

	/**
	 * This alerts MFPlayer when the component has moved or resized so that the
	 * proper aspect ratio is maintained.
	 */
    private class MFComponentListener extends ComponentAdapter {
		
		public void componentResized(ComponentEvent e) {
			if (mfPlayer != null){
				mfPlayer.update();
			}
		}
		
		public void componentMoved(ComponentEvent e) {
			if (mfPlayer != null){
				mfPlayer.update();
			}
		}
	}

}
