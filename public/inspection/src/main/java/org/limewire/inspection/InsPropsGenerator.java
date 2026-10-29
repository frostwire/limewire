package org.limewire.inspection;

import java.io.File;
import java.util.Map;

import javax.swing.SwingUtilities;

import com.google.inject.Guice;
import com.google.inject.Module;

public class InsPropsGenerator {

    public static void main(String[] args) throws Exception {
    	if(args.length < 2) {
    		System.err.println("Usage: java -cp <classpath> " + InsPropsGenerator.class.getName() + " <scanRoot> <module classname> <ignorepaths...>");
    		System.exit(1);
    	}
    	
    	final File scanRoot = new File(args[0]);
    	final String moduleName = args[1];
    	final String[] ignorePaths = new String[args.length - 2];
    	if(args.length > 2)
    		System.arraycopy(args, 2, ignorePaths, 0, args.length-2);
    	
    	// Do this in the Swing thread in case any modules require the EDT.
    	SwingUtilities.invokeAndWait(new Runnable() {
    	    @Override
    	    public void run() {
    	        try {
        	        Class<?> moduleClass = Class.forName(moduleName);
        	        Module module = (Module)moduleClass.newInstance();
        	        
        	        Map<String, String> m = InspectionTool.generateMappings(scanRoot, Guice.createInjector(module), ignorePaths);
        	        for (String s : m.keySet())
        	            System.out.println(s+"="+m.get(s));
    	        } catch(Throwable t) {
    	            throw new RuntimeException(t);
    	        }
    	    }
    	});
    }
}
