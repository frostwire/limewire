package net.sf.fmj.gui.controlpanel;

import javax.swing.ImageIcon;

/**
 * Provides icons to {@link SwingLookControlPanel}.
 * @author Ken Larson
 *
 */
public interface Skin
{
	public ImageIcon getPlayIcon();
	public ImageIcon getPauseIcon();
	public ImageIcon getStopIcon();
	public ImageIcon getRewindIcon();
	public ImageIcon getFastForwardIcon();
	public ImageIcon getStepForwardIcon();
	public ImageIcon getStepBackwardIcon();
	public ImageIcon getMuteOnIcon();
	public ImageIcon getMuteOffIcon();
}
