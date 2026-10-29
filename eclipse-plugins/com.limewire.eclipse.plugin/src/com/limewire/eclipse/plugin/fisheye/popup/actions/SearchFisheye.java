package com.limewire.eclipse.plugin.fisheye.popup.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.limewire.eclipse.plugin.LimeWirePlugin;

public class SearchFisheye implements IObjectActionDelegate {

	public SearchFisheye() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (!(action instanceof ObjectPluginAction)) {
			return;
		}
		
		ObjectPluginAction objectAction = (ObjectPluginAction) action;
		
		if(!(objectAction.getSelection() instanceof StructuredSelection)) {
			LimeWirePlugin.displayError();
			return;
		}
			
		StructuredSelection selection = (StructuredSelection)objectAction.getSelection();
		LimeWirePlugin.searchFisheye(selection);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
