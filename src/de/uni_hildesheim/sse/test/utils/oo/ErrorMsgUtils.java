package de.uni_hildesheim.sse.test.utils.oo;

import java.util.Collection;
import java.util.Iterator;

import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestFailure;
import de.uni_hildesheim.sse.test.suite.AbstractJavaTestSuite;

/**
 * Static methods for providing meaningful error messages to students after an error was detected in a submission.
 * These methods do <b>not</b> perform any tests, this must be done before.
 * @author El-Sharkawy
 *
 */
//checkstyle: stop parameter number check
public class ErrorMsgUtils {
    
    /**
     * Creates a meaningful error message if the specified attribute is missing.
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     * @param srcFile Optional: The file where the error was detected.
     *     Should be <tt>null</tt>, if no concrete file is known.
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
     */
    static void attributeMissing(boolean isMandatory, String srcFile, String packageName, String className,
        String attributeName, Visibility visibility, Modifier modifier, boolean shallBeStatic, Class<?> type) {
        
        StringBuffer errorMsg = isMandatory ? new StringBuffer("Fehler: ") : new StringBuffer("Warnung: ");
        errorMsg.append("Es wurde nicht ein in der Hausaufgabe spezifiziertes"
            + " Attribut gefunden. Bei dem fehlenden Attribut handelt es sich um das Attribut \"");
        
        // public, private, protected
        if (null != visibility && Visibility.PACKAGE != visibility) {
            errorMsg.append(visibility.name().toLowerCase());
            errorMsg.append(" ");
        }
        
        if (shallBeStatic) {
            errorMsg.append("static ");
        }
        
        if (null != modifier) {
            errorMsg.append(modifier.name().toLowerCase());
            errorMsg.append(" ");
        }
        
        errorMsg.append(getSimpleNameSafe(type));
        errorMsg.append(" ");
        
        errorMsg.append(attributeName);
        
        errorMsg.append("\" in der Klasse: \"");
        
        if (null != packageName) {
            errorMsg.append(packageName);
            errorMsg.append(".");
        }
        errorMsg.append(className);
        errorMsg.append("\".");
        
        AbstractJavaTestSuite.getTestResult().addTestFailure(
            new TestFailure(errorMsg.toString(), isMandatory, srcFile, -1));
    }
    
    /**
     * Creates a meaningful error message if the specified attribute is missing.
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     * @param srcFile Optional: The file where the error was detected.
     *     Should be <tt>null</tt>, if no concrete file is known.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param attributeName The name of the specified attribute inside the class.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param type The desired type of the attribute. Must not be <tt>null</tt>.
     *     This will also allow inherited class of the specified <tt>type</tt>.
     * @param value The value which was specified inside the homework.
     */
    static void staticValueMissing(boolean isMandatory, String srcFile, String packageName, String className,
        String attributeName, Visibility visibility, Modifier modifier, Class<?> type, Object value) {
        
        StringBuffer errorMsg = isMandatory ? new StringBuffer("Fehler: ") : new StringBuffer("Warnung: ");
        errorMsg.append("Es wurde nicht ein in der Hausaufgabe spezifiziertes"
            + " Attribut samt Wert gefunden. Bei dem fehlenden Attribut und dessen Wert handelt es sich um das"
            + " Attribut \"");
        
        // public, private, protected
        if (null != visibility && Visibility.PACKAGE != visibility) {
            errorMsg.append(visibility.name().toLowerCase());
            errorMsg.append(" ");
        }
        
        errorMsg.append("static ");
        
        if (null != modifier) {
            errorMsg.append(modifier.name().toLowerCase());
            errorMsg.append(" ");
        }
        
        errorMsg.append(getSimpleNameSafe(type));
        errorMsg.append(" ");
        errorMsg.append(attributeName);
        errorMsg.append(" = ");
        errorMsg.append(value.toString());
        
        errorMsg.append(";\" in der Klasse: \"");
        
        if (null != packageName) {
            errorMsg.append(packageName);
            errorMsg.append(".");
        }
        errorMsg.append(className);
        errorMsg.append("\".");
        
        AbstractJavaTestSuite.getTestResult().addTestFailure(
            new TestFailure(errorMsg.toString(), isMandatory, srcFile, -1));
    }

    /**
     * Creates a meaningful error message if the specified class is missing.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     */
    static void classMissing(boolean isMandatory, String packageName, String className, Visibility visibility,
        Modifier modifier) {
        
        StringBuffer errorMsg = isMandatory ? new StringBuffer("Fehler: ") : new StringBuffer("Warnung: ");
        errorMsg.append("Es wurde nicht die in der Hausaufgabe spezifizierte"
            + " Klasse gefunden. Bei der fehlenden Klasse handelt es sich um \"");
        
        // public, private, protected
        if (null != visibility && Visibility.PACKAGE != visibility) {
            errorMsg.append(visibility.name().toLowerCase());
            errorMsg.append(" ");
        }
        
        // abstract, final (cannot happen that both are specified at the same time)
        if (null != modifier) {
            errorMsg.append(modifier.name().toLowerCase());
            errorMsg.append(" ");
        }
        
        // Class name
        errorMsg.append(className);
        errorMsg.append("\" ");
        
        // Package name
        if (null != packageName) {
            errorMsg.append("im Paket \"");
            errorMsg.append(packageName);
            errorMsg.append("\" ");
        }
        
        if (null != visibility && Visibility.PACKAGE == visibility) {
            errorMsg.append("mit Paketsichtbarkeit (nicht public, private oder protected) ");
        }
        
        // Replace last whitespace by a dot.
        errorMsg.deleteCharAt(errorMsg.length() - 1);
        errorMsg.append(".");
        
        AbstractJavaTestSuite.getTestResult().addTestFailure(
            new TestFailure(errorMsg.toString(), isMandatory, null, -1));
    }
    
    /**
     * Creates a meaningful error message if the specified interface is missing (or is a class).
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param interfaceName The simple name of the interface
     *     (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     */
    static void interfaceMissing(boolean isMandatory, String packageName, String interfaceName, Visibility visibility) {
        
        StringBuffer errorMsg = isMandatory ? new StringBuffer("Fehler: ") : new StringBuffer("Warnung: ");
        errorMsg.append("Es wurde nicht das in der Hausaufgabe spezifizierte"
                + " Interface gefunden. Bei dem fehlenden Interface handelt es sich um \"");
        
        // public, private, protected
        if (null != visibility && Visibility.PACKAGE != visibility) {
            errorMsg.append(visibility.name().toLowerCase());
            errorMsg.append(" ");
        }
        
        // Class name
        errorMsg.append(interfaceName);
        errorMsg.append("\" ");
        
        // Package name
        if (null != packageName) {
            errorMsg.append("im Paket \"");
            errorMsg.append(packageName);
            errorMsg.append("\" ");
        }
        
        if (null != visibility && Visibility.PACKAGE == visibility) {
            errorMsg.append("mit Paketsichtbarkeit (nicht public, private oder protected) ");
        }
        
        // Replace last whitespace by a dot.
        errorMsg.deleteCharAt(errorMsg.length() - 1);
        errorMsg.append(".");
        
        AbstractJavaTestSuite.getTestResult().addTestFailure(
            new TestFailure(errorMsg.toString(), isMandatory, null, -1));
    }
    
    /**
     * Creates a meaningful error message if the specified inheritance relation is missing.
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     * @param srcFile Optional: The file where the error was detected.
     *     Should be <tt>null</tt>, if no concrete file is known.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param superPackageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.<br/>
     *     &nbsp;&nbsp;<b>Attention:</b> If packageName starts with <tt>java</tt>, this method will check whether the
     *     given class extends a Java API class.
     * @param superClassName The name of the class from the super class.
     */
    static void inheritanceMissing(boolean isMandatory, String srcFile, String packageName, String className,
        String superPackageName, String superClassName) {
        
        String childName = (null != packageName) ? "\"" + packageName + "." + className + "\""
            : "\"" + className + "\"";
        String superName = (null != superPackageName) ? "\"" + superPackageName + "." + superClassName + "\""
            : "\"" + superClassName + "\"";
        
        StringBuffer errorMsg = isMandatory ? new StringBuffer("Fehler: ") : new StringBuffer("Warnung: ");
        errorMsg.append("Fehlende Vererbung zwischen ");
        errorMsg.append(childName);
        errorMsg.append(" und ");
        errorMsg.append(superName);
        errorMsg.append(". Es wurde in der Klasse ");
        errorMsg.append(childName);
        errorMsg.append(" folgende Definition erwartet: \"");
        errorMsg.append(className);
        errorMsg.append(" extends ");
        errorMsg.append(superClassName);
        errorMsg.append("\".");
        
        AbstractJavaTestSuite.getTestResult().addTestFailure(
            new TestFailure(errorMsg.toString(), isMandatory, srcFile, -1));
    }
    
    /**
     * Creates a meaningful error message if the specified interface was not implemented.
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     * @param srcFile Optional: The file where the error was detected.
     *     Should be <tt>null</tt>, if no concrete file is known.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param interfacePackageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.<br/>
     *     &nbsp;&nbsp;<b>Attention:</b> If packageName starts with <tt>java</tt>, this method will check whether the
     *     given class extends a Java API class.
     * @param interfaceClassName The name of the class from the super class.
     */
    static void interfaceNotImplemented(boolean isMandatory, String srcFile, String packageName, String className,
        String interfacePackageName, String interfaceClassName) {
        
        String childName = (null != packageName) ? "\"" + packageName + "." + className + "\""
            : "\"" + className + "\"";
        String interfaceName = (null != interfacePackageName) ? "\"" + interfacePackageName + "." + interfaceClassName
            + "\"" : "\"" + interfaceClassName + "\"";
        
        StringBuffer errorMsg = isMandatory ? new StringBuffer("Fehler: ") : new StringBuffer("Warnung: ");
        errorMsg.append("Das geforderte Interface ");
        errorMsg.append(interfaceName);
        errorMsg.append(" wurde in Klasse ");
        errorMsg.append(childName);
        errorMsg.append(" nicht implementiert. Es wurde in der Klasse ");
        errorMsg.append(childName);
        errorMsg.append(" folgende Definition erwartet: \"");
        errorMsg.append(className);
        errorMsg.append(" implements ");
        errorMsg.append(interfaceClassName);
        errorMsg.append("\".");
        
        AbstractJavaTestSuite.getTestResult().addTestFailure(
            new TestFailure(errorMsg.toString(), isMandatory, srcFile, -1));
    }
    
    /**
     * Creates a meaningful error message if the specified attribute is missing.
     * @param srcFile Optional: The file where the error was detected.
     *     Should be <tt>null</tt>, if no concrete file is known.
     * @param clazz The class where the setter was found.
     * @param attributeName The name of the specified attribute inside the class.
     */
    static void isNotReadonlyAttribute(String srcFile, Class<?> clazz, String attributeName) {
        
        StringBuffer errorMsg = new StringBuffer("Warnung: Fuer dass Attribut \"");
        errorMsg.append(attributeName);
        errorMsg.append("\" in Klasse \"");
        errorMsg.append(clazz.getCanonicalName());
        errorMsg.append("\" wurde ein public Setter gefunden. Das Attribute wurde jedoch als {readOnly} spezifiziert, "
            + "bzw. es wurde kein Setter spezifiziert. Es wird empfohlen diesen Setter zu loeschen oder die "
            + "Sichtbarkeit auf private zu reduzieren. Andernfalls wird Ihr Projekt hoechstwahrscheinlich gegen "
            + "die geforderte Spezifikation verstossen.");
        
        AbstractJavaTestSuite.getTestResult().addTestFailure(
            new TestFailure(errorMsg.toString(), false, srcFile, -1));
    }
    
    /**
     * Creates a meaningful error message if the specified method is missing.
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     * @param srcFile Optional: The file where the error was detected.
     *     Should be <tt>null</tt>, if no concrete file is known.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     * @param methodName The name of the specified method inside the class.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     * @param modifier The specified modifier (abstract or final) or <tt>null</tt> if no modifier was specified by us.
     * @param shallBeStatic <tt>true</tt> if the method shall be static, <tt>false</tt> otherwise.
     * @param exceptions Optional collection of specified exceptions which shall be thrown. Will be ignored if it is
     *     <tt>null</tt>.
     * @param returnType The desired return type or <tt>null</tt> if the method shall be a void method.
     * @param parameterTypes The parameters of the constructor.
     */
    static void methodMissing(boolean isMandatory, String srcFile, String packageName, String className,
        String methodName, Visibility visibility, Modifier modifier, boolean shallBeStatic, Class<?> returnType,
        Collection<Class<?>> exceptions, Class<?>... parameterTypes) {
        
        StringBuffer errorMsg = isMandatory ? new StringBuffer("Fehler: ") : new StringBuffer("Warnung: ");
        errorMsg.append("Es wurde nicht die in der Hausaufgabe spezifizierte"
            + " Methode gefunden. Bei der fehlenden Methode handelt es sich um die Methode \"");
        
        // public, private, protected
        if (null != visibility && Visibility.PACKAGE != visibility) {
            errorMsg.append(visibility.name().toLowerCase());
            errorMsg.append(" ");
        }
        
        if (shallBeStatic) {
            errorMsg.append("static ");
        }
        
        if (null != modifier) {
            errorMsg.append(modifier.name().toLowerCase());
            errorMsg.append(" ");
        }
        
        if (null != returnType) {
            errorMsg.append(getSimpleNameSafe(returnType));
            errorMsg.append(" ");
        } else {
            errorMsg.append("void ");
        }
        
        errorMsg.append(methodName);
        
        errorMsg.append("(");
        createParemeterList(errorMsg, parameterTypes);
        errorMsg.append(")");

        if (null != exceptions) {
            errorMsg.append(" throws ");
            Iterator<Class<?>> itr = exceptions.iterator();
            while (itr.hasNext()) {
                Class<?> exception = itr.next();
                if (null != exception) {
                    errorMsg.append(getSimpleNameSafe(exception));
                    if (itr.hasNext()) {
                        errorMsg.append(", ");
                    }
                }
            }
        }
        errorMsg.append("\" in der Klasse: \"");
        
        if (null != packageName) {
            errorMsg.append(packageName);
            errorMsg.append(".");
        }
        errorMsg.append(className);
        errorMsg.append("\".");
        
        AbstractJavaTestSuite.getTestResult().addTestFailure(
            new TestFailure(errorMsg.toString(), isMandatory, srcFile, -1));
    }

    /**
     * Creates the parameter list of a method/constructor.
     * @param errorMsg the current error message which is currently built to display to the user.
     * @param parameterTypes The parameters of the constructor/method.
     */
    private static void createParemeterList(StringBuffer errorMsg, Class<?>... parameterTypes) {
        if (null != parameterTypes && parameterTypes.length > 0 && parameterTypes[0] != null) {
            errorMsg.append(getSimpleNameSafe(parameterTypes[0]));
            errorMsg.append(" parameter1");
            for (int i = 1, n = parameterTypes.length; i < n; i++) {
                errorMsg.append(", ");
                errorMsg.append(getSimpleNameSafe(parameterTypes[i]));
                errorMsg.append(" parameter");
                errorMsg.append(i + 1);
            }
        }
    }
    
    /**
     * Creates a meaningful error message if the specified method is missing.
     * @param isMandatory <tt>true</tt> reported errors will block the submission, <tt>false</tt>
     *     found errors will be reported as warnings.
     * @param srcFile Optional: The file where the error was detected.
     *     Should be <tt>null</tt>, if no concrete file is known.
     * @param packageName Optional package name specification. Can be <tt>null</tt>. Package name check will be
     *     case sensitive and must be in java syntax (e.g. <tt>java.util</tt>) if not <tt>null</tt>.
     *     If it is <tt>null</tt> this method returns <tt>true</tt> if there exist
     *     at least one class which fulfills the given specification.
     * @param className The simple name of the class (must <b>not</b> contain package names or be empty/<tt>null</tt>).
     *     This name is also used as constructor name.
     * @param visibility The specified visibility or <tt>null</tt> if no visibility was specified by us.
     *     This will also inherited class of the specified <tt>returnType</tt> as a return type.
     * @param parameterTypes The parameters of the method.
     */
    static void constructorMissing(boolean isMandatory, String srcFile, String packageName, String className,
        Visibility visibility, Class<?>... parameterTypes) {
        
        StringBuffer errorMsg = isMandatory ? new StringBuffer("Fehler: ") : new StringBuffer("Warnung: ");
        errorMsg.append("Es wurde nicht der in der Hausaufgabe spezifizierte Konstruktor gefunden. "
            + "Bei dem fehlenden Konstruktor handelt es sich um den Konstruktor \"");
        
        // public, private, protected
        if (null != visibility && Visibility.PACKAGE != visibility) {
            errorMsg.append(visibility.name().toLowerCase());
            errorMsg.append(" ");
        }
        
        errorMsg.append(className);
        
        errorMsg.append("(");
        createParemeterList(errorMsg, parameterTypes);
        errorMsg.append(")\" in der Klasse: \"");
        
        if (null != packageName) {
            errorMsg.append(packageName);
            errorMsg.append(".");
        }
        errorMsg.append(className);
        errorMsg.append("\".");
        
        AbstractJavaTestSuite.getTestResult().addTestFailure(
            new TestFailure(errorMsg.toString(), isMandatory, srcFile, -1));
    }
    
    /**
     * Returns the simple name of a class considering that the given class may be <b>null</b>.
     * 
     * @param type the type to return the simple name for
     * @return the name or a constant string if <code>type</code> is <b>null</b>
     */
    private static String getSimpleNameSafe(Class<?> type) {
        return type != null ? type.getSimpleName() : "<expected but not implemented type";
    }

}
//checkstyle: resume parameter number check