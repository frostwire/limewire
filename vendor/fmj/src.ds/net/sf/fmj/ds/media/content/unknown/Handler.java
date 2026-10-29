package net.sf.fmj.ds.media.content.unknown;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import net.sf.fmj.utility.ExceptionUtils;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.URLUtils;
import net.sf.jdshow.Com;
import net.sf.jdshow.ComException;
import net.sf.jdshow.IBasicAudio;
import net.sf.jdshow.IGraphBuilder;
import net.sf.jdshow.IMediaControl;
import net.sf.jdshow.IMediaSeeking;
import net.sf.jdshow.IVideoWindow;
import net.sf.jdshow.WindowedRendering;

import com.sun.jna.Native;

/**
 * 
 * @author Ken Larson
 *
 */
public class Handler extends AbstractPlayer
{
	/**
	 * ExecutorService for all native calls.  This ensures that all native methods are called on the same thread.
	 */
	private static final ExecutorService executorService = ExecutorServiceManager.getExecutorService();

	private static final Logger logger = LoggerSingleton.logger;

	private boolean prefetchNeeded = true;
	
	private static final boolean TRACE = true;
	
	/**Width divided by height.  Can only be set from ExecutorService.**/
	private volatile double aspectRatio;


	//Only safe to use in executor service thread!
	private IGraphBuilder graphBuilder;
	//Only safe to use in executor service thread!
	private IMediaControl mediaControl;
	//Only safe to use in executor service thread!
	private IMediaSeeking mediaSeeking;
	//Only safe to use in executor service thread!
	private volatile IBasicAudio basicAudio;
	
	private final ComponentListener myComponentListener = new MyComponentListener();
	
	
    public void setSource(final DataSource source) throws IncompatibleSourceException
	{
		Future future = executorService.submit(new Callable<Void>() {
			public Void call()throws Exception {
				if (TRACE)
					logger.fine("DataSource: " + source);

				if (!source.getLocator().getProtocol().equals("file"))
					throw new IncompatibleSourceException(
							"Only file URLs supported: " + source);

				// TODO: only handling file URLs right now.
				String path = URLUtils.extractValidPathFromFileUrl(source
						.getLocator().toExternalForm());
				if (path == null)
					throw new IncompatibleSourceException(
							"Unable to extract valid file path from URL: "
									+ source.getLocator().toExternalForm());

				try {
					// because Java thinks that /C: is a valid way to start a
					// path on Windows, we have to turn it
					// into something more normal.
					path = new File(path).getCanonicalPath();
				} catch (IOException e1) {
					final String msg = "Unable to get canonical path from "
							+ path + ": " + e1;
					logger.log(Level.WARNING, msg, e1);
					throw new IncompatibleSourceException(e1.toString() + " \n" + ExceptionUtils.getStackTrace(e1));
				}

				logger.info("Path: " + path);

				try {
					System.loadLibrary("jdshow");
				} catch (Throwable e) {
					logger.log(Level.WARNING, "" + e, e);
					throw new IncompatibleSourceException(e.toString() + " \n" + ExceptionUtils.getStackTrace(e));
				}

				try {
					Com.CoInitialize();

					int hr;

					long[] p = new long[1];
					hr = Com.CoCreateInstance(Com.CLSID_FilterGraph, 0L,
							Com.CLSCTX_ALL, Com.IID_IGraphBuilder, p);
					if (Com.FAILED(hr))
						throw new ComException(hr);

					graphBuilder = new IGraphBuilder(p[0]);

					hr = graphBuilder.RenderFile(path, "");
					if (Com.FAILED(hr))
						throw new ComException(hr);

					hr = graphBuilder.QueryInterface(Com.IID_IMediaControl, p, "IID_IMediaControl");
					if (Com.FAILED(hr))
						throw new ComException(hr);
					mediaControl = new IMediaControl(p[0]);

					hr = graphBuilder.QueryInterface(Com.IID_IMediaSeeking, p, "IID_IMediaSeeking");
					if (Com.FAILED(hr))
						throw new ComException(hr);
					mediaSeeking = new IMediaSeeking(p[0]);

					hr = graphBuilder.QueryInterface(Com.IID_IBasicAudio, p, "IID_IBasicAudio");
					if (Com.FAILED(hr)){
						logger.log(Level.WARNING, "iBasicAudio instantiation failed", new ComException(hr));
						//no need to throw an exception.  some files just can't handle audio controls.
						//throw new ComException(hr);
					} else {
						basicAudio = new IBasicAudio(p[0]);
						long[] vol = new long[1];
						hr = basicAudio.get_Volume(vol);
						if (Com.FAILED(hr)){
							logger.log(Level.WARNING, "Audio controlls not supported for file " + new File(path).getName(), new ComException(hr));
							basicAudio.Release();
							basicAudio = null;
						}
						
					}

					hr = graphBuilder.QueryInterface(Com.IID_IVideoWindow, p, "IID_IVideoWindow");
					if (Com.FAILED(hr)) {
						Handler.super.setSource(source);
						return null;
					}

					// determine video size:
					final IVideoWindow videoWindow = new IVideoWindow(p[0]);

					{
						long[] width = new long[1];
						hr = videoWindow.get_Width(width);
						if (Com.FAILED(hr)) {
							Handler.super.setSource(source);
							return null;
						}
						// logger.fine("width: " + width[0]);

						long[] height = new long[1];
						hr = videoWindow.get_Height(height);
						if (Com.FAILED(hr)) {
							Handler.super.setSource(source);
							return null;
						}
						// logger.fine("height: " + height[0]);

						videoSize = new Dimension((int) width[0],
								(int) height[0]);
						
						//TODO: a zero width or height is probably not going to be playable.  should throw an exception?
						//make sure we don't divide by zero or end up with a 0 aspect ratio
						if (width[0] == 0 || height[0] == 0){
							aspectRatio = 1;
						} else {
							aspectRatio = (double) width[0] / (double) height[0];
						}
					}

					videoWindow.Release();
					

				} catch (ComException e) {
					logger.log(Level.WARNING, "" + e, e);
					throw new IncompatibleSourceException(e.toString() + " \n" + ExceptionUtils.getStackTrace(e));
				}

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
				throw new IncompatibleSourceException(e.toString());
			}
		}

		if (basicAudio != null) {
			setGainControl(new DSGainControl());
		}
	}

	//@Override
	public void doPlayerClose()
	{
		// TODO
		logger.info("Handler.doPlayerClose");
	}

	//@Override
	public boolean doPlayerDeallocate()
	{
		// normally, this should only be called once, and visualComponent cannot be null.
		// but in the case where the initialization is canceled, the visualComponent may never have been created.
		if (visualComponent !=null) {
			visualComponent.removeComponentListener(myComponentListener);
			visualComponent = null;
		}
		Future future = executorService.submit(new Runnable(){
			public void run() {
				graphBuilder.Abort();
				logger.info("Handler.doPlayerDeallocate");
				int result = mediaControl.Stop();
				if (Com.FAILED(result)){
					logger.warning("mediaControl.Stop failed");
				} else {
					logger.info("mediaControl stopped");
				}
				
				long remaining = mediaControl.Release();
				mediaControl = null;
				logger.info("mediaControl released: " + remaining);
				remaining = graphBuilder.Release();
				graphBuilder = null;
				logger.info("graphBuilder released: " + remaining);
				remaining = mediaSeeking.Release();
				mediaSeeking = null;
				logger.info("mediaSeeking released: " + remaining);
				if (basicAudio != null) {
					remaining = basicAudio.Release();
					basicAudio = null;
					logger.info("basicAudio released: " + remaining);
				} else {
					logger.info("no basicAudio to release");
				}
				
				Com.CoUninitialize();
				logger.info("Com coUninitialized");
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
		if( ! prefetchNeeded ) return true;
		 
		prefetchNeeded = false;
 
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
				//logger.info("Handler.doPlayerSetMediaTime" + t);
				try {
					long[] current = new long[1];
					long[] stop = new long[1];

					int hr = mediaSeeking.GetPositions(current, stop);
					if (Com.FAILED(hr))
						throw new ComException(hr);

					current[0] = t.getNanoseconds() / 100; // TODO: this assumes
															// REFERENCE_TIME
															// format is being
															// used.

					hr = mediaSeeking.SetPositions(current,
							IMediaSeeking.AM_SEEKING_AbsolutePositioning, stop,
							IMediaSeeking.AM_SEEKING_NoPositioning);
					if (Com.FAILED(hr))
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
		
		try
		{
			int hr = mediaSeeking.SetRate(rate);
			if (Com.FAILED(hr))
				throw new ComException(hr);
		}
		catch (ComException e)
		{
			logger.log(Level.WARNING, "" + e, e);
			return getRate();	// TODO: what to return?
		}
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
		Future future = executorService.submit(new Runnable(){
			public void run() {
		logger.info("Handler.doPlayerStop");

		mediaControl.Stop();
			}
		});
		
		try {
			future.get();
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e);
			return false;
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "" + e, e);
			ExceptionUtils.reportOrReturn(e.getCause());
			return false;
		}
		return true;
	}

	//@Override
	public boolean doPlayerSyncStart(final Time t)
	{
		Future<Boolean> future = executorService.submit(new Callable<Boolean>() {					
			public Boolean call() {						
				logger.info("Handler.doPlayerSyncStart" + t);
						
				try {							
					int hr = mediaControl.Run();
							
					if (Com.FAILED(hr))								
						throw new ComException(hr);						
				} catch (ComException e) {							
					logger.log(Level.WARNING, "" + e, e);							
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
	public Time getPlayerDuration()
	{
		Future<Time> future = executorService.submit(new Callable<Time>() {					
			public Time call() {
				if (getState() < Realized) {
					return DURATION_UNKNOWN;
				} else if (mediaSeeking != null) {
					try {
						long[] duration = new long[1];

						int hr = mediaSeeking.GetDuration(duration);
						if (Com.FAILED(hr))
							throw new ComException(hr);

						return new Time(duration[0] * 100); // TODO: this
															// assumes
															// REFERENCE_TIME
															// format is being
															// used.

					} catch (ComException e) {
						logger.log(Level.WARNING, "" + e, e);
						return DURATION_UNKNOWN;
					}
				} else {
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
	public Time getMediaTime()
	{
		Future<Time> future = executorService.submit(new Callable<Time>() {
			public Time call() {
				if (mediaSeeking != null) {
					try {
						long[] current = new long[1];

						int hr = mediaSeeking.GetCurrentPosition(current);
						if (Com.FAILED(hr))
							throw new ComException(hr);

						return new Time(current[0] * 100); // TODO: this assumes
															// REFERENCE_TIME
															// format is being
															// used.

					} catch (ComException e) {
						logger.log(Level.WARNING, "" + e, e);
						return DURATION_UNKNOWN;
					}
				}
				return DURATION_UNKNOWN;
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

	//visualComponent can only be accessed on the EDT
	private MyCanvas visualComponent;
	
	//@Override
	public Component getVisualComponent()
	{
		if(visualComponent == null){
			visualComponent = new MyCanvas();

			visualComponent.addComponentListener(myComponentListener);
		}
		
		return visualComponent;
	}

	private class MyComponentListener implements ComponentListener
	{

		public void componentHidden(ComponentEvent e)
		{
			logger.fine("componentHidden");
		}

		public void componentMoved(ComponentEvent e)
		{
			int x = ComponentEvent.COMPONENT_MOVED;
			logger.fine("componentMoved: " + e.getID());
			bindVisualComponent();
			
		}

		public void componentResized(ComponentEvent e)
		{
			logger.fine("componentResized");
			bindVisualComponent();			
		}

		public void componentShown(ComponentEvent e)
		{
			logger.fine("componentShown");
			bindVisualComponent();
			
		}

	}
	
	private Dimension calculateSize(int maxWidth, int maxHeight){
		if (maxWidth < 2 || maxHeight < 2) {
			// aspect ratio is meaningless when the picture is overly small and
			// we can't go negative getting it exact.
			return new Dimension(2, 2);
		}
		
		double maxRatio = (double)maxWidth/(double)maxHeight;
		int newWidth = maxWidth;
		int newHeight = maxHeight;
		
		if(maxRatio > aspectRatio){
			//width is too large
			newWidth =(int) (aspectRatio * maxHeight);
			if(newWidth > maxWidth){
				//container is not wide enough - recalculate with lower height
				return calculateSize(maxWidth, maxHeight - 5);
			}
		} else {
			//height is too large
			newHeight =(int) (maxWidth / aspectRatio);
			if(newHeight > maxHeight){
				//container is not tall enough - recalculate with lower width
				return calculateSize(maxWidth - 5, maxHeight);				
			}
		}
		return new Dimension(newWidth, newHeight);
	}
	
	//private boolean visualComponentBound = false;
	//can only be set in executor thread
	private volatile Dimension videoSize;
	//only called on EDT
	private boolean bindVisualComponent()
	{
		assert(SwingUtilities.isEventDispatchThread());
		
		if (!visualComponent.isDisplayable()) {
			// avoid java.lang.IllegalStateException: Component must be
			// displayable exception
			return false;
		}
		
		// if (visualComponentBound)
		// return false;
		final long hwnd = Native.getComponentID(visualComponent);
		// logger.fine("HWND: " + hwnd);
		
		final int width = visualComponent.getWidth();
		final int height = visualComponent.getHeight();
		
		final Dimension dimension = calculateSize(width, height);
		
		int x = (width - dimension.width)/2;
		int y = (height - dimension.height)/2;
		//make sure these are not negative!
		final int xPos = (x < 0)? 0 : x;
		final int yPos = (y < 0)? 0 : y;
		
		
		Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
			public Boolean call() {						
				try {							
					final int hr = WindowedRendering.InitWindowedRendering2(hwnd, graphBuilder,											
							xPos, yPos, dimension.width, dimension.height);
					if (Com.FAILED(hr))								
						throw new ComException(hr);			

				} catch (ComException e) {							
					logger.log(Level.WARNING, "" + e, e);							
					return false;						
				}						
				// visualComponentBound = true;						
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
	
	private class MyCanvas extends Canvas
	{
		{ 
			setBackground(Color.BLACK);
		}

		//@Override
		public Dimension getPreferredSize()
		{
			if (videoSize == null)
				return new Dimension(0, 0);
			return videoSize;
			//return new Dimension(400, 300);	// TODO
		}




	}

    public Component getControlPanelComponent() {
        Component c = super.getControlPanelComponent();

        if( c == null ) {
            c = ControlPanelFactorySingleton.getInstance().getControlPanelComponent(this);
            setControlPanelComponent(c);
        }

        return c;
    }
    
    private class DSGainControl extends AbstractGainControl {
    	private static final long DS_MINIMUM_VOLUME = -10000;

		public float getLevel() {
			Future<Float> future = executorService.submit(new Callable<Float>() {
						
				public Float call() throws ComException{							
					long[] volume = new long[1];							
					int hr = basicAudio.get_Volume(volume);							
					if (Com.FAILED(hr)) {	
						throw new ComException(hr);							
					}							
					return dsToJmf(volume[0]);						
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
					int hr = basicAudio.put_Volume(jmfToDS(level));			
					if (Com.FAILED(hr)){	
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
		
		private long jmfToDS(float level){
			//DirectShow uses 100*db
			long dsVolume = (long) (levelToDb(level) * 100);
			//setting the volume to a value less than DS_MINIMUM_VOLUME has no effect
			if(dsVolume < DS_MINIMUM_VOLUME){
				dsVolume = DS_MINIMUM_VOLUME;
			}
			return dsVolume;
		}
		
		private float dsToJmf(long db){
			//DirectShow uses 100*db
			return (float) (dBToLevel((float)db) / 100.0);			
		}
    }

}
