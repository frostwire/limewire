package com.limewire.eclipse.plugin.fisheye.utils;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;

public class GUIUtils {
	public static void openUrl(String url) {
		try {
			if (WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.EXTERNAL) {
				try {
					IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
					support.getExternalBrowser().openURL(new URL(url));
				} catch (Exception e) {
					MessageDialog.openError(Display.getDefault().getActiveShell(), e.getMessage(), "could not open task url");
				}
			} else {
				IWebBrowser browser = null;
				int flags = 0;
				if (WorkbenchBrowserSupport.getInstance().isInternalWebBrowserAvailable()) {
					flags = WorkbenchBrowserSupport.AS_EDITOR | WorkbenchBrowserSupport.LOCATION_BAR
							| WorkbenchBrowserSupport.NAVIGATION_BAR;

				} else {
					flags = WorkbenchBrowserSupport.AS_EXTERNAL | WorkbenchBrowserSupport.LOCATION_BAR
							| WorkbenchBrowserSupport.NAVIGATION_BAR;
				}
				String title = "Fisheye Browser";
				browser = WorkbenchBrowserSupport.getInstance().createBrowser(flags,title, null, null);
				browser.openURL(new URL(url));
			}
		} catch (PartInitException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Browser init error",
					"Browser could not be initiated");
		} catch (MalformedURLException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "URL not found", "URL Could not be opened. Please check your server configuration in Window->Preferences->FisheyeGlasses");
		}
	}
}
