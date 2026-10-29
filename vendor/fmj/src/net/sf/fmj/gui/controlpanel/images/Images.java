package net.sf.fmj.gui.controlpanel.images;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import net.sf.fmj.utility.LoggerSingleton;

/**
 * 
 * @author Warren Bloomer
 *
 */
public final class Images {
	
	private static final Logger logger = LoggerSingleton.logger;
	
	public static final String SLIDER_THUMB_HORIZ = "slider_thumb_horiz.png";
	public static final String SLIDER_THUMB_VERT = "slider_thumb_vert.png";

	public static final String MEDIA_PLAY = "Play24.gif";
	public static final String MEDIA_STOP = "Stop24.gif";
	public static final String MEDIA_PAUSE = "Pause24.gif";
	public static final String MEDIA_REWIND = "Rewind24.gif";
	public static final String MEDIA_FASTFORWARD = "FastForward24.gif";
	public static final String MEDIA_STEPBACK = "StepBack24.gif";
	public static final String MEDIA_STEPFORWARD = "StepForward24.gif";
	public static final String MUTE_OFF = "Volume24.gif";
	public static final String MUTE_ON = "VolumeOff24.gif";
	
	public static ImageIcon get(String name) {
		return singleton.doGet(name);
	}
	
	public static void flush() {
		singleton.doFlush();
	}
	
	private static final Images singleton = new Images();
	
	private static final String basePath = '/' + Images.class.getPackage().getName().replace('.', '/') + '/';
	
	public static void main(String[] args)
	{	System.out.println("" + basePath);
	}
	
	private HashMap images = new HashMap();
	
	private void doFlush() {
		images.clear();
	}

	private ImageIcon doGet(String imageName) {
		ImageIcon icon = (ImageIcon) images.get(imageName);
		if (icon == null) {
			
			icon = new ImageIcon(getClass().getResource(basePath + imageName));
			if (icon != null) {
				images.put(imageName, icon);
			}
			else
			{	logger.warning("Unable to load icon: " + basePath + imageName);	// can this actually happen?
			}
		}

		return icon;
	}

}
