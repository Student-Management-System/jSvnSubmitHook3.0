package de.uni_hildesheim.sse.javaSvnHooks.tests.javac;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.ITestMessageFilter;
import de.uni_hildesheim.sse.javaSvnHooks.util.XmlUtilities;

/**
 * Implements a test filter according to the message format 
 * of the Java compiler. Currently, this filter does not
 * consider warnings. For the output XML format see 
 * {@link ITestMessageFilter}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class JavacTestFilter implements ITestMessageFilter {

    /**
     * Stores the state while parsing the tool output.
     * 1 denotes the example part of a message where the
     * error is indicated by a "^", 0 denotes the remainder
     * (beginning) of the message.
     * 
     * @since 1.00
     */
    private int errorWarningMessageState = 0;

    /**
     * Stores a message buffer so that multiple lines 
     * can be processed.
     * 
     * @since 1.00
     */
    private StringBuilder buffer = new StringBuilder();

    /**
     * Stores if the last message had an example.
     * 
     * @since 1.00
     */
    private boolean hadExample = false;

    /**
     * Stores if the currently processed message
     * was the first message.
     * 
     * @since 1.00
     */
    private boolean firstMsg = true;
    
    /**
     * Stores the global configuration instance.
     * 
     * @since 1.00
     */
    private Configuration configuration;
    
    private PathConfiguration pathConfiguration;

    /**
     * Creates a new filter instance.
     * 
     * @param pathConfiguration the configuration data to be considered
     * 
     * @since 1.00
     */
    public JavacTestFilter(PathConfiguration pathConfiguration) {
        this.pathConfiguration = pathConfiguration;
        this.configuration = pathConfiguration.getGlobalConfiguration();
    }

    /**
     * Filters a given <code>line</code>.
     * 
     * @param lineNr the line number of <code>line</code> in the
     *        entire output of the test
     * @param line the line with number <code>lineNr</code> of the 
     *        test output
     * @return the filtered message in XML format
     * 
     * @since 1.00
     */
    public String filterMessage(int lineNr, String line) {
        final String input = line;
        boolean handled = false;
        switch (errorWarningMessageState) {
        case 0:
            line = handleErrorWarningMessageStateEquals0(line);
            handled = line != null;
            break;
        case 1:
            line = handleErrorWarningMessageStateEquals1(line);
            handled = true;
            break;
        default:
            break;
        }
        if (null == line && 0 == errorWarningMessageState) {
            if (!hadExample && !firstMsg) {
                line = "  </message>\n";
            }
        }
        if (configuration.produceXmlOutput()) {
            String out = input;
            if (!handled) {
                out += " |***";
            }
            Logger.INSTANCE.log(out);
        }
        return line;
    }

    /**
     * Called by filterMessage() if errorWarningMessageState is 1.
     * @param line The line to be filtered.
     * @return The filtered line.
     */
    private String handleErrorWarningMessageStateEquals1(String line) {
        if (line.trim().equals("^")) {
            int pos = line.indexOf("^");
            line =
                "    <example position=\"" + pos + "\">\n"
                + XmlUtilities.xmlify(buffer.toString())
                + "    </example>\n  </message>";
            errorWarningMessageState = 0;
            buffer = new StringBuilder();
            hadExample = true;
        } else {
            if (null != line) {
                buffer.append(line);
                buffer.append("\n");
            }
            line = null;
        }
        return line;
    }

    /**
     * Called by filterMessage() if errorWarningMessageState is 0.
     * @param line The line to be filtered.
     * @return The filtered line.
     */
    private String handleErrorWarningMessageStateEquals0(String line) {
        int dotPos = line.indexOf(".");
        if (dotPos > 0) {
            int firstColonPos = line.indexOf(":", dotPos + 1);
            if (firstColonPos > 0) {
                int sndColonPos = line.indexOf(":", firstColonPos + 1);
                if (sndColonPos > 0) {
                    String result = "";
                    if (!hadExample && !firstMsg) {
                        result = "  </message>\n";
                    }
                    hadExample = false;
                    errorWarningMessageState = 1;
                    result +=
                        "  <message tool=\"javac\" type=\"error\" "
                        + "file=\""
                        + XmlUtilities.xmlify(pathConfiguration.relativizeFileName(
                                line.substring(0, firstColonPos)));
                    result =
                        result
                            + "\" line=\""
                            + line.substring(firstColonPos + 1,
                                sndColonPos);
                    line =
                        result
                            + "\" message=\""
                            + XmlUtilities.xmlify(line.substring(sndColonPos + 1)
                                .trim()) + "\">";
                    firstMsg = false;
                } else {
                    line = null;
                }
            } else {
                line = null;
            }
        } else {
            if (line.startsWith("javac:")) {
                String result = "  <message tool=\"javac\" type=\"error\" ";
                line =
                    result
                        + "message=\""
                        + XmlUtilities.xmlify(line) + "\"/>";
            } else {
                line = null;
            }
        }
        return line;
    }

    @Override
    public String done() {
        return null;
    }

}