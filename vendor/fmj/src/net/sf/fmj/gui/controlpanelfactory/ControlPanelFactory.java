package net.sf.fmj.gui.controlpanelfactory;

import java.awt.Component;

import javax.media.Player;

/**
 * A factory for a control panel component.
 * @author Ken Larson
 *
 */
public interface ControlPanelFactory
{
	public Component getControlPanelComponent(Player p);
}
