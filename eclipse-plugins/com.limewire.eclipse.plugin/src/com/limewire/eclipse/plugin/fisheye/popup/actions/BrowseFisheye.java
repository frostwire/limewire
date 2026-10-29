package com.limewire.eclipse.plugin.fisheye.popup.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.limewire.eclipse.plugin.LimeWirePlugin;

public class BrowseFisheye extends Object implements IObjectActionDelegate {
	
	public BrowseFisheye() {
		super();
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		LimeWirePlugin.openFisheyeMain();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}