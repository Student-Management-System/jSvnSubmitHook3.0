package de.uni_hildesheim.sse.test.suite;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.ClassRegistry;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestFailure;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestResult;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestSuiteSecurityManager;

/**
 * Basis-Implementierung der Hauptklasse, die von jSVNSubmitHook geladen wird. 
 * Test-Klassen muessen von dieser Klasse erben.
 * 
 * @author Adam Krafczyk
 * @author Holger Eichelberger
 */
public abstract class AbstractJavaTestSuite {
    
    // TODO old style with public attributes, keep for now and change to private
    // checkstyle: stop visibility modifier check
    
    /**
     * Die {@link ClassRegistry} um kompilierte Klassen der Hausaufgabe zu laden.
     * 
     * @deprecated use getter, will become private
     */
    @Deprecated()
    public static ClassRegistry classRegistry;
    
    /**
     * Das {@link TestResult} Objekt, mit dem die {@link TestFailure}s
     * uebergeben werden koennen.
     * 
     * @deprecated use getter, will become private
     */
    @Deprecated
    public static TestResult testResult;
    
    /**
     * Der {@link SecurityManager}, der gesetzt werden sollte, wenn Methoden aus
     * der Abgabe ausgeführt werden.
     * 
     * @deprecated use getter, will become private
     */
    @Deprecated
    public static TestSuiteSecurityManager securityManager;

    // checkstyle: resume visibility modifier check

    /**
     * Setzt die {@link ClassRegistry} um die kompilierten Klassen der
     * Hausaufgabe zu laden. Wird vom jSVNSubmitHook aufgerufen.
     * 
     * @param newClassRegistry die {@link ClassRegistry} mit dem die Klassen
     * geladen werden.
     */
    public static void setClassRegistry(ClassRegistry newClassRegistry) {
        classRegistry = newClassRegistry;
    }
    
    /**
     * Setzt das {@link TestResult} Objekt, mit dem die {@link TestFailure}s
     * uebergeben werden koennen.
     * @param testResult Das {@link TestResult} Objekt.
     */
    public static void setTestResult(TestResult testResult) {
        AbstractJavaTestSuite.testResult = testResult;
    }

    /**
     * Gibt die {@link ClassRegistry} um die kompilierten Klassen der
     * Hausaufgabe zu laden zurueck. Wird vom jSVNSubmitHook aufgerufen.
     * 
     * @return die {@link ClassRegistry} mit dem die Klassen
     * geladen werden.
     */
    public static ClassRegistry getClassRegistry() {
        return classRegistry;
    }
    
    /**
     * Gibt das {@link TestResult} Objekt, mit dem die {@link TestFailure}s
     * uebergeben werden koennen.
     * @return Das {@link TestResult} Objekt.
     */
    public static TestResult getTestResult() {
        return AbstractJavaTestSuite.testResult;
    }
    
    /**
     * Gibt das {@link TestSuiteSecurityManager} Objekt, mit dem das Ausführen
     * fremder Methoden gesichert werden soll, zurück.
     * @return Das {@link TestSuiteSecurityManager} Object.
     */
    public static TestSuiteSecurityManager getSecurityManager() {
        return AbstractJavaTestSuite.securityManager;
    }
    
    /**
     * Setzt das {@link TestSuiteSecurityManager} Objekt, mit dem das Ausführen
     * fremder Methoden gesichert werden soll.
     * @param securityManager Das {@link TestSuiteSecurityManager} Object.
     */
    public static void setSecurityManager(
            TestSuiteSecurityManager securityManager) {
        
        AbstractJavaTestSuite.securityManager = securityManager;
    }
    
    /**
     * Ruft eine Methode auf. Setzt den {@link TestSuiteSecurityManager} wenn
     * vorhanden.
     * 
     * @see Method#invoke(Object, Object...)
     * 
     * @param method Die auszuführende Methode.
     * @param obj Das Objekt, dessen Methode ausgeführ wird.
     * @param args Die Parameter des Aufrufs.
     * @return Den Rückgabewert der Methode.
     * 
     * @throws IllegalArgumentException in case of illegal arguments
     * @throws IllegalAccessException in case of illegal access
     * @throws InvocationTargetException if execution of method fails
     * @throws SecurityException if access is not permitted
     */
    public static Object callMethod(Method method, Object obj, Object ... args)
            throws SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        
        if (securityManager != null) {
            securityManager.set();
        }
        
        Object result = null;
        try {
            try {
                result = method.invoke(obj, args);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof SecurityException) {
                    testResult.addTestFailure(new TestFailure(
                            "Programm verletzt die folgende Berechtigung: "
                            + e.getCause().getMessage(), true, null, -1));
                }
                throw e;
            }
        } finally {
            if (securityManager != null) {
                securityManager.unset();
            }
        }
        
        return result;
    }
    
}
