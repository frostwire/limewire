package net.sf.jdshow;

import java.util.logging.Logger;

import net.sf.fmj.utility.LoggerSingleton;

/**
 * Helper to set up windowed rendering.
 * @author Ken Larson
 *
 */
public class WindowedRendering
{
	private static final Logger logger = LoggerSingleton.logger;;
	
	/**
	 * Pure native.
	 */
	public static native int InitWindowedRendering( 
		    long hwndApp,                  // Window to hold the video. 
		    long /*IGraphBuilder**/ pGraph         // Pointer to the Filter Graph Manager. 
		    );   

	public static int InitWindowedRendering2( 
		    long hwndApp,                  // Window to hold the video. 
		    IGraphBuilder /*IGraphBuilder**/ pGraph,         // Pointer to the Filter Graph Manager. 
		    int x, int y, 
		    int width, int height
		)
	{
		int hr;
		final long[] p = new long[1];
		
		hr = pGraph.QueryInterface(Com.IID_IVideoWindow, p, "IID_IVideoWindow");
		if (Com.FAILED(hr)){
			logger.warning("IVideoWindow QueryInterface failed");
			return hr;
		}
		
		final IVideoWindow videoWindow = new IVideoWindow(p[0]);

		hr = videoWindow.put_Owner(hwndApp);
		if (Com.FAILED(hr)){
			logger.warning("IVideoWindow put_Owner failed");
			videoWindow.Release(); 
			return hr;
		}
		 
		hr = videoWindow.put_WindowStyle(Com.WS_CHILD | Com.WS_CLIPSIBLINGS);
		if (Com.FAILED(hr)){
			logger.warning("IVideoWindow put_WindowStyle failed");
			videoWindow.Release(); 
			return hr;
		}
		 
		hr = videoWindow.put_MessageDrain(hwndApp);
		if (Com.FAILED(hr)){
			logger.warning("IVideoWindow put_MessageDrain failed");
			videoWindow.Release(); 
			return hr;
		}
		
		hr = videoWindow.setWindowPosition(x, y, width, height);
		if (Com.FAILED(hr)){
			logger.warning("IVideoWindow setWindowPosition failed");
			videoWindow.Release(); 
			return hr;
		}


		videoWindow.Release(); 
		
		return Com.S_OK;
	}
}
