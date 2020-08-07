package de.uni_hildesheim.sse.test.utils.oo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.WrappedClass;

/**
 * Non static functions to analyze the same (set of) class multiple times.
 * 
 * @author El-Sharkawy
 * 
 */
//checkstyle: stop parameter number check
public class OOAnalyzer {

    private Set<WrappedClass> javaClasses;
    private String packageName;
    private String className;
    private boolean isMandatory;
    /**
     * The file of the sumbission, if exactly one match was found, otherwise <tt>null</tt>.
     */
    private String file;

    /**
     * Default constructor for this class.
     * Errors will be reported as mandatory failures.
     * 
     * @param packageName
     *            Optional package name specification. Can be <tt>null</tt>.
     *            Package name check will be case sensitive and must be in java
     *            syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     * @param className
     *            The simple name of the class (must <b>not</b> contain package
     *            names or be empty/<tt>null</tt>).
     * @see #OOAnalyzer(String, String, boolean)
     */
    public OOAnalyzer(String packageName, String className) {
        this(packageName, className, true);
    }
    
    /**
     * Constructor for this class to specify whether all reported errors are <b>mandatory errors</b>
     * or <b>warnings</b>.
     * 
     * @param packageName
     *            Optional package name specification. Can be <tt>null</tt>.
     *            Package name check will be case sensitive and must be in java
     *            syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     * @param className
     *            The simple name of the class (must <b>not</b> contain package
     *            names or be empty/<tt>null</tt>).
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     */
    public OOAnalyzer(String packageName, String className, boolean isMandatory) {
        this.packageName = packageName;
        this.className = className;
        this.isMandatory = isMandatory;
        javaClasses = OOAnalyzerUtils.getClasses(packageName, className);
        
        if (javaClasses != null && javaClasses.size() == 1) {
            Iterator<WrappedClass> itr = javaClasses.iterator();
            WrappedClass jClass = itr.hasNext() ? itr.next() : null;
            file = null != jClass ? jClass.getFileName() : null;
        }
    }

    /**
     * Checks whether there exist a class which fulfills the specification of the homework.
     * Leads to an error if the class does not exist. Short cut for {@link #classExists(Visibility, Modifier, boolean)}.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @return <tt>true</tt> if at least one class could be found inside the submission, which fulfills the
     *     specification completely, <tt>false</tt> otherwise.
     * @see #classExists(Visibility, Modifier, boolean)
     * @see #isInterface()
     */
    public boolean classExists(Visibility visibility, Modifier modifier) {
        return classExists(visibility, modifier, true);
    }

    /**
     * Checks whether there exist a class which fulfills the specification of the homework.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param mustExist if <code>true</code> leads to an error message if the class does not exist, no error message
     *      if <code>false</code>
     * @return <tt>true</tt> if at least one class could be found inside the submission, which fulfills the
     *     specification completely, <tt>false</tt> otherwise.
     * @see #isInterface()
     */
    public boolean classExists(Visibility visibility, Modifier modifier, boolean mustExist) {
        boolean classExists = OOAnalyzerUtils.classExists(javaClasses, visibility, modifier);
        if (mustExist && !classExists) {
            ErrorMsgUtils.classMissing(isMandatory, packageName, className, visibility, modifier);
        }
        
        return classExists;
    }
    
    /**
     * Short cut for {@link #classExists(Visibility, Modifier, boolean)} if no visibility and no modifier where
     * specified for the class itself. Leads to an error if the class does not exist.
     * @return <tt>true</tt> if at least one class could be found inside the submission, which fullfills the
     *     specification completely, <tt>false</tt> otherwise.
     * @see #classExists(Visibility, Modifier, boolean)
     * @see #isInterface()
     */
    public boolean classExists() {
        return classExists(null, null);
    }

    /**
     * Short cut for {@link #classExists(Visibility, Modifier, boolean)} if no visibility and no modifier where
     * specified for the class itself.
     * @param mustExist if <code>true</code> leads to an error message if the class does not exist, no error message
     *     if <code>false</code>
     * @return <tt>true</tt> if at least one class could be found inside the submission, which fulfills the
     *     specification completely, <tt>false</tt> otherwise.
     * @see #classExists(Visibility, Modifier, boolean)
     * @see #isInterface()
     */
    public boolean classExists(boolean mustExist) {
        return classExists(null, null, mustExist);
    }

    /**
     * Checks whether there exist an interface which fulfills the specification of the homework.
     * This method will do a similary operation as {@link #classExists()} but will also test that the class is an
     * interface.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @return <tt>true</tt> if at least one interface could be found inside the submission, which fulfills
     *     the specification completely, <tt>false</tt> otherwise.
     */
    public boolean isInterface(Visibility visibility) {
        boolean isInterface = OOAnalyzerUtils.interfaceExists(javaClasses, visibility);
        
        if (!isInterface) {
            ErrorMsgUtils.interfaceMissing(isMandatory, packageName, className, visibility);
        }
        
        return isInterface;
    }
    
    /**
     * Shortcut for {@link #isInterface(Visibility)} if no visibility where specified for the interface itself.
     * @return <tt>true</tt> if at least one interface could be found inside the submission, which fulfills
     *     the specification completely, <tt>false</tt> otherwise.
     * @see #isInterface(Visibility)
     * @see #classExists()
     */
    public boolean isInterface() {
        return isInterface(null);
    }

    /**
     * Shortcut of {@link #methodExists(String, Visibility, Modifier, boolean, Class, Class...)} for testing Getters.
     * These methods are usually public, have no modifiers, parameters and are not static. If one of the assumptions
     * is violated, than {@link #methodExists(String, Visibility, Modifier, boolean, Class, Class...)} should be used.
     * @param methodName The name of the specified method inside the class (e.g. <tt>getName</tt>).
     * @param returnType The desired return type or <tt>null</tt> if the method shall be a void method.
     *     This will also allow inherited classes of the specified <tt>returnType</tt> as a return type.
     * @return <tt>true</tt> if at least one methods could be found inside the submission, which fulfills the specified
     *     completely, <tt>false</tt> otherwise.
     * @see #methodExists(String, Visibility, Modifier, boolean, Class, Class...)
     */
    public boolean getterExists(String methodName, Class<?> returnType) {
        return methodExists(methodName, Visibility.PUBLIC, null, false, returnType);
    }
    
    /**
     * Checks whether there exist a method which fulfills the specification of the homework.<p>
     * <b>Attention:</b> Will not detect inherited methods.
     * @param methodName The name of the specified method inside the class.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param shallBeStatic <tt>true</tt> if the method shall be static, <tt>false</tt> otherwise.
     * @param returnType The desired return type or <tt>null</tt> if the method shall be a void method.
     *     This will also allow inherited classes of the specified <tt>returnType</tt> as a return type.
     * @param parameterTypes The parameters of the method.
     * @return <tt>true</tt> if at least one methods could be found inside the submission, which fulfills the specified
     *     completely, <tt>false</tt> otherwise.
     */
    public boolean methodExists(String methodName, Visibility visibility, Modifier modifier,
        boolean shallBeStatic, Class<?> returnType, Class<?>... parameterTypes) {
        
        return methodExists(isMandatory, methodName, visibility, modifier, shallBeStatic, returnType, null,
            parameterTypes);
    }
    
    /**
     * Checks whether there exist a method which fulfills the specification of the homework.<p>
     * <b>Attention:</b> Will not detect inherited methods.
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     * @param methodName The name of the specified method inside the class.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param shallBeStatic <tt>true</tt> if the method shall be static, <tt>false</tt> otherwise.
     * @param returnType The desired return type or <tt>null</tt> if the method shall be a void method.
     *     This will also allow inherited classes of the specified <tt>returnType</tt> as a return type.
     * @param exceptions Optional collection of specified exceptions which shall be thrown. Will be ignored if it is
     *     <tt>null</tt>.
     * @param parameterTypes The parameters of the method.
     * @return <tt>true</tt> if at least one methods could be found inside the submission, which fulfills the specified
     *     completely, <tt>false</tt> otherwise.
     */
    public boolean methodExists(boolean isMandatory, String methodName, Visibility visibility, Modifier modifier,
        boolean shallBeStatic, Class<?> returnType, Collection<Class<?>> exceptions, Class<?>... parameterTypes) {
        
        boolean methodExists = OOAnalyzerUtils.methodExists(javaClasses, methodName, visibility, modifier,
                shallBeStatic, returnType, exceptions, parameterTypes);
        
        if (!methodExists) {
            ErrorMsgUtils.methodMissing(isMandatory, file, packageName, className, methodName, visibility, modifier,
                shallBeStatic, returnType, exceptions, parameterTypes);
        }
        
        return methodExists;
    }
    
    /**
     * Checks whether there exist a constructor which fulfills the specification of the homework.<p>
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param parameterTypes The parameters of the constructor.
     * @return <tt>true</tt> if at least one constructor could be found inside the submission,
     *     which fulfills the specified completely, <tt>false</tt> otherwise.
     */
    public boolean constructorExists(Visibility visibility, Class<?>... parameterTypes) {
        return constructorExists(isMandatory, visibility, parameterTypes);
    }
    
    /**
     * Checks whether there exist a constructor which fulfills the specification of the homework.<p>
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param parameterTypes The parameters of the constructor.
     * @return <tt>true</tt> if at least one constructor could be found inside the submission,
     *     which fulfills the specified completely, <tt>false</tt> otherwise.
     */
    public boolean constructorExists(boolean isMandatory, Visibility visibility, Class<?>... parameterTypes) {
        
        boolean constructorExists = OOAnalyzerUtils.constructorExists(javaClasses, visibility, parameterTypes);
        
        if (!constructorExists) {
            ErrorMsgUtils.constructorMissing(isMandatory, file, packageName, className, visibility, parameterTypes);
        }
        
        return constructorExists;
    }
    
    /**
     * Short-cut for {@link #attributeExists(String, Visibility, Modifier, boolean, Class)}. This will test whether
     * a non static private attribute with no modifier exists. <p>
     * <b>Attention:</b> Will not probably not detect inherited methods.
     * @param attributeName The name of the specified attribute inside the class.
     * @param type The desired type of the attribute. Must not be <tt>null</tt>.
     *     This will also allow inherited class of the specified <tt>type</tt>.
     * @return <tt>true</tt> if at least one attribute could be found inside the submission, which fulfills the
     *     specified completely, <tt>false</tt> otherwise.
     */
    public boolean simpleAttributeExists(String attributeName, Class<?> type) {
        return attributeExists(attributeName, Visibility.PRIVATE, null, false, type);
    }
    
    /**
     * Checks whether there exist an attribute which fulfills the specification of the homework.<p>
     * <b>Attention:</b> Will not probably not detect inherited methods.
     * @param attributeName The name of the specified attribute inside the class.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param shallBeStatic <tt>true</tt> if the method shall be static, <tt>false</tt> otherwise.
     * @param type The desired type of the attribute. Must not be <tt>null</tt>.
     *     This will also allow inherited class of the specified <tt>type</tt>.
     * @return <tt>true</tt> if at least one attribute could be found inside the submission, which fulfills the
     *     specified completely, <tt>false</tt> otherwise.
     */
    public boolean attributeExists(String attributeName, Visibility visibility, Modifier modifier,
        boolean shallBeStatic, Class<?> type) {
        
        return attributeExists(isMandatory, attributeName, visibility, modifier, shallBeStatic, type);
    }
    
    /**
     * Checks whether there exist an attribute which fulfills the specification of the homework.<p>
     * <b>Attention:</b> Will not probably not detect inherited methods.
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     * @param attributeName The name of the specified attribute inside the class.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param shallBeStatic <tt>true</tt> if the method shall be static, <tt>false</tt> otherwise.
     * @param type The desired type of the attribute. Must not be <tt>null</tt>.
     *     This will also allow inherited class of the specified <tt>type</tt>.
     * @return <tt>true</tt> if at least one attribute could be found inside the submission, which fulfills the
     *     specified completely, <tt>false</tt> otherwise.
     */
    public boolean attributeExists(boolean isMandatory, String attributeName, Visibility visibility, Modifier modifier,
        boolean shallBeStatic, Class<?> type) {
        
        boolean attributeExists = OOAnalyzerUtils.attributeExists(javaClasses, attributeName, visibility, modifier,
            shallBeStatic, type);
        
        if (!attributeExists) {
            ErrorMsgUtils.attributeMissing(isMandatory, file, packageName, className, attributeName, visibility,
                modifier, shallBeStatic, type);
        }
        
        return attributeExists;
    }
    
    /**
     * Tests whether a given attribute has no setters. Since this test is only an heuristic, the produces error message
     * will only be returned as a warning and not block the submission.
     * @param attributeName The name of the attribute, which should be &#123;readOnly&#125;.
     * @param type The desired type of the attribute. Must not be <tt>null</tt>.
     *     This will also allow inherited class of the specified <tt>type</tt>.
     * @return <tt>true</tt> if no Setter was found for this attribute , <tt>false</tt> otherwise.
     * @see #attributeExists(String, Visibility, Modifier, boolean, Class)
     */
    public boolean isReadOnlyAttribute(String attributeName, Class<?> type) {
        boolean hasNoSetter = true;
        
        Class<?> errorClass = OOAnalyzerUtils.isReadOnlyAttribute(javaClasses, attributeName, type);
        if (null != errorClass) {
            hasNoSetter = false;
            
            ErrorMsgUtils.isNotReadonlyAttribute(file, errorClass, attributeName);
        }
        
        return hasNoSetter;
    }
    
    /**
     * Tests whether this class extends the specified class. The super class can be a student defined class or
     * a class from the Java API.<p>
     * <b>Attention:</b> If the super class does not exits or could not be found, this method will not create an error.
     * For this reason, the existence of the super class should also be tested, if the super class has to be defined
     * by the students.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.<p>
     *     &nbsp;&nbsp;<b>Attention:</b> If packageName starts with <tt>java</tt>, this method will check whether the
     *     given class extends a Java API class.
     * @param className The name of the class from the super class.
     * @return <tt>true</tt> if at least one correct inheritance relationship was found or if no super class was found
     *     matching the given parameters, <tt>false</tt> otherwise.
     */
    public boolean extendsFrom(String packageName, String className) {
        boolean correctExtends = OOAnalyzerUtils.classExtends(javaClasses, packageName, className);
        
        if (!correctExtends) {
            ErrorMsgUtils.inheritanceMissing(isMandatory, file, this.packageName, this.className, packageName,
                className);
        }
        
        return correctExtends;
    }  
    
    /**
     * Tests whether this class implements the specified interface. The interface can be a student defined interface or
     * an interface from the Java API.<p>
     * <b>Attention:</b> If the interface does not exits or could not be found, this method will not create an error.
     * For this reason, the existence of the interface should also be tested, if the interface has to be defined
     * by the students.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.<p>
     *     &nbsp;&nbsp;<b>Attention:</b> If packageName starts with <tt>java</tt>, this method will check whether the
     *     given class implements a Java API interface.
     * @param interfaceName The name of the interface to be implemented.
     * @return <tt>true</tt> if at least one correct interface was implemented or if no interface was found
     *     matching the given parameters, <tt>false</tt> otherwise.
     */
    public boolean implementsInterface(String packageName, String interfaceName) {
        return implementsInterface(isMandatory, packageName, interfaceName);
    }
    
    /**
     * Tests whether this class implements the specified interface. The interface can be a student defined interface or
     * an interface from the Java API.<p>
     * <b>Attention:</b> If the interface does not exits or could not be found, this method will not create an error.
     * For this reason, the existence of the interface should also be tested, if the interface has to be defined
     * by the students.
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.<p>
     *     &nbsp;&nbsp;<b>Attention:</b> If packageName starts with <tt>java</tt>, this method will check whether the
     *     given class implements a Java API interface.
     * @param interfaceName The name of the interface to be implemented.
     * @return <tt>true</tt> if at least one correct interface was implemented or if no interface was found
     *     matching the given parameters, <tt>false</tt> otherwise.
     */
    public boolean implementsInterface(boolean isMandatory, String packageName, String interfaceName) {
        boolean correctImplements = OOAnalyzerUtils.implementsInterface(javaClasses, packageName, interfaceName);
        
        if (!correctImplements) {
            ErrorMsgUtils.interfaceNotImplemented(isMandatory, file, this.packageName, className, packageName,
                interfaceName);
        }
        
        return correctImplements;
    }
    
    /**
     * Tests whether the given <b>static</b> attribute exists in a class and has a specific value. <p>
     * @param attributeName The name of the specified attribute
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param type The desired type of the attribute. Must not be <tt>null</tt>.
     *     This will also allow inherited class of the specified <tt>type</tt>.
     * @param value The specified value of the static attribute.
     * @return <tt>true</tt> if at least one attribute  with the specified values was found inside the submission,
     *     which fulfills the specification completely, <tt>false</tt> otherwise.
     */
    public boolean hasStaticValue(String attributeName, Visibility visibility, Modifier modifier,
        Class<?> type, Object value) {
        
        return hasStaticValue(isMandatory, attributeName, visibility, modifier, type, value);
    }
    
    /**
     * Tests whether the given <b>static</b> attribute exists in a class and has a specific value. <p>
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     * @param attributeName The name of the specified attribute
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param type The desired type of the attribute. Must not be <tt>null</tt>.
     *     This will also allow inherited class of the specified <tt>type</tt>.
     * @param value The specified value of the static attribute.
     * @return <tt>true</tt> if at least one attribute  with the specified values was found inside the submission,
     *     which fulfills the specification completely, <tt>false</tt> otherwise.
     */
    public boolean hasStaticValue(boolean isMandatory, String attributeName, Visibility visibility, Modifier modifier,
        Class<?> type, Object value) {
        
        boolean hasCorrectValue = OOAnalyzerUtils.attributeExists(javaClasses, attributeName, visibility, modifier,
            true, type);
        hasCorrectValue = hasCorrectValue && OOAnalyzerUtils.hasClassValue(javaClasses, attributeName, value);
        
        if (!hasCorrectValue) {
            ErrorMsgUtils.staticValueMissing(isMandatory, file, packageName, className, attributeName, visibility,
                modifier, type, value);
        }
        
        return hasCorrectValue;
    }
}
//checkstyle: resume parameter number check