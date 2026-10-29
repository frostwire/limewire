package org.limewire.eclipse.gettext;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class GettextPlugin extends Plugin {

	private static final String PLUGIN_ID = "org.limewire.eclipse.gettext";
	
	private static GettextPlugin plugin;

	public static GettextPlugin getDefault() {
		return plugin;
	}

	public void start(BundleContext context) throws Exception {
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	/**
	 * Convenience method for logging statuses to the plug-in log
	 * 
	 * @param status
	 *            the status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Convenience method for logging exceptions to the plug-in log
	 * 
	 * @param e
	 *            the exception to log
	 */
	public static void log(Throwable e) {
		log(toStatus(e));
	}

	public static IStatus toStatus(Throwable e) {
		String message = e.getMessage();
		if (e.getMessage() == null) {
			message = e.getClass().toString();
		}
		return new Status(Status.ERROR, PLUGIN_ID, 0, message, e);
	}
	
}
