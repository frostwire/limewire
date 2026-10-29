package com.limewire.eclipse.plugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.limewire.eclipse.plugin.fisheye.utils.FisheyePaths;
import com.limewire.eclipse.plugin.fisheye.utils.GUIUtils;

/**
 * The activator class controls the plug-in life cycle
 */
public class LimeWirePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.limewire.eclipse.plugin";

	public static String SERVER_PATH_PREFERENCE_KEY = "SERVER_PATH";
	
	public static String REPOSITORY_NAME_PREFERENCE_KEY = "REPOSITORY_NAME";
	
	// The shared instance
	private static LimeWirePlugin plugin;
	
	/**
	 * The constructor
	 */
	public LimeWirePlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static LimeWirePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static String getServerPath() {
		return plugin.getPreferenceStore().getString(SERVER_PATH_PREFERENCE_KEY);
	}
	
	public static String getRepositoryName() {
		return plugin.getPreferenceStore().getString(REPOSITORY_NAME_PREFERENCE_KEY);
	}
	
	public static void openFisheyeMain() {
		GUIUtils.openUrl(getBrowsePath());
	}
	
	public static void openFisheyeSelection(StructuredSelection selection) {
		Object o = selection.getFirstElement();
		IResource resource = null;
		if(o instanceof IResource) {
			resource = (IResource) o;
		} else if(o instanceof IJavaElement) {
			IJavaElement unit = (IJavaElement) o;
			resource = unit.getResource();
		} else {
			displayError();
			return;
		}
		
		RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject());
		String path = null;
		if(provider != null && provider instanceof CVSTeamProvider) {
			CVSTeamProvider cvs = (CVSTeamProvider) provider;
			CVSWorkspaceRoot root = cvs.getCVSWorkspaceRoot();
			try {
				ICVSResource cvsResource = root.getCVSResourceFor(resource);
				path = cvsResource.getRepositoryRelativePath();
			} catch (CVSException e) {
				e.printStackTrace();
				path = resource.getProjectRelativePath().toString();
			}
		} else {
			path = resource.getProjectRelativePath().toString();
		}
		String fullPath = getBrowsePath() + "/" + path;
		GUIUtils.openUrl(fullPath);
	}
	
	public static void searchFisheye(StructuredSelection selection) {
		Object o = selection.getFirstElement();
		String path = "";
		if(o instanceof IFile) {
			IFile file = (IFile) o;
			path = file.getName();
		} else if(o instanceof IJavaElement) {
			IJavaElement unit = (IJavaElement) o;
			path = unit.getElementName();
		} else {
			return;
		}
		
		String fullPath = getServerPath() + "/"
		+ FisheyePaths.SEARCH_PATH + "/" + getRepositoryName() 
		+ "/?q=" + path;
		
		GUIUtils.openUrl(fullPath);
	}
	
	public static String getBrowsePath() {
		return getServerPath() + "/"
			+ FisheyePaths.BROWSE_PATH + "/" + getRepositoryName();
	}
	
	public static void log(String message) {
		getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
	}
	
	public static void displayError() {
		Shell shell = new Shell();
		MessageDialog.openInformation(
				shell,
				"FisheyeGlasses Plug-in",
				"Invalid selection!");
		return;
	}

}
