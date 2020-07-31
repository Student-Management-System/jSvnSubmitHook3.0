package de.uni_hildesheim.sse.javaSvnHooks.tests.javac;

import java.io.PrintStream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import de.uni_hildesheim.sse.javaSvnHooks.util.XmlUtilities;

/**
 * A class for converting java compiler diagnostic objects to
 * textual output.
 * 
 * @author Holger Eichelberger
 * @since 1.20
 * @version 1.20
 */
public class PrintDiagnostics {

    /**
     * Prevents this class from being instantiated from outside.
     * 
     * @since 1.20
     */
    private PrintDiagnostics() {
    }
    
    /**
     * Prints the diagnostics of the given collator.
     * 
     * @param out The {@link PrintStream} to write the results to.
     * @param diagnostics the diagnostics of the given collator
     * @param xml print the diagnostics as XML elements
     * 
     * @since 1.00
     */
    public static void printDiagnostics(PrintStream out,
        DiagnosticCollector<? extends JavaFileObject> diagnostics, 
        boolean xml) {
        for (Diagnostic<? extends JavaFileObject> diagnostic 
            : diagnostics.getDiagnostics()) {
            String type;
            switch (diagnostic.getKind()) {
            case ERROR:
                type = "error";
                break;
            case MANDATORY_WARNING:
                type = "warning";
                break;
            case NOTE:
                type = null;
                break;
            case WARNING:
                type = "warning";
                break;
            case OTHER:
                type = null;
                break;
            default:
                type = null;
                break;
            }
            if (null != type) {
                if (xml) {
                    out.print("<message tool=\"javac\"");
                    out.print("type = \"" + type + "\"");
                    out.print("file = \"" 
                        + XmlUtilities.xmlify(diagnostic.getSource().getName()) 
                        + "\"");
                    out.print("message = \"" 
                        + XmlUtilities.xmlify(diagnostic.getMessage(null)) 
                        + "\"");
                    out.println("line = \"" 
                        + diagnostic.getLineNumber() + "\">");
                    if (Diagnostic.NOPOS != diagnostic.getColumnNumber()) {
                        out.println("<example position=\"" 
                            + diagnostic.getColumnNumber() + "\">");
                        if (null != diagnostic.getCode()) {
                            out.println(
                                XmlUtilities.xmlify(diagnostic.getCode()));
                        }
                        out.println("</example>");
                    }
                    out.println("</message>");
                } else {
                    out.print(
                        diagnostic.getSource().getName());
                    out.print(":");
                    out.print(
                        String.valueOf(diagnostic.getLineNumber()));
                    out.print(":");
                    
                    out.println(
                        toOneLine(diagnostic.getMessage(null)));
                }
            }
        }
    }

    /**
     * Removes all line breaks.
     * 
     * @param text the text to be processed
     * @return the line breaks to be removed
     * 
     * @since 1.00
     */
    private static String toOneLine(String text) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                c = ' ';
            } else if (c == '\r') {
                c = 0;
            }
            if (c != 0) {
                buf.append(c);
            }
        }
        return buf.toString();
    }
    
}
