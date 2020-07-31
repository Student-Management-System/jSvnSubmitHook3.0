package de.uni_hildesheim.sse.test.utils.oo;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.ClassRegistry;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.WrappedClass;
import de.uni_hildesheim.sse.test.suite.AbstractJavaTestSuite;

/**
 * Static functions to analyze whether a project/class fulfills the class/OO specifications.
 * Methods can be separated into two different kinds of tests:
 * <ul>
 * <li>Names of <b>High-level tests</b> starts with a <tt>assert</tt>. These tests, will automatically
 * create a sufficient <b>error</b> message via
 * {@link de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestResult
 * #addTestFailure(de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestFailure)}</li>
 * <li>All other tests will test whether the specification is fulfilled, but <b>not</b> automatically produce an
 * <b>error</b> message.</li>
 * </ul>
 * @author El-Sharkawy
 *
 */
//checkstyle: stop parameter number check
//checkstyle: stop exception type check
public class OOAnalyzerUtils {
    private static final ClassRegistry REGISTRY = AbstractJavaTestSuite.getClassRegistry();
    private static final int ERROR_MODIFIER_VALUE = -1;
    
    /**
     * Avoids instantiation.
     */
    private OOAnalyzerUtils() {}
    
    /**
     * Returns a set of classes having the specified name.
     * <ul>
     * <li>Package names will be ignored.</li>
     * <li>Will only consider classes created by the students (which are part of the submission).</li>
     * <li>The <tt>className</tt> must be case sensitive.</li>
     * </ul>
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @return A list of all student defined classes having the defined name. This list may be empty.
     */
    public static Set<WrappedClass> getClasses(String packageName, String className) {
        Set<WrappedClass> javaClasses = new HashSet<WrappedClass>();
        for (WrappedClass javaClass : REGISTRY.getAllWrappedClasses()) {
            Class<?> clazz = javaClass.getWrappedClass();
            if (null != clazz && clazz.getSimpleName().equals(className)) {
                // If packaName was specified, check also package name
                if (null != packageName) {
                    Package javaPackage = clazz.getPackage();
                    if (null != javaPackage && javaPackage.getName().equals(packageName)) {
                        javaClasses.add(javaClass);
                    }
                } else {
                    // Add class without package name check, if no name was specified
                    javaClasses.add(javaClass);
                }
            }
        }
        
        return javaClasses;
    }
    
    /**
     * Searches for a user defined {@link Class} and returns it. This method should be used for testing correct usage
     * user of user defined datatypes, e.g. parameter of a method or type of an attribute.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @return The user defined class if exectly on match was found which matches the specification,
     *     <tt>null</tt> otherwise.
     */
    public static Class<?> getUserDefinedType(String packageName, String className) {
        Class<?> userType = null;
        
        Set<WrappedClass> wrappedClasses = getClasses(packageName, className);
        if (null != wrappedClasses && wrappedClasses.size() == 1) {
            WrappedClass wClass = wrappedClasses.iterator().next();
            userType = (wClass != null) ? wClass.getWrappedClass() : null;
        }
        
        return userType;
    }
    
    /**
     * Creates the array class object out of a class object.
     * @param type The class object which shall be transformed to an array.
     * @return The array class type (<tt>type[].class</tt>), or <tt>null</tt> in case of any (technical) errors.
     */
    public static Class<?> toArray(Class<?> type) {
        Class<?> arrayType = null;
        try {
            arrayType = Array.newInstance(type, 0).getClass();
        } catch (Exception exc) {
            /*
             * In case of any (technical) problems, do not fail and work with null
             */
        }
        
        return arrayType;
    }
    
    /**
     * Checks whether a {@link Class}, {@link Field} or {@link Method} has the specified visibility.
     * @param modifier The modifier of the tested element. In case of any error, use
     *     <tt>{@value #ERROR_MODIFIER_VALUE}</tt>. 
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @return <tt>true</tt>If the visibility fulfills the specification or if nothing was specified. Will also
     *     return <tt>true</tt> in case of any (technical) problems which may occur in our test infrastructure.<br/>
     *     Returns <tt>false</tt> if a visibility was specified and the checked element does not fulfill
     *     the specification.
     */
    private static boolean checkVisibility(int modifier, Visibility visibility) {
        boolean correctVisibility = true;
        
        if (null != visibility && ERROR_MODIFIER_VALUE != modifier) {
            switch (visibility) {
            case PUBLIC:
                correctVisibility = java.lang.reflect.Modifier.isPublic(modifier);
                break;
            case PROTECTED:
                correctVisibility = java.lang.reflect.Modifier.isProtected(modifier);
                break;
            case PACKAGE:
                /* 
                 * There exist no check for package/default visibility.
                 * Therefore, test that the element has no visibility modifier.
                 */
                correctVisibility = (!java.lang.reflect.Modifier.isPublic(modifier)
                    && !java.lang.reflect.Modifier.isProtected(modifier)
                    && !java.lang.reflect.Modifier.isPrivate(modifier));
                break;
            case PRIVATE:
                correctVisibility = java.lang.reflect.Modifier.isPrivate(modifier);
                break;
            default:
                /*
                 * No further action needed.
                 * In case of any (technical) problems, do not report an error to the students
                 * and accept their submission.
                 */
                break;
            }
        }
        
        return correctVisibility;
    }
    
    /**
     * Checks whether a {@link Class}, {@link Field} or {@link Method} has the specified modifier (abstract or final).
     * @param modifierValue The modifier of the tested element. In case of any error, use
     *     <tt>{@value #ERROR_MODIFIER_VALUE}</tt>.
     * @param modifier The specified modifier or <tt>null</tt> if no modifier was specified by us.
     * @return <tt>true</tt>If the modifier fulfills the specification or if nothing was specified. Will also
     *     return <tt>true</tt> in case of any (technical) problems which may occur in our test infrastructure.<br/>
     *     Returns <tt>false</tt> if a modifier was specified and the checked element does not fulfill
     *     the specification.
     */
    private static boolean checkCorrectModifier(int modifierValue, Modifier modifier) {
        boolean isCorrect = true;
        
        if (ERROR_MODIFIER_VALUE != modifierValue && null != modifier) {
            switch (modifier) {
            case FINAL:
                isCorrect = java.lang.reflect.Modifier.isFinal(modifierValue);
                break;
            case ABSTRACT:
                isCorrect = java.lang.reflect.Modifier.isAbstract(modifierValue);
                break;
            default:
                /*
                 * No further action needed.
                 * In case of any (technical) problems, do not report an error to the students
                 * and accept their submission.
                 */
            }
        }
        
        return isCorrect;
    }
    
    /**
     * Checks whether there exist a class which fulfills the specification of the homework.
     * Contrary to {@link #classExists(String, String, Visibility, Modifier)}, it will also create a mandatory
     * JUnit error together with an expressive error message.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @return <tt>true</tt> if at lest one class could be found inside the submission, which fulfills the specified
     *     completely, <tt>false</tt> otherwise.
     * @see #classExists(String, String, Visibility, Modifier)
     */
    public static boolean assertClassExists(String packageName, String className, Visibility visibility,
        Modifier modifier) {
        
        boolean classExists = classExists(packageName, className, visibility, modifier);
        if (!classExists) {
            ErrorMsgUtils.classMissing(true, packageName, className, visibility, modifier);
        }
        
        return classExists;
    }
    
    /**
     * Checks whether there exist a method which fulfills the specification of the homework.
     * Contrary to {@link #methodExists(String, String, String, Visibility, Modifier, boolean, Class, Class...)},
     * it will also create a mandatory JUnit error together with an expressive error message.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param methodName The name of the specified method inside the class.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param shallBeStatic <tt>true</tt> if the method shall be static, <tt>false</tt> otherwise.
     * @param returnType The desired return type or <tt>null</tt> if the method shall be a void method.
     *     This will also inherited class of the specified <tt>returnType</tt> as a return type.
     * @param parameterTypes The parameters of the method.
     * @return <tt>true</tt> if at least one methods could be found inside the submission, which fulfills the specified
     *     completely, <tt>false</tt> otherwise.
     * @see #methodExists(String, String, String, Visibility, Modifier, boolean, Class, Class...)
     */
    public static boolean assertMethodExists(String packageName, String className, String methodName,
        Visibility visibility, Modifier modifier, boolean shallBeStatic, Class<?> returnType,
        Class<?>... parameterTypes) {
        
        boolean methodExists = methodExists(packageName, className, methodName, visibility, modifier, shallBeStatic,
            returnType, parameterTypes);
        
        if (!methodExists) {
            ErrorMsgUtils.methodMissing(true, null, packageName, className, methodName, visibility, modifier,
                 shallBeStatic, returnType, null, parameterTypes);
        }
        
        
        return methodExists;
    }

    
    /**
     * Checks whether there exist an attribute which fulfills the specification of the homework.
     * Contrary to {@link #attributeExists(String, String, String, Visibility, Modifier, boolean, Class)},
     * it will also create a mandatory JUnit error together with an expressive error message.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param attributeName The name of the specified attribute inside the class.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param shallBeStatic <tt>true</tt> if the method shall be static, <tt>false</tt> otherwise.
     * @param type The desired type of the attribute. Must not be <tt>null</tt>.
     *     This will also allow inherited class of the specified <tt>type</tt>.
     * @return <tt>true</tt> if at least one attribute could be found inside the submission, which fulfills the
     *     specified completely, <tt>false</tt> otherwise.
     * @see #attributeExists(String, String, String, Visibility, Modifier, boolean, Class)
     */
    public static boolean assertAttributeExists(String packageName, String className, String attributeName,
        Visibility visibility, Modifier modifier, boolean shallBeStatic, Class<?> type) {
        
        boolean attributeExists = attributeExists(packageName, className, attributeName, visibility, modifier,
                shallBeStatic, type);
        
        if (!attributeExists) {
            ErrorMsgUtils.attributeMissing(true, null, packageName, className, attributeName, visibility, modifier,
                shallBeStatic, type);
        }
        
        
        return attributeExists;
    }
    
    /**
     * Checks whether there exist a class which fulfills the specification of the homework.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @return <tt>true</tt> if at least one class could be found inside the submission, which fulfills the specified
     *     completely, <tt>false</tt> otherwise.
     */
    public static boolean classExists(String packageName, String className, Visibility visibility,
        Modifier modifier) {
        
        Set<WrappedClass> candidates = getClasses(packageName, className);
        return classExists(candidates, visibility, modifier);
    }
    
    /**
     * Checks whether there exist a class which fulfills the specification of the homework.
     * @param javaClasses A set of all student defined classes which should be used for testing.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @return <tt>true</tt> if at least one class could be found inside the submission, which fulfills the specified
     *     completely, <tt>false</tt> otherwise.
     * @see #getClasses(String, String)
     */
    static boolean classExists(Set<WrappedClass> javaClasses, Visibility visibility, Modifier modifier) {
        
        boolean classFound = false;
        
        Iterator<WrappedClass> iter = javaClasses.iterator();
        while (iter.hasNext() && !classFound) {
            WrappedClass javaClass = iter.next();
            Class<?> clazz = javaClass.getWrappedClass();
            int mod = null != clazz ? clazz.getModifiers() : ERROR_MODIFIER_VALUE;
            classFound = true;
            
            // Candidate found, checking modifier
            classFound &= checkVisibility(mod, visibility);
            classFound &= checkCorrectModifier(mod, modifier);
        }
        
        return classFound;
    }
    
    /**
     * Checks whether there exist a class which fulfills the specification of the homework.
     * @param javaClasses A set of all student defined classes which should be used for testing.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @return <tt>true</tt> if at least one class could be found inside the submission, which fulfills the specified
     *     completely, <tt>false</tt> otherwise.
     * @see #getClasses(String, String)
     */
    static boolean interfaceExists(Set<WrappedClass> javaClasses, Visibility visibility) {
        
        boolean classFound = false;
        
        Iterator<WrappedClass> iter = javaClasses.iterator();
        while (iter.hasNext() && !classFound) {
            WrappedClass javaClass = iter.next();
            Class<?> clazz = javaClass.getWrappedClass();
            int mod = null != clazz ? clazz.getModifiers() : ERROR_MODIFIER_VALUE;
            classFound = true;
            
            // Candidate found, checking modifier
            classFound &= checkVisibility(mod, visibility);
            
            // Check whether the class is an interface
            if (classFound) {
                classFound &= java.lang.reflect.Modifier.isInterface(mod);
            }
        }
        
        return classFound;
    }
    
    /**
     * Checks whether there exist a method which fulfills the specification of the homework.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param methodName The name of the specified method inside the class.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param shallBeStatic <tt>true</tt> if the method shall be static, <tt>false</tt> otherwise.
     * @param returnType The desired return type or <tt>null</tt> if the method shall be a void method.
     *     This will also inherited class of the specified <tt>returnType</tt> as a return type.
     * @param parameterTypes The parameters of the method.
     * @return <tt>true</tt> if at least one methods could be found inside the submission, which fulfills the specified
     *     completely, <tt>false</tt> otherwise.
     */
    public static boolean methodExists(String packageName, String className, String methodName, Visibility visibility,
        Modifier modifier, boolean shallBeStatic, Class<?> returnType, Class<?>... parameterTypes) {
        
        Set<WrappedClass> candidates = getClasses(packageName, className);
        return methodExists(candidates, methodName, visibility, modifier, shallBeStatic, returnType, null,
            parameterTypes);
    }
    
    /**
     * Checks whether there exist a method which fulfills the specification of the homework.
     * Does not check methods of super classes.
     * @param javaClasses A set of all student defined classes which should be used for testing.
     * @param methodName The name of the specified method inside the class.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param shallBeStatic <tt>true</tt> if the method shall be static, <tt>false</tt> otherwise.
     * @param returnType The desired return type or <tt>null</tt> if the method shall be a void method.
     *     This will also allow inherited classes of the specified <tt>returnType</tt> as a return type.
     * @param exceptions Optional collection of specified exceptions which shall be thrown. Will be ignored if it is
     *     <tt>null</tt>. <tt>null</tt> elements inside the collection will also be ignored to avoid misleading
     *     failure messages.
     * @param parameterTypes The parameters of the method.
     * @return <tt>true</tt> if at least one methods could be found inside the submission, which fulfills the specified
     *     completely, <tt>false</tt> otherwise.
     */
    static boolean methodExists(Set<WrappedClass> javaClasses, String methodName, Visibility visibility,
        Modifier modifier, boolean shallBeStatic, Class<?> returnType, Collection<Class<?>> exceptions,
        Class<?>... parameterTypes) {
        
        boolean methodFound = false;
        Iterator<WrappedClass> iter = javaClasses.iterator();
        while (iter.hasNext() && !methodFound) {
            WrappedClass javaClass = iter.next();
            Class<?> clazz = javaClass.getWrappedClass();
            if (null != clazz) {
                Method method = null;
                try {
                    method = clazz.getDeclaredMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException exc) {
                    methodFound = false;
                } catch (Exception exc) {
                    /*
                     * In case of any (technical) problems, do not report an error to the students
                     * and accept their submission.
                     */
                    methodFound = true;
                }
                if (null != method && !methodFound) {
                    int mod = method.getModifiers();
                    
                    methodFound = true;
                    Class<?> actualReturnType = method.getReturnType();
                    if (null != returnType) {
                        methodFound &= null != actualReturnType ? returnType.isAssignableFrom(actualReturnType) : false;
                    } else {
                        methodFound &= void.class == actualReturnType;
                    }
                    methodFound &= checkVisibility(mod, visibility);
                    methodFound &= checkCorrectModifier(mod, modifier);
                    if (shallBeStatic) {
                        methodFound &= java.lang.reflect.Modifier.isStatic(mod);
                    }
                    
                    if (methodFound && null != exceptions) {
                        // Create set of declared Exceptions
                        Set<Class<?>> declaredExceptions = new HashSet<Class<?>>();
                        if (null != method.getExceptionTypes()) {
                            Collections.addAll(declaredExceptions, method.getExceptionTypes());
                        }
                        
                        // Check whether specified Exceptions are declared
                        for (Class<?> specifiedException : exceptions) {
                            if (null != specifiedException) {
                                methodFound &= declaredExceptions.contains(specifiedException);
                            }
                        }
                    }
                }
            } else {
                /*
                 * In case of any (technical) problems, do not report an error to the students
                 * and accept their submission.
                 */
                methodFound = true;
            }
        }
        
        return methodFound;
    }
    
    /**
     * Checks whether there exist a constructor which fulfills the specification of the homework.
     * @param javaClasses A set of all student defined classes which should be used for testing.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     *     This will also inherited class of the specified <tt>returnType</tt> as a return type.
     * @param parameterTypes The parameters of the method.
     * @return <tt>true</tt> if at least one methods could be found inside the submission, which fulfills the specified
     *     completely, <tt>false</tt> otherwise.
     */
    static boolean constructorExists(Set<WrappedClass> javaClasses, Visibility visibility, Class<?>... parameterTypes) {
        
        boolean constructorFound = false;
        
        Iterator<WrappedClass> iter = javaClasses.iterator();
        while (iter.hasNext() && !constructorFound) {
            WrappedClass javaClass = iter.next();
            Class<?> clazz = javaClass.getWrappedClass();
            if (null != clazz) {
                Constructor<?> constructor = null;
                try {
                    if (Visibility.PUBLIC == visibility) {
                        constructor = clazz.getConstructor(parameterTypes);
                    } else {
                        constructor = clazz.getDeclaredConstructor(parameterTypes);
                    }
                } catch (NoSuchMethodException exc) {
                    constructorFound = false;
                } catch (Exception exc) {
                    /*
                     * In case of any (technical) problems, do not report an error to the students
                     * and accept their submission.
                     */
                    constructorFound = true;
                }
                if (null != constructor && !constructorFound) {
                    int mod = constructor.getModifiers();
                    
                    constructorFound = true;
                    constructorFound &= checkVisibility(mod, visibility);
                }
            } else {
                /*
                 * In case of any (technical) problems, do not report an error to the students
                 * and accept their submission.
                 */
                constructorFound = true;
            }
        }
        
        return constructorFound;
    }
    
    /**
     * Checks whether there exist an attribute which fulfills the specification of the homework.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param attributeName The name of the specified attribute inside the class.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param shallBeStatic <tt>true</tt> if the method shall be static, <tt>false</tt> otherwise.
     * @param type The desired type of the attribute. Must not be <tt>null</tt>.
     *     This will also allow inherited class of the specified <tt>type</tt>.
     * @return <tt>true</tt> if at least one attribute could be found inside the submission, which fulfills the
     *     specified completely, <tt>false</tt> otherwise.
     */
    public static boolean attributeExists(String packageName, String className, String attributeName,
        Visibility visibility, Modifier modifier, boolean shallBeStatic, Class<?> type) {
        
        
        Set<WrappedClass> candidates = getClasses(packageName, className);
        return attributeExists(candidates, attributeName, visibility, modifier, shallBeStatic, type);
    }

    /**
     * Checks whether there exist an attribute which fulfills the specification of the homework.
     * @param javaClasses A set of all student defined classes which should be used for testing.
     * @param attributeName The name of the specified attribute inside the class.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param shallBeStatic <tt>true</tt> if the method shall be static, <tt>false</tt> otherwise.
     * @param type The desired type of the attribute. Must not be <tt>null</tt>.
     *     This will also allow inherited class of the specified <tt>type</tt>.
     * @return <tt>true</tt> if at least one attribute could be found inside the submission, which fulfills the
     *     specified completely, <tt>false</tt> otherwise.
     */
    static boolean attributeExists(Set<WrappedClass> javaClasses, String attributeName, Visibility visibility,
        Modifier modifier, boolean shallBeStatic, Class<?> type) {
        
        boolean attributeFound = false;
        
        Iterator<WrappedClass> iter = javaClasses.iterator();
        while (iter.hasNext() && !attributeFound) {
            WrappedClass javaClass = iter.next();
            Class<?> clazz = javaClass.getWrappedClass();
            if (null != clazz) {
                Field attribute = null;
                try {
                    if (Visibility.PUBLIC == visibility) {
                        attribute = clazz.getField(attributeName);
                    } else {
                        attribute = clazz.getDeclaredField(attributeName);
                    }
                } catch (NoSuchFieldException exc) {
                    attributeFound = false;
                } catch (Exception exc) {
                    /*
                     * In case of any (technical) problems, do not report an error to the students
                     * and accept their submission.
                     */
                    attributeFound = true;
                }
                if (null != attribute && !attributeFound) {
                    int mod = attribute.getModifiers();
                    
                    attributeFound = true;
                    Class<?> actualType = attribute.getType();
                    if (null != type) {
                        attributeFound &= null != actualType ? type.isAssignableFrom(actualType) : true;
                    }
                    attributeFound &= checkVisibility(mod, visibility);
                    attributeFound &= modifier != Modifier.ABSTRACT ? checkCorrectModifier(mod, modifier) : true;
                    if (shallBeStatic) {
                        attributeFound &= java.lang.reflect.Modifier.isStatic(mod);
                    } else {
                        attributeFound &= !java.lang.reflect.Modifier.isStatic(mod);
                    }
                }
            } else {
                /*
                 * In case of any (technical) problems, do not report an error to the students
                 * and accept their submission.
                 */
                attributeFound = true;
            }
        }
        
        return attributeFound;
    }
    
    /**
     * Checks whether a setter exists for a given attribute.
     * This method should only be sued to produce warnings, because this methods uses heuristics for finding setters.
     * @param javaClasses A set of all student defined classes which should be used for testing.
     * @param attributeName The name of the specified attribute inside the class.
     * @param type The desired type of the attribute. Must not be <tt>null</tt>.
     *     This will also allow inherited class of the specified <tt>type</tt>.
     * @return <tt>null</tt> if no setter exists, otherwise the first class which has a public setter for the given
     *     attribute.
     */
    public static Class<?> isReadOnlyAttribute(Set<WrappedClass> javaClasses, String attributeName, Class<?> type) {
        Class<?> errorClass = null;
        
        if (null != attributeName) {
            attributeName = attributeName.toLowerCase();
        }
        Iterator<WrappedClass> iter = javaClasses.iterator();
        while (iter.hasNext() && null == errorClass) {
            WrappedClass javaClass = iter.next();
            Class<?> clazz = javaClass.getWrappedClass();
            if (null != clazz) {
                Field attribute = null;
                try {
                    attribute = clazz.getDeclaredField(attributeName);
                } catch (Exception exc) {
                    /*
                     * In case of any (technical) problems, do not report an error to the students
                     * and accept their submission.
                     */
                }
                if (null != attribute && null != attributeName) {
                    try {
                        // Has attribute, check whether it has a public setter
                        Method[] methods = clazz.getDeclaredMethods();
                        for (Method method : methods) {
                            Class<?>[] parameters = null != method ? method.getParameterTypes() : null;
                            
                            // Check method parameter and visibility
                            if (null != method && java.lang.reflect.Modifier.isPublic(method.getModifiers())
                                && null != parameters && parameters.length == 1) {
                                
                                // Check method name
                                String methodName = method.getName();
                                if (methodName != null && methodName.startsWith("set") && parameters[0] == type
                                    && methodName.toLowerCase().contains(attributeName)) {
                                    errorClass = clazz;
                                }
                            }
                        }
                    } catch (Exception exc) {
                        /*
                         * In case of any (technical) problems, do not report an error to the students
                         * and accept their submission.
                         */
                    }
                }
            }
        }
        
        return errorClass;
    }
    
    /**
     * Checks whether the given <tt>javaClass</tt> extends a given user defined / java API class. 
     * @param javaClasses A set of all student defined classes which should be used for testing.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.<br/>
     *     &nbsp;&nbsp;<b>Attention:</b> If packageName starts with <tt>java</tt>, this method will check whether the
     *     given class extends a Java API class.
     * @param className The name of the class from the super class.
     * @return <tt>true</tt> if at least one correct inheritance relationship was found or if no super class was found
     *     matching the given parameters, <tt>false</tt> otherwise.
     */
    static boolean classExtends(Set<WrappedClass> javaClasses, String packageName, String className) {
        boolean inheritanceCorrect = true;
        
        Class<?> superType = null;
        try {
            if (null != packageName && packageName.startsWith("java")) {
                superType = Class.forName(packageName + "." + className);
            } else {
                superType = OOAnalyzerUtils.getUserDefinedType(packageName, className);
            }
        } catch (Exception exc) {
            /*
             * In case of any (technical) problems, do not report an error to the students
             * and accept their submission.
             */
        }
        
        // Check only if a super type was found, otherwise it's maybe an error from us.
        if (null != superType) {
            // Find at least one correct inheritance
            inheritanceCorrect = false;
            
            Iterator<WrappedClass> iter = javaClasses.iterator();
            while (iter.hasNext() && !inheritanceCorrect) {
                WrappedClass javaClass = iter.next();
                Class<?> clazz = javaClass.getWrappedClass();
                if (null != clazz) {
                    do {
                        clazz = clazz.getSuperclass();
                        if (clazz == superType) {
                            inheritanceCorrect = true;
                        }
                    } while (clazz.getSuperclass() != null && !inheritanceCorrect);
                }
            }
        }
        
        return inheritanceCorrect;
    }
    
    /**
     * Checks whether the given <tt>javaClass</tt> implements a given user defined / java API interface. 
     * @param javaClasses A set of all student defined classes which should be used for testing.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.<br/>
     *     &nbsp;&nbsp;<b>Attention:</b> If packageName starts with <tt>java</tt>, this method will check whether the
     *     given class implements a Java API interface.
     * @param interfaceName The name of the interface which shall be implemented.
     * @return <tt>true</tt> if at least one correct interface was found which was implemented matching the given
     *     parameters, <tt>false</tt> otherwise.
     */
    static boolean implementsInterface(Set<WrappedClass> javaClasses, String packageName, String interfaceName) {
        boolean inheritanceCorrect = true;
        
        Class<?> superType = null;
        try {
            if (null != packageName && packageName.startsWith("java")) {
                superType = Class.forName(packageName + "." + interfaceName);
            } else {
                superType = OOAnalyzerUtils.getUserDefinedType(packageName, interfaceName);
            }
        } catch (Exception exc) {
            /*
             * In case of any (technical) problems, do not report an error to the students
             * and accept their submission.
             */
        }
        
        // Check only if a super type was found, otherwise it's maybe an error from us.
        if (null != superType) {
            // Find at least one correct inheritance
            inheritanceCorrect = false;
            
            Iterator<WrappedClass> iter = javaClasses.iterator();
            while (iter.hasNext() && !inheritanceCorrect) {
                WrappedClass javaClass = iter.next();
                Class<?> clazz = javaClass.getWrappedClass();
                if (null != clazz) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (int i = 0; null != interfaces &&  i < interfaces.length && !inheritanceCorrect; i++) {
                        if (interfaces[i] == superType) {
                            inheritanceCorrect = true;
                        }
                    }
                }
            }
        }
        
        return inheritanceCorrect;
    }
    
    /**
     * Checks whether a static attribute of a class has the value which was specified in the homework. <br/>
     * Please note that the existence of the static attribute should be determined before in a separate test.
     * @param javaClasses A set of all student defined classes which should be used for testing.
     * @param attributeName The name of the specified <b>static</b> attribute inside the class.
     * @param value The value which was specified inside the homework.
     * @return <tt>true</tt> if the static value was found in one of the given classes, or if a (technical) error
     *     occurred, <tt>false</tt> otherwise.
     */
    static boolean hasClassValue(Set<WrappedClass> javaClasses, String attributeName, Object value) {
        boolean hasCorrectValue = false;
        Iterator<WrappedClass> iter = javaClasses.iterator();
        
        while (iter.hasNext() && !hasCorrectValue) {
            WrappedClass javaClass = iter.next();
            Class<?> clazz = javaClass.getWrappedClass();
            if (null != clazz) {
                try {
                    Field attribute = clazz.getDeclaredField(attributeName);
                    attribute.setAccessible(true);
                    if (null != attribute) {
                        Object staticValue = attribute.get(null);
                        if (null != staticValue && staticValue.equals(value)) {
                            hasCorrectValue = true;
                        }
                    }
                } catch (NoSuchFieldException e) {
                    // No action needed (hasCorrectValue should stay false)
                } catch (SecurityException e) {
                    /*
                     * In case of any (technical) problems, do not report an error to the students
                     * and accept their submission.
                     */
                    hasCorrectValue = true;
                } catch (ReflectiveOperationException e) {
                    /*
                     * In case of any (technical) problems, do not report an error to the students
                     * and accept their submission.
                     */
                    hasCorrectValue = true;
                }
            }
        }
        return hasCorrectValue;
    }
}
//checkstyle: resume parameter number check
//checkstyle: resume exception type check