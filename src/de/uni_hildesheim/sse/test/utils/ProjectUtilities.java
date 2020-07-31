package de.uni_hildesheim.sse.test.utils;

import java.lang.reflect.Method;

import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestFailure;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.WrappedClass;
import de.uni_hildesheim.sse.test.suite.AbstractJavaTestSuite;

/**
 * Testet Eigenschaften des gesamten Projets.
 * @author El-Sharkawy
 *
 */
public class ProjectUtilities {
    private static final String MAIN_METHOD_NAME = "main";
    private static final Class<?> MAIN_METHOD_PARAMETER = String[].class;

    /**
     * Vermeiden der Instanziierung, da es sich hier um eine UitlityKlasse handelt.
     */
    private ProjectUtilities() {}
    
    /**
     * Testet das ein Projekt nicht mehr als die maximale Anzahl von Main-Methoden hat und gibt einen Fehler aus,
     * wenn das abgegebene Projekt dagegen verstoesst.
     * @param maxMains Die maximale Anzahl an Main-Methoden, die ein Projekt anthalten darf. Empfohlen wird hier 1.
     */
    public static void assertMaxMainMethods(int maxMains) {
        int nMethods = countMainMethods();
        if (nMethods > maxMains) {
            AbstractJavaTestSuite.getTestResult().addTestFailure(new TestFailure(
                    "Ihr Projekt enthaelt zu viele Klassen mit einer Main-Methode. "
                    + "Erlaubt sind maximal " + maxMains + " Klassen mit einer Main-Methode, "
                    + "gefunden wurden aber " + nMethods + " Klassen. Bitten geben Sie nur die fuer die "
                    + "aktuelle Hausaufgabe relevanten Klassen ab.", true, null, -1));
        }
    }

    /**
     * Testet das ein Projekt mehr als die minimale Anzahl von Main-Methoden hat und gibt einen Fehler aus,
     * wenn das abgegebene Projekt dagegen verstoesst.
     * @param minMains Die minimale Anzahl an Main-Methoden, die ein Projekt anthalten darf. Empfohlen wird hier 1.
     */
    public static void assertMinMainMethods(int minMains) {
        int nMethods = countMainMethods();
        if (nMethods < minMains) {
            AbstractJavaTestSuite.getTestResult().addTestFailure(new TestFailure(
                    "Ihr Projekt enthaelt zu wenige Klassen mit einer Main-Methode. "
                    + "Erlaubt sind minimal " + minMains + " Klassen mit einer Main-Methode, "
                    + "gefunden wurden aber " + nMethods + " Klassen. Bitten fügen Sie geeignete "
                    + "main-Methoden hinzu.", true, null, -1));
        }
    }

    /**
     * Asserts the author comment in the first line.
     */
    public static void assertAuthorComment() {
        assertAuthorComment(null);
    }

    /**
     * Asserts the author comment in the first line.
     * 
     * @param regEx optional regEx applied to each complete class source code, 
     *     may be <b>null</b> for none
     */
    public static void assertAuthorComment(String regEx) {
        for (WrappedClass javaClass : AbstractJavaTestSuite.getClassRegistry().getAllWrappedClasses()) {
            if (null != javaClass.getContent()) {
                String srcFile = javaClass.getFileName();
                String quellCode = javaClass.getContent();
                if (!quellCode.startsWith("//") && !quellCode.startsWith("/*")) {
                    boolean ok = true;
                    if (null != regEx) {
                        ok = quellCode.matches(regEx);
                    }
                    if (!ok) {
                        AbstractJavaTestSuite.getTestResult().addTestFailure(new TestFailure(
                                "Angabe der Autoren fehlt.", true, srcFile, 1));
                    }
                }
            }
        }
    }

    /**
     * Gibt die Anzahl von main-Methoden im Projekt zurück.
     * 
     * @return die Anzahl von main-Methoden
     */
    public static int countMainMethods() {
        int nMethods = 0;
        for (WrappedClass javaClass : AbstractJavaTestSuite.getClassRegistry().getAllWrappedClasses()) {
            Class<?> clazz = javaClass.getClass();
            if (null != clazz) {
                Method method;
                try {
                    method = clazz.getMethod(MAIN_METHOD_NAME, MAIN_METHOD_PARAMETER);
                    if (null != method) {
                        nMethods++;
                    }
                } catch (ReflectiveOperationException e) {
                    // No action needed
                }
            }
        }
        return nMethods;
    }
    
    /**
     * Zaehlt das Vorkommen des angegebenen <tt>code</tt>-Fragments im Quellcode des gesamten Projekts.
     * @param code Unerwuenschter Code Ausschnitt, z. B. <tt>for (</tt>.
     * @return Die Anzahl wie hauefig der gesuchte Code innerhalb des gesamten Projekts ausserhalb von Kommentaren
     *     vorkommt.
     */
    public static int countCodeAbsolute(String code) {
        int countCode = 0;
        for (WrappedClass javaClass : AbstractJavaTestSuite.getClassRegistry().getAllWrappedClasses()) {
            countCode += CodeUtilities.countCode(javaClass, code);
        }
        
        return countCode;
    }
}
