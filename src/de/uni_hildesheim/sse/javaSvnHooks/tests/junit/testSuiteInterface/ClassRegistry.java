package de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.WrongPackageException;

/**
 * A registry for all {@link WrappedClass} of the submission.
 * 
 * @author Adam Krafczyk
 */
public class ClassRegistry {

    private URLClassLoader classLoader;
    private Map<Class<?>, WrappedClass> classes;

    /**
     * Creates a {@link ClassRegistry} for the given project directory. The java
     * files must be already compiled.
     * 
     * @param projectDir The directory of the submission.
     * 
     * @throws ClassNotFoundException If loading a class fails.
     * @throws IOException If reading a file fails.
     * @throws WrongPackageException If a class has a wrong package declaration.
     */
    public ClassRegistry(File projectDir) throws ClassNotFoundException, 
        IOException, WrongPackageException {
        this(projectDir, null);
    }

    /**
     * Creates a {@link ClassRegistry} for the given project directory. The java
     * files must be already compiled.
     * 
     * @param projectDir The directory of the submission.
     * @param classpath optional classpath entries (may be <b>null</b>)
     * 
     * @throws ClassNotFoundException If loading a class fails.
     * @throws IOException If reading a file fails.
     * @throws WrongPackageException If a class has a wrong package declaration.
     */
    public ClassRegistry(File projectDir, List<String> classpath) 
        throws ClassNotFoundException, IOException, WrongPackageException {
        // Create class loader for project
        List<URL> urls = new ArrayList<URL>();
        try {
            urls.add(projectDir.toURI().toURL());
            if (null != classpath) {
                for (int i = 0; i < classpath.size(); i++) {
                    urls.add(new File(classpath.get(i)).toURI().toURL());
                }
            }
            Logger.INSTANCE.log("Building a classloader with " + urls);
            URL[] tmp = new URL[urls.size()];
            urls.toArray(tmp);
            classLoader = new URLClassLoader(tmp);
        } catch (MalformedURLException e1) {
            throw new ClassNotFoundException("", e1);
        }
        
        classes = new HashMap<Class<?>, WrappedClass>();
        registerSubmittedFiles(projectDir);
        
        // and register the rest of the classes, but without sources
        registerClasspathLibraries(urls);
        
        classes = Collections.unmodifiableMap(classes);
    }
    
    /**
     * Registers submitted files as classes if possible.
     * 
     * @param projectDir the project directory to scan
     * @throws ClassNotFoundException If loading a class fails.
     * @throws IOException If reading a file fails.
     * @throws WrongPackageException If a class has a wrong package declaration.
     */
    private void registerSubmittedFiles(File projectDir) 
        throws ClassNotFoundException, IOException, WrongPackageException {
        // Get all submitted java files
        Collection<File> javaFiles = FileUtils.listFiles(projectDir, 
            new String[] {"java"}, true);
        
        // Get all class names
        Collection<File> classFiles = FileUtils.listFiles(projectDir, 
            new String[] {"class"}, true);
        for (File classFile : classFiles) {
            String className = filePathToJavaClass(classFile.getAbsolutePath(),
                    projectDir.getAbsolutePath());
            
            Class<?> clazz = null;
            try {
                clazz = classLoader.loadClass(className);
            } catch (NoClassDefFoundError exc) {
                throw new WrongPackageException(classFile);
            }
            
            // Try to get the .java file
            String guessedName = classFile.getAbsolutePath();
            guessedName = guessedName.substring(0, guessedName.length()
                    - "class".length()) + "java";
            String matchingJavaFileName = null;
            File matchingJavaFile = null;
            for (File javaFile : javaFiles) {
                if (guessedName.equalsIgnoreCase(javaFile.getAbsolutePath())) {
                    
                    URI homeDirURI = projectDir.toURI();
                    URI javaFileURI = javaFile.toURI();
                    matchingJavaFileName = homeDirURI.relativize(javaFileURI)
                            .getPath();
                    matchingJavaFile = javaFile;
                    
                    break;
                }
            }
            
            String content = null;
            if (matchingJavaFile != null) {
                // Read content of the java file
                content = FileUtils.readFileToString(matchingJavaFile);
            }
            classes.put(clazz, new WrappedClass(clazz, matchingJavaFileName,
                    content));
        }
    }

    /**
     * Registers classes on the library path. Do not throw exceptions, as 
     * library classes may not completely be resolvable.
     * 
     * @param urls the URLs to scan
     */
    private void registerClasspathLibraries(List<URL> urls) {
        for (URL url : urls) {
            try {
                File file = new File(url.toURI());
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    registerClasspathLibrary(file);
                }
            } catch (URISyntaxException e) {
                Logger.INSTANCE.log("URI exception: " + e.getMessage());
            }
        }
    }

    /**
     * Registers a class on the library path. Do not throw exceptions, as 
     * library classes may not completely be resolvable. Do not look for code,
     * as sources may not be available.
     * 
     * @param file the class file to register
     */
    private void registerClasspathLibrary(File file) {
        try {
            JarInputStream jis = new JarInputStream(
                new FileInputStream(file));
            JarEntry entry;
            do {
                entry = jis.getNextJarEntry();
                if (null != entry && !entry.isDirectory()) {
                    String fileName = entry.getName();
                    if (fileName.endsWith(".class")) {
                        // see JarSpec, only /, not \
                        fileName = fileName.substring(0, fileName
                            .length() - 6).replace("/", ".");
                        try {
                            Class<?> clazz = classLoader
                                .loadClass(fileName);
                            classes.put(clazz, new WrappedClass(
                                clazz, null, null));
                        } catch (NoClassDefFoundError e2) {
                            Logger.INSTANCE.log("Library class "
                                + "not found: " + e2.getMessage());
                        } catch (ClassNotFoundException e2) {
                            Logger.INSTANCE.log("Library class "
                                + "not found: " + e2.getMessage());
                        }
                    }
                }
            } while (null != entry);
            jis.close();
        } catch (IOException e) {
            Logger.INSTANCE.log("I/O exception: " + e.getMessage());
        }
    }

    /**
     * Converts a file path to a class name for Java.
     * E.g.:
     * <code>/home/user/myPackage/MyClass.class</code> to
     * <code>myPackage.MyClass</code>
     * @param filePath The absolute path to the class file
     * @param rootDir The absoulte path to the root dir (should be a substring
     *        of filePath)
     * @return Fully qualified name of the class
     */
    private String filePathToJavaClass(String filePath, String rootDir) {
        StringBuffer fileName = new StringBuffer(filePath);
        // Remove the root path
        fileName.delete(0, rootDir.length() + 1);
        // Remove the file extension
        fileName.delete(fileName.length() - ".class".length(),
                fileName.length());
        // Convert separators into dots
        for (int i = 0; i < fileName.length(); i++) {
            if (fileName.charAt(i) == File.separatorChar) {
                fileName.replace(i, i + 1, ".");
            }
        }
        return fileName.toString();
    }
    
    /**
     * Searches a method in all classes.
     * @param name The name of the method
     * @param onlyPublic <code>false</code> if the method can have any
     * visibility.
     * @param parameterTypes The parameter types of the method.
     * @return The method or <code>null</code>.
     */
    public Method searchMethod(String name, boolean onlyPublic,
            Class<?> ... parameterTypes) {
        
        Method method = null;
        for (Class<?> clazz : classes.keySet()) {
            try {
                if (onlyPublic) {
                    method = clazz.getMethod(name, parameterTypes);
                } else {
                    method = clazz.getDeclaredMethod(name, parameterTypes);
                    method.setAccessible(true);
                }
            } catch (ReflectiveOperationException e) {
                // ignore
            }
        }
        
        return method;
    }
    
    /**
     * Getter for a {@link WrappedClass} of this registry.
     * @param clazz The {@link Class} to get the {@link WrappedClass} for.
     * @return The {@link WrappedClass} or <code>null</code>.
     */
    public WrappedClass getWrappedClass(Class<?> clazz) {
        return classes.get(clazz);
    }
    
    /**
     * Getter for all wrapped classes of this registry.
     * @return An unmodifiable collection of all wrapped classes.
     */
    public Collection<WrappedClass> getAllWrappedClasses() {
        return Collections.unmodifiableCollection(classes.values());
    }
    
    /**
     * Getter for all classes of this registry.
     * @return An unmodifiable set of all classes.
     */
    public Set<Class<?>> getAllClasses() {
        return Collections.unmodifiableSet(classes.keySet());
    }
    
    /**
     * Gets the line number of the given method. Uses method.getDeclaringClass()
     * to find the source file.
     * @param method The method to get the line number for.
     * @return The line number or -1 if not found.
     */
    public int getLineOfMethod(Method method) {
        WrappedClass wrappedClass = getWrappedClass(method.getDeclaringClass());
        
        int result = -1;
        
        if (wrappedClass != null) {
            String src = wrappedClass.getContent();
            if (src != null) {
                Class<?>[] parameters = method.getParameterTypes();
                String patternStr = method.getName() + "[^\\w]*\\x28[^\\w]*";
                for (Class<?> param : parameters) {
                    patternStr += param.getName() + "[^,]*,[^,]*";
                }
                patternStr += "\\x29";
                Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
                Matcher matcher = pattern.matcher(src);
                if (matcher.find()) {
                    int pos = matcher.start();
                    
                    result = src.substring(0, pos).split("\n").length;
                }
            }
        }
        
        return result;
    }
    
    @Override
    protected void finalize() throws Throwable {
        classLoader.close();
        super.finalize();
    }
    
}
