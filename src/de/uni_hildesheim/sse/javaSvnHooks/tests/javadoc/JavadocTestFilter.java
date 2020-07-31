package de.uni_hildesheim.sse.javaSvnHooks.tests.javadoc;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.ITestMessageFilter;
import de.uni_hildesheim.sse.javaSvnHooks.util.MessageUtils;
import de.uni_hildesheim.sse.javaSvnHooks.util.XmlUtilities;

/**
 * Implements a test filter according to the message format 
 * of the Javadoc/checkstyle program. Currently, this filter 
 * does not consider warnings. For the output XML format see 
 * {@link ITestMessageFilter}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class JavadocTestFilter implements ITestMessageFilter {
    
    protected static class StatusPrefix {
        private String prefix;
        private String type;

        /**
         * Creates a status prefix.
         * 
         * @param prefix the prefix to react on
         * @param type the respective message type
         */
        public StatusPrefix(String prefix, String type) {
            this.prefix = prefix;
            this.type = type;
        }
        
        /**
         * Returns the message type.
         * 
         * @return the message type
         */
        public String getType() {
            return type;
        }
        
        /**
         * Does {@code line} matches the prefix?
         * 
         * @param line the line to check
         * @return {@code true} for match, {@code false} else
         */
        public boolean matches(String line) {
            return line.startsWith(prefix) && line.length() > prefix.length();
        }

        /**
         * Handle the line. Shall be called only if {@link #matches(String)} 
         * is {@code true}.
         * 
         * @param line the line to handle
         * @return the modified line
         */
        public String handle(String line) {
            return line.substring(prefix.length(), line.length()).trim();
        }
        
    }

    /**
     * Stores the global configuration instance.
     * 
     * @since 1.00
     */
    private Configuration configuration;
    
    private PathConfiguration pathConfiguration;
    
    private static final StatusPrefix[] statusPrefix = {
        new StatusPrefix("error -", MessageUtils.TYPE_ERROR),
        new StatusPrefix("warning -", MessageUtils.TYPE_WARNING)};

    /**
     * Stores if an error was detected so far.
     * 
     * @since 1.00
     */
    private boolean errorDetected = false;

    /**
     * Stores whether errors and warnings should be distinguished 
     * (depends on the concrete output of the tool the messages 
     * are parsed for) or if each message is considered
     * as an error.
     * 
     * @since 1.00
     */
    private boolean checkType = false;

    /**
     * Stores the tool using this filter.
     * 
     * @since 1.00
     */
    private String tool;
    
    /**
     * Creates a new filter.
     * 
     * @param pathConfiguration the path configuration instance
     * @param tool the name of the tool
     * @param checkType <code>true</code> if errors and warnings
     *        should be distinguished (depends on the concrete
     *        output of the tool the messages are parsed for),
     *        <code>false</code> if each message is considered
     *        as an error
     * 
     * @since 1.00
     */
    public JavadocTestFilter(PathConfiguration pathConfiguration, String tool, 
        boolean checkType) {
        this.pathConfiguration = pathConfiguration;
        this.configuration = pathConfiguration.getGlobalConfiguration();
        this.checkType = checkType;
        this.tool = tool;
    }

    /**
     * Returns if an error was detected.
     * 
     * @return <code>true</code> if an error was detected,
     *         <code>false</code> else
     * 
     * @since 1.00
     */
    public boolean hasError() {
        return errorDetected;
    }

    /**
     * Filters a given <code>line</code>. Calls {@link #filterMessageDetails(int, String)} and 
     * {@link #shallLogMessageLine(String)}.
     * 
     * @param lineNr the line number of <code>line</code> in the
     *        entire output of the test
     * @param line the line with number <code>lineNr</code> of the 
     *        test output
     * @return the filtered message in XML format
     * 
     * @since 1.00
     */
    @Override
    public String filterMessage(int lineNr, String line) {
        String input = line;
        // yes, should be in a subclass...
        if (line.startsWith("Unable to create")) {
            if (configuration.produceXmlOutput()) {
                line = MessageUtils.composeTutorMessage(configuration, tool, MessageUtils.TYPE_ERROR,  
                    "\"" + tool + "\" server configuration error. Please inform tutor including submission "
                    + "time/date.");
            }
        } else if (line.startsWith("Starting audit...")) {
            line = null;
        } else if (line.startsWith("Audit done.")) {
            line = null;
        } else {
            line = filterMessageDetails(lineNr, line);
        }
        if (shallLogMessageLine(input)) {
            Logger.INSTANCE.log(input);
        }
        if (!configuration.produceXmlOutput()) {
            line = input;
        }
        return line;
    }
    
    /**
     * Whether a given message {@code line} shall be logged.
     * 
     * @param line the line to be logged
     * @return {@code true} for logging, {@code false} for not logging
     */
    protected boolean shallLogMessageLine(String line) {
        return configuration.produceXmlOutput();
    }
    
    /**
     * Returns the configuration.
     * 
     * @return the configuration
     */
    protected Configuration getConfiguration() {
        return configuration;
    }
    
    /**
     * Returns the tool name.
     * 
     * @return the tool name
     */
    protected String getTool() {
        return tool;
    }
    
    /**
     * Sets the {@link #errorDetected error flag} to {@code true}.
     */
    protected void setErrorDetected() {
        errorDetected = true;
    }
    
    @Override
    public String done() {
        return null;
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
    protected String filterMessageDetails(int lineNr, String line) {
        String input = line;
        int dotPos = line.indexOf(".");
        if (dotPos > 0) {
            int firstColonPos = line.indexOf(":", dotPos + 1);
            if (firstColonPos > 0) {
                int sndColonPos = line.indexOf(":", firstColonPos + 1);
                if (sndColonPos > 0) {
                    line = parseLine(line, firstColonPos,
                            sndColonPos);
                } else {
                    line = null;
                }
            } else {
                line = null;
            }
        } else {
            line = null;
        }
        if (null == line) {
            line = input;
            int firstColonPos = line.indexOf(":");
            if (firstColonPos > 0) {
                String[] tmp = parseMsg(extractMsg(line, firstColonPos));
                String msg = tmp[0];
                String msgType = tmp[1];
                errorDetected = MessageUtils.TYPE_ERROR.equals(msgType);
                if (configuration.produceXmlOutput()) {
                    line = "  <message tool=\"" + XmlUtilities.xmlify(tool) 
                        + "\" type=\"" + msgType + "\" file=\"\" "
                        + "line=\"\" message=\"" + XmlUtilities.xmlify(extractFinalMessage(msg)) + "\"/>";
                } else {
                    line = null;
                }
            } else {
                line = null;
            }
        }
        return line;
    }

    /**
     * Parses the message for status/message type.
     * 
     * @param msg the message
     * @return the parsed/modified message in the first place, the message 
     *     type in the second
     */
    protected String[] parseMsg(String msg) {
        String msgType = "";
        for (StatusPrefix prefix : getStatusPrefixes()) {
            if (prefix.matches(msg)) {
                msg = prefix.handle(msg);
                if (checkType) {
                    msgType = prefix.getType();
                } else { // override
                    msgType = MessageUtils.TYPE_ERROR;
                }
                break;
            }
        }
        return new String[] {msg, msgType};
    }
    
    /**
     * Returns the status prefixes.
     * 
     * @return the status prefixes
     */
    protected StatusPrefix[] getStatusPrefixes() {
        return statusPrefix;
    }

    /**
     * Called by filterMessageDetails() if sndColonPos>0.
     * @param line The line of the test output.
     * @param firstColonPos The position of the first colon.
     * @param sndColonPos The position of the second colon.
     * @return The line.
     */
    private String parseLine(String line, int firstColonPos,
            int sndColonPos) {
        int cutFrom = sndColonPos;
        int rdColonPos = line.indexOf(":", sndColonPos + 1);
        if (rdColonPos > 0) {
            // could be emitted as character position
            cutFrom = rdColonPos;
        }
        String[] tmp = parseMsg(extractMsg(line, cutFrom));
        String msg = tmp[0];
        String msgType = tmp[1];
        if (configuration.produceXmlOutput()) {
            String file = extractFile(msg, firstColonPos);
            String result =
                "  <message tool=\"" + XmlUtilities.xmlify(tool) 
                + "\" type=\"" + msgType + "\" file=\""
                + XmlUtilities.xmlify(pathConfiguration.relativizeFileName(file));
            result =
                result + "\" line=\""
                + line.substring(firstColonPos + 1, sndColonPos);
            line = result + "\" message=\"" + XmlUtilities.xmlify(extractFinalMessage(msg)) + "\"/>";
        }
        return line;
    }

    /**
     * Extracts the message for parsing.
     * 
     * @param line the line to extract the message from
     * @param colonPos the first colon position
     * @return the message
     */
    protected String extractMsg(String line, int colonPos) {
        return line.substring(colonPos + 1).trim();
    }

    /**
     * Extracts the file name from message for parsing.
     * 
     * @param line the line to extract the message from
     * @param colonPos the first colon position
     * @return the file name
     */
    protected String extractFile(String line, int colonPos) {
        return line.substring(0, colonPos);
    }
    
    /**
     * Extracts the final message.
     * 
     * @param msg the message
     * @return the final message
     */
    protected String extractFinalMessage(String msg) {
        return msg;
    }
    
    /**
     * Returns the path configuration.
     * 
     * @return the path configuration
     */
    protected PathConfiguration getPathConfiguration() {
        return pathConfiguration;
    }

}
