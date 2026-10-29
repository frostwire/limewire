package org.limewire.inspection;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.limewire.inject.MoreScopes;
import org.limewire.util.FileUtils;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.ConvertedConstantBinding;
import com.google.inject.spi.ExposedBinding;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.ProviderKeyBinding;
import com.google.inject.spi.UntargettedBinding;

/**
 * Looks for Inspectable annotations in classes, ensuring that 
 * {@link InspectionPoint} annotations are within classes that are
 * reachable by Guice's {@link Injector}, and are declared as being
 * a Singleton.
 * 
 * The idea is this:
 * 
 *  1) Scan through every classfile, looking for fields that are annotated with:
 *   {@link InspectablePrimitive}, {@link InspectableForSize}, or
 *   with {@link InspectionPoint} and also implement {@link Inspectable}.
 *  
 *  2) When we find a field that meets the above conditions, we must verify that
 *  it is contained within a suitable class.  If the container is annotated with
 *  {@link InspectableContainer}, then we check that container's container.  The
 *  ultimate container must be annotated with {@link Singleton}.  Alternatively,
 *  if the field is static we <b>map that inspection point to the field</b> and
 *  move to the next field.
 *   
 *  3) Once we verify that the container is a singleton, we must ensure that Guice
 *  can reliable retrieve a sole reference to that singleton, potentially from an
 *  interface.  To do this, we look at each interface the container implements, as well
 *  as any subclasses it extends.  For each of these, we get the Provider from Guice.
 *  If no provider exists, we <b>fail because there is no way to retrieve that class</b>.
 *  
 *  4) If each Provider.get() returns the exact same reference, we 
 *  <b>map that inspection point to the interface that gave us the Provider, and 
 *  optionally the InspectableContainer it was within</b>.  If atleast one provider returns
 *  something other than what we expect, we <b>fail with an exception</b>.  
 */
public class InspectionTool {
	
	private final Injector injector;	
	
    public InspectionTool(Injector injector) {
		this.injector = injector;
	}

	public static Map<String, String> generateMappings(File base, Injector injector, String[] ignorePaths) {
    	InspectionTool tool = new InspectionTool(injector);
    	return tool.go(base, ignorePaths);
    }
    
    private Map<String, String> go(File base, String[] ignorePaths) {
        Map<String, String> props = new TreeMap<String, String>();
		File[] classFiles = FileUtils.getFilesRecursive(base,
				new String[] { "class" });
		try {
	        for (File f : classFiles) {
	        	if(fileContainsPaths(f.getPath(), ignorePaths))
	        		continue;
	            ClassParser parser = new ClassParser(f.getPath());
	            JavaClass clazz = parser.parse();
	            if (clazz.isInterface() || clazz.isAnnotation())
	                continue;
	            
	            scanClass(clazz, props);
	        }
        } catch(IOException iox) {
        	throw new RuntimeException(iox);
        }
        return props;
    }
    
    private boolean fileContainsPaths(String path, String[] ignorePaths) {
    	for(String s : ignorePaths) {
    		if(path.contains(s))
    			return true;
    	}
    	return false;
    }
    
    private void scanClass(JavaClass clazz, Map<String, String> props) {            
        boolean checkContainer = false;
        List<FieldAndAnnotation> instanceFields = new ArrayList<FieldAndAnnotation>();
        for (Field field : clazz.getFields()) {
            for (AnnotationEntry ae : field.getAnnotationEntries()) {
            	boolean foundField = false;
                if ((ae.getAnnotationType().contains("InspectablePrimitive") || ae.getAnnotationType().contains("InspectableForSize"))) {
                	foundField = true;
                } else if (ae.getAnnotationType().contains("InspectionPoint")) {
                	foundField = true;
                	try {
	                    if(!Repository.implementationOf(field.getType().toString(), Inspectable.class.getName()))
	                    	throw new RuntimeException("InspectionPoint on: " + field + ", but does not implement Inspectable");
                	} catch(ClassNotFoundException cnfe) {
                		throw new RuntimeException(cnfe);
                	}
                }
                
                if(foundField) {
	                if(field.isStatic()) {
	                	map(true, field, null, clazz, ae, props);
	                } else {
	                	instanceFields.add(new FieldAndAnnotation(field, ae));
	                	checkContainer = true;
	                }
                }
            }
        }
        
        if (checkContainer) {
        	validateContainer(instanceFields, clazz, props);
        }
    }
    
    private void validateContainer(List<FieldAndAnnotation> fields, JavaClass clazz, Map<String, String> props) {
        boolean singleton = false;
        boolean container = false;
        for (AnnotationEntry ae : clazz.getAnnotationEntries()) {
            String type = ae.getAnnotationType();
            singleton |= type.contains("Singleton");
            singleton |= type.contains("LazySingleton");
            singleton |= type.contains("EagerSingleton");
            container |= type.contains("InspectableContainer");
        }
        
        if(singleton && container)
        	throw new RuntimeException("Can't be both container && singleton.  Class: " + clazz.getClassName());

        JavaClass actualClass = null;
        JavaClass lookupClass = null;
        if(container) {
    		try {
				Class<?> asClass = Class.forName(clazz.getClassName());
				Class<?> parent = asClass.getEnclosingClass();
				JavaClass parentClazz = Repository.lookupClass(parent);
				lookupClass = validateGuice(parentClazz);
				if(parentClazz == lookupClass)
				    lookupClass = null;
				actualClass = clazz; 
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
        } else {
            actualClass = clazz;
        	lookupClass = validateGuice(clazz);
        	if(lookupClass.equals(actualClass)) {
        	    lookupClass = null;
        	}
        }

        if(actualClass != null) {
	        for(FieldAndAnnotation faa : fields) {
	        	map(false, faa.getField(), lookupClass, actualClass, faa.getAnnotation(), props);
	        }
        }
    }
    
    private JavaClass validateGuice(JavaClass javaClazz) {
    	try {
	    	Class<?> clazz = Class.forName(javaClazz.getClassName());
	    	List<ProviderAndBoundClass> bindings = new ArrayList<ProviderAndBoundClass>();
	    	while(clazz != null && clazz != Object.class) {
	    		Provider<?> provider = getProvider(injector, clazz);
	    		if(provider != null)
	    			bindings.add(new ProviderAndBoundClass(clazz, provider));
	    		for(Class<?> interfaze : clazz.getInterfaces()) {
	    			provider = getProvider(injector, interfaze);
		    		if(provider != null)
		    			bindings.add(new ProviderAndBoundClass(interfaze, provider));
	    		}
	    		clazz = clazz.getSuperclass();
	    	}
	    	
	    	if(bindings.isEmpty())
	    		throw new RuntimeException("nothing bound to class: " + javaClazz.getClassName());
	    	
	    	Class<?> originalClass = Class.forName(javaClazz.getClassName());
	    	Provider<?> originalProvider = getProvider(injector, originalClass);
	    	Object originalInstance = null;
	    	if(originalProvider != null) {
	    	    originalInstance = originalProvider.get();
	    	    // if it wasn't singleton, null it out.
	    	    if(originalInstance != originalProvider.get()) {
	    	        originalInstance = null;
	    	    }
	    	}
	    	
	    	// If there was an instance this pointed to, and it was a singleton,
	    	// then just make sure that no other bindings are subclasses of this,
	    	// as that may point to an error.
	    	if(originalInstance != null) {
	    	    for(ProviderAndBoundClass binding : bindings) {
	    	        Object newInstance = binding.getProvider().get();
	    	        if(newInstance != originalInstance && originalClass.isInstance(newInstance)) {
	    	            throw new RuntimeException("Found subclass binding!  Subclass instance: " + newInstance + ", original instance: " + originalInstance + ".  This may not actually be an error, but it's difficult to tell, so we're failing.");
	    	        }
	    	    }
	    	    if(!verifySingleton(injector, originalClass)) {
	    	        throw new RuntimeException("Instance class: " + originalClass + ", from container: " + javaClazz.getClassName() + " is not a singleton or lazysingleton!");    
	    	    }
	    	    return Repository.lookupClass(originalClass);
	    	} else {	    	
    	    	// Now that we have every possible binding to this class, make sure
    	    	// that each Provider returns the same instance.
    	    	Object instance = null;
    	    	for(ProviderAndBoundClass binding : bindings) {
    	    		Object newInstance = binding.getProvider().get();
    	    		if(instance == null)
    	    			instance = newInstance;
    	    		if(instance != newInstance) {
    	    			throw new RuntimeException("Instances don't match.  Current binding: " + binding + ".  Prior instance: " + instance + ", current instance: " + newInstance);
    	    		}
    	    	}
    	    	
    	    	// All bindings point to the same instance, so it doesn't matter
    			// which binding we use to do the lookup.
    	    	clazz = bindings.get(0).getBoundClass();
    	    	
    	    	// Verify this is a singleton
    	    	if(!verifySingleton(injector, clazz))
    	    	    throw new RuntimeException("clazz: " + clazz + ", from container: " + javaClazz.getClassName() + " is not a singleton or lazysingleton!");
    	    	
    	    	return Repository.lookupClass(clazz);
	    	}
    	} catch(ClassNotFoundException cnfe) {
    		throw new RuntimeException(cnfe);
    	}
    }
    
    private Provider<?> getProvider(Injector injector, Class<?> clazz) {
    	try {
    		return injector.getProvider(clazz);
    	} catch(RuntimeException ignored) {
    		return null;
    	}
    }
    
    private boolean verifySingleton(Injector injector, Class<?> clazz) {
        Binding<?> binding = resolveBinding(injector, injector.getBinding(clazz));
        return binding.acceptScopingVisitor(new BindingScopingVisitor<Boolean>() {
            public Boolean visitEagerSingleton() { return true; }
            public Boolean visitNoScoping() { return false; }
            public Boolean visitScope(com.google.inject.Scope scope) { return scope == Scopes.SINGLETON || scope == MoreScopes.LAZY_SINGLETON || scope == MoreScopes.EAGER_SINGLETON; }
            public Boolean visitScopeAnnotation(java.lang.Class<? extends Annotation> scopeAnnotation) { return false; }
        });    	
    }    
    
    private Binding<?> resolveBinding(final Injector injector, final Binding<?> binding) {
        return binding.acceptTargetVisitor(new BindingTargetVisitor<Object, Binding<?>>() {

            @Override
            public Binding<?> visit(InstanceBinding<? extends Object> link) {
                return binding;
            }

            @Override
            public Binding<?> visit(ProviderInstanceBinding<? extends Object> link) {
                return binding;
            }

            @Override
            public Binding<?> visit(ProviderKeyBinding<? extends Object> link) {
                return resolveBinding(injector, injector.getBinding(link.getProviderKey()));
            }

            @Override
            public Binding<?> visit(LinkedKeyBinding<? extends Object> link) {
                return resolveBinding(injector, injector.getBinding(link.getLinkedKey()));
            }

            @Override
            public Binding<?> visit(ExposedBinding<? extends Object> link) {
                return binding;
            }

            @Override
            public Binding<?> visit(UntargettedBinding<? extends Object> link) {
                return binding;
            }

            @Override
            public Binding<?> visit(ConstructorBinding<? extends Object> link) {
                return binding;
            }

            @Override
            public Binding<?> visit(ConvertedConstantBinding<? extends Object> link) {
                return binding;
            }

            @Override
            public Binding<?> visit(ProviderBinding<? extends Object> link) {
                return resolveBinding(injector, injector.getBinding(link.getProvidedKey()));
            }
            
        });
    }
    
    private void map(boolean staticField, Field field, JavaClass lookupClass, JavaClass actualClass, AnnotationEntry annotation, Map<String, String> map) {
    	String key = annotation.getElementValuePairs()[0].getValue().toString();
    	String value = actualClass.getClassName() + (staticField ? ":" : ",") + field.getName();
    	if(lookupClass != null)
    		value = lookupClass.getClassName() + "|" + value;
    	if(key == null || key.trim().length() == 0)
    		throw new RuntimeException("no ID for point: " + field.getName() + ", in: "+ actualClass.getClassName() + ", lookedup in: " + (lookupClass == null ? "null" : lookupClass.getClassName()));
    	map.put(key, value);
    }
    
    private static class FieldAndAnnotation {
    	private final Field field;
    	private final AnnotationEntry annotation;
    	
		public FieldAndAnnotation(Field field, AnnotationEntry annotation) {
			this.field = field;
			this.annotation = annotation;
		}

		public Field getField() {
			return field;
		}

		public AnnotationEntry getAnnotation() {
			return annotation;
		}
    }
    
    private static class ProviderAndBoundClass {
    	private final Class<?> boundClass;
    	private final Provider<?> provider;
    	
		public ProviderAndBoundClass(Class<?> boundClass, Provider<?> provider) {
			this.boundClass = boundClass;
			this.provider = provider;
		}

		public Class<?> getBoundClass() {
			return boundClass;
		}

		public Provider<?> getProvider() {
			return provider;
		}
		
		@Override
		public String toString() {
			return "provider: " + provider + ", bound to: " + boundClass;
		}
    }
}

