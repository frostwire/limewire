package com.limewire.eclipse.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

public class LimeWireConfigurationListener implements ILaunchConfigurationListener {
    
    public static final List MAIN_TYPES_LIST = Arrays.asList(new Object[] { "org.limewire.ui.swing.Main", 
    		"org.limewire.ui.swing.mainframe.AppFrame" });
    
	public static final String COM_PACKAGE_PREFIX = "com.limegroup";
	
	public static final String ORG_PACKAGE_PREFIX = "org.limewire";
	
	public static final String NEEDS_WORKING_DIRECTORY_MAIN_TYPE = "com.limegroup.gnutella.gui.Main";
	
	public static final String GUI_FOLDER = "gui";
	
	public static final String TESTS_FOLDER = "tests";
	
	public static final String[] PLATFORM_RUNTIME_JARS = new String[] { };
	
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		LimeWirePlugin.log("Configuration added");
		try {
			String mainType = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, (String)null);
			ILaunchConfigurationWorkingCopy copy = configuration.getWorkingCopy();
			if (mainType != null && mainType.length() > 0) { 
				if (MAIN_TYPES_LIST.contains(mainType)) {
					LimeWirePlugin.log("Setting properties for Main class");
					addNativePath(copy);
					addIPArguments(copy);
					addEnableAssertions(copy);
					copy.doSave();
				}
				else if ((mainType.startsWith(COM_PACKAGE_PREFIX) || mainType.startsWith(ORG_PACKAGE_PREFIX)) 
						&& configuration.getAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_CONTAINER, (String)null) != null) {
					LimeWirePlugin.log("Setting properties for unit test");
					addNativePath(copy);
					addIPArguments(copy);
					addEnableAssertions(copy);
					copy.doSave();
				}
			}
			else {
				// launch configuration created on package
				String launchContainer = configuration.getAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_CONTAINER, (String)null);
				if (launchContainer != null && (launchContainer.contains(COM_PACKAGE_PREFIX) || launchContainer.contains(ORG_PACKAGE_PREFIX))) {
					LimeWirePlugin.log("Setting properties for unit test package");
					addIPArguments(copy);
					addEnableAssertions(copy);
					copy.doSave();
				}
			}
		} 
		catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	private void setWorkingDirectory(ILaunchConfigurationWorkingCopy config, String relatitveDirectory) throws CoreException {
		IProject project = getProject(config);
		IFolder folder = project.getFolder(relatitveDirectory);
		String projectPath = folder.getFullPath().makeRelative().toString();
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, 
				"${workspace_loc:" + projectPath + "}");
	}
	
	private void addNativePath(ILaunchConfigurationWorkingCopy config) throws CoreException {
		String nativePath = getNativePath(config);
		
		if (nativePath != null) {
			String javaLibraryPath = "-Djava.library.path=\"" + nativePath + "\"";
			String jnaLibraryPath = "-Djna.library.path=\"" + nativePath + "\"";
			
			appendValue(config, IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, javaLibraryPath);
			appendValue(config, IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, jnaLibraryPath);
		}
	}
	
	private void addEnableAssertions(ILaunchConfigurationWorkingCopy config) throws CoreException {
		appendValue(config, IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-ea -da:ca.odell.glazedlists...");
	}
	
	private void addIPArguments(ILaunchConfigurationWorkingCopy config) throws CoreException {
		appendValue(config, IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Djava.net.preferIPv4stack=true");
		appendValue(config, IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-Djava.net.preferIPv6Addresses=false");
	}
	
	private void appendValue(ILaunchConfigurationWorkingCopy config, String attributeName, String value) throws CoreException {
		String fullValue = config.getAttribute(attributeName, (String)null);
		fullValue = fullValue != null ? fullValue + " " + value : value;
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, fullValue);	
	}
	
	private IProject getProject(ILaunchConfiguration config) throws CoreException {
		String projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			}
		}
		return null;
	}

	
	private IRuntimeClasspathEntry getPlatformLibraryEntry(IProject project, String library) throws CoreException {
		IFile file = project.getFile(getPlatformJarPath() + library);
		if (file.exists()) {
			return JavaRuntime.newArchiveRuntimeClasspathEntry(file);
		}
		return null;
	}
	
 
	private void addPlatformRuntimeJars(ILaunchConfigurationWorkingCopy copy, String[] platformRuntimeJars) throws CoreException {
		List mementos = new ArrayList();
		IProject project = getProject(copy);
		for (int i = 0; i < platformRuntimeJars.length; i++) {
			IRuntimeClasspathEntry entry = getPlatformLibraryEntry(project, platformRuntimeJars[i]);
			if (entry != null) {
				mementos.add(entry.getMemento());
			}
		}
		if (!mementos.isEmpty()) {
			List finalList = new ArrayList();
			// compute default classpath from configuration and add it, otherwise it's not there
			IRuntimeClasspathEntry[] runtimeClasspath = JavaRuntime.computeUnresolvedRuntimeClasspath(copy);
			for (int i = 0; i < runtimeClasspath.length; i++) {
				finalList.add(runtimeClasspath[i].getMemento());
			}
			finalList.addAll(mementos);
			copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			copy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, finalList);
		}
	}
	
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		
	}

	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
	}
	
	private String getNativePath(ILaunchConfigurationWorkingCopy config) throws CoreException {
	    IProject project = getProject(config);
        IFolder folder = project.getFolder("lib");
        String projectPath = folder.getFullPath().makeRelative().toString();
        String root = "${workspace_loc:" + projectPath + "/native/";
		if (isLinux()) {
			root += "linux";
		}
		else if (isMacOSX()) {
			root += "osx";
		}
		else if (isWindows()) {
			root += "windows";
		} else {
		    return null;
		}
		
		return root + "}";
	}
	
	/**
	 * @return platform specific jar path with trailing slash
	 */
	private static String getPlatformJarPath() {
		String prefix = "lib/jars/";
		if (isLinux()) {
			return prefix + "linux/";
		}
		else if (isMacOSX()) {
			return prefix + "osx/";
		}
		else if (isWindows()) {
			return prefix + "windows/";
		}
		return null;
	}


	private static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase(Locale.US);
		return os.contains("windows");
	}
	
	private static boolean isLinux() {
		String os = System.getProperty("os.name").toLowerCase(Locale.US);
		return os.contains("linux");
	}
	
	private static boolean isMacOSX() {
		String os = System.getProperty("os.name").toLowerCase(Locale.US);
		if (os.startsWith("mac os")) {
			 return os.endsWith("x");
		}
		return false;
	}
		
}
