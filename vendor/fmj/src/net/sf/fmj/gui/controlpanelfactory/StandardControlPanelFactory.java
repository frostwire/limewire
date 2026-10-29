package net.sf.fmj.gui.controlpanelfactory;

import java.awt.Component;

import javax.media.Player;

import net.sf.fmj.ejmf.toolkit.gui.controlpanel.StandardControlPanel;

/**
 * {@link ControlPanelFactory} for {@link StandardControlPanel}, which is based on EJMF.
 * @author Ken Larson
 *
 */
public class StandardControlPanelFactory implements ControlPanelFactory
{

	public Component getControlPanelComponent(Player p)
	{
        return new StandardControlPanel(p, StandardControlPanel.USE_START_CONTROL | StandardControlPanel.USE_STOP_CONTROL | StandardControlPanel.USE_PROGRESS_CONTROL);
        
	}

}
