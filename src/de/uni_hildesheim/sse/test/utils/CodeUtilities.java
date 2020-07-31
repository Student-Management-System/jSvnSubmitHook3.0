package de.uni_hildesheim.sse.test.utils;

import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.TestFailure;
import de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface.WrappedClass;
import de.uni_hildesheim.sse.test.suite.AbstractJavaTestSuite;

/**
 * Statische Hilfsfunktionen um die Abgaben zu testen. Testfaelle koennen hiervon beliebig Gebrauch machen. 
 * @author El-Sharkawy
 *
 */
public class CodeUtilities {
    /**
     * Regulaerer Ausdruck fuer einen Zeilenumbruch. Sollte in
     * {@link String#split(String)} verwendet werden, um platformunabhaengig Windows und Mac/Linux Abgaben korrekt
     * behandeln zu koennen. 
     */
    public static final String LINEBREAK = "\\r?\\n";
    
    /**
     * Vermeiden der Instanziierung, da es sich hier um eine UitlityKlasse handelt.
     */
    private CodeUtilities() {}

    /**
     * Prueft ob der Code eine nicht erlaubte Funktion/Aufruf enthaelt.
     * <b>Vorsicht:</b> Durchsucht ebenfalls Kommentare.
     * @param javaClass Eine Java Klasse der Abgabe, welche getestet werden soll. (Ggf. bietet sich hier eine Schleife
     *     ueber alle Klassen an).
     * @param nichtErlaubterCode Eine Funktion, die die Studis nicht verwenden duerfen, z.B. " for (" oder " if (".
     *     Der Code Ausschnitt sollte moeglichst so gross gewaehlt werden, dass nicht versehentlich richtiger Code
     *     abgelehnt wird.
     * @param fehlermeldung Die Fehlermeldung, die dem Studenten angezeigt werden soll, wenn dieser den Code verwendet.
     */
    public static void unerlaubterCode(WrappedClass javaClass, String nichtErlaubterCode, String fehlermeldung) {
        String code = null != javaClass ? javaClass.getContent() : null;
        String srcFile = null != javaClass ? javaClass.getFileName() : null;
        
        if (null != code && code.contains(nichtErlaubterCode)) {
            AbstractJavaTestSuite.getTestResult().addTestFailure(new TestFailure(fehlermeldung, true, srcFile, -1));
        }
    } 
    
    /**
     * Zaehlt das Vorkommen des angegebenen <tt>code</tt>-Fragments in Quellcode der angegebenen {@link WrappedClass}.
     * @param javaClass Die zu untersuchende Java-Klasse eines abgegebenen Projekts.
     * @param code Unerwuenschter Code Ausschnitt, z. B. <tt>for (</tt>.
     * @return Die Anzahl wie hauefig der gesuchte Code innerhalb der spezifierten Klasse ausserhalb von Kommentaren
     *     vorkommt.
     */
    public static int countCode(WrappedClass javaClass, String code) {
        String srcCode = null != javaClass ? javaClass.getContent() : null;
        int codeCount = 0;
        
        if (null != srcCode) {
            String[] lines = srcCode.split(LINEBREAK);
            if (null != lines) {
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (!isCommentLine(line) && line.contains(code)) {
                        codeCount++;
                    }
                }
            }
        }
        
        return codeCount;
    }
    
    /**
     * Tests whether the given code fragment belongs to a comment or not. The given code fragment may be trimmed or not.
     * @param codeLine A code snippet to test.
     * @return <tt>true</tt>If the given code line is a comment, <tt>false</tt> otherwise.
     */
    public static boolean isCommentLine(String codeLine) {
        return codeLine.matches("^\\s*(\\*|/\\*|//).*");
    }
    
    /**
     * Liefert zeilenweise den Quellcode der gegebenen {@link WrappedClass} zurueck.
     * @param javaClass eine Java-Klasse der Abgabe, fuer die der Quelltext zurueck gegeben werden soll.
     * @return Die einzelnen Zeilen des Quellcodes oder <tt>null</tt> wenn dieser nicht verfuegbar sein sollte.
     */
    public static String[] codeLines(WrappedClass javaClass) {
        String srcCode = null != javaClass ? javaClass.getContent() : null;
        String[] lines = null != srcCode ? srcCode.split(LINEBREAK) : null;
        
        return lines;
    }
    
    /**
     * Checks for a comment in the first line of each file.
     */
    public static void commentInFirstLine() {
        for (WrappedClass javaClass : AbstractJavaTestSuite.getClassRegistry().getAllWrappedClasses()) {
            if (null != javaClass.getContent()) {
                String srcFile = javaClass.getFileName();
                String quellCode = javaClass.getContent();
                if (!quellCode.startsWith("//") && !quellCode.startsWith("/*")) {
                    AbstractJavaTestSuite.getTestResult().addTestFailure(new TestFailure(
                        "Angabe der Autoren fehlt.", true, srcFile, 1));
                }
            }
        }
    }
    
}
