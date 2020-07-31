package de.uni_hildesheim.sse.javaSvnHooks.tests.junit.testSuiteInterface;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Utilityklasse um Konsoleeingaben und -ausgaben zu simulieren/abzufangen.
 * @author El-Sharkawy
 *
 */
public class Console {

    /**
     *  Singletoninstanz dieser Console.
     *  Zum Beginn sollte {@link #aktivieren()} aufgerufen werden.
     *  Nach Beendigung der Tests sollte {@link #deaktivieren()} aufgerufen
     *  werden.
     */
    public static final Console INSTANCE = new Console();
    
    private PrintStream systemOut;
    private PrintStream systemErr;
    private InputStream systemIn;
    
    private SystemInStream inBuffer;
    private ByteArrayOutputStream outBuffer;
    private ByteArrayOutputStream errBuffer;
    
    private boolean aktiv;
    
    /**
     * Singleton-Konstruktur.
     */
    private Console() {
        aktiv = false;
        
        // Originale System-Streams
        systemOut = System.out;
        systemErr = System.err;
        systemIn = System.in;
        
        // Stream fuer Benutzerinteraktionen
        inBuffer = new SystemInStream();
    }
    
    /**
     * Aktiviert die Simulation der Konsole.
     */
    public void aktivieren() {
        if (!aktiv) {
            aktiv = true;
            resetOut();
            resetErr();
            inBuffer.reset();
            System.setIn(inBuffer);
        }
    }
    
    /**
     * Beendet die Simulation der Konsole.
     */
    public void deaktivieren() {
        if (aktiv) {
            aktiv = false;
            System.setOut(systemOut);
            System.setErr(systemErr);
            System.setIn(systemIn);
        }
    }
    
    /**
     * Gibt einen Text wie {@link System#in} in die Konsole ein.
     * Nach dem Aufruf dieser Methode sollte zu einem spaeteren Zeitpunkt
     * die {@link #deaktivieren()} aufgerufen werden.
     * @param eingabeText Der Text der in {@link System#in} eingegeben werden
     *     soll. Mehrere Eingaben sollten mit mehreren Aufrufen eingegeben
     *     werden.
     */
    public void eingabe(String eingabeText) {
        inBuffer.input(eingabeText);
    }
    
    /**
     * Liest die bisherigen Ausgaben von {@link System#out}.
     * @return Die bisherigen Ausgaben auf der Konsole oder <tt>null</tt>
     *     wenn {@link #aktivieren()} nicht aufgerufen wurde.
     */
    public String leseOut() {
        return outBuffer != null ? outBuffer.toString() : null;
    }
    
    /**
     * Liest die bisherigen Ausgaben von {@link System#err}.
     * @return Die bisherigen Ausgaben auf der Konsole oder <tt>null</tt> wenn
     *     {@link #aktivieren()} nicht aufgerufen wurde.
     */
    public String leseError() {
        return errBuffer != null ? errBuffer.toString() : null;
    }
    
    /**
     * Löscht die bisherigen Eingaben auf der Konsole.
     * Eingaben die vor diesen Aufruf gemacht wurden, werden verworfen.
     * Mit {@link #leseOut()} werden nur noch Eingaben gelesen, welche nach
     * diesem Aufruf gemacht wurden.
     */
    public void resetOut() {
        if (aktiv) {
            outBuffer = new ByteArrayOutputStream();
            PrintStream simOut = new PrintStream(outBuffer);
            System.setOut(simOut);
        }
    }
    
    /**
     * Löscht die bisherigen Eingaben auf der Konsole.
     * Eingaben die vor diesen Aufruf gemacht wurden, werden verworfen.
     * Mit {@link #leseError()} werden nur noch Eingaben gelesen, welche nach
     * diesem Aufruf gemacht wurden.
     */
    public void resetErr() {
        if (aktiv) {
            errBuffer = new ByteArrayOutputStream();
            PrintStream simErr = new PrintStream(errBuffer);
            System.setErr(simErr);
        }
    }
}
