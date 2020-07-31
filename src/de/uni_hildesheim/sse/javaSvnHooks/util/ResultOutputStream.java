package de.uni_hildesheim.sse.javaSvnHooks.util;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;

/**
 * Represents the result output stream. First step, provide methods to generate
 * the output stream. Second (future) step - avoid passing the output stream
 * through the Hook, use an instance of this class.
 * 
 * @author eichelbe
 */
public class ResultOutputStream {

    /**
     * Defines message types.
     * 
     * @author eichelbe
     */
    public enum MessageType {
        ERROR("error");
        
        private String text;
        
        /**
         * Creates a message type.
         * 
         * @param text the text to use as XML representation (shall be xmlified)
         */
        private MessageType(String text) {
            this.text = text;
        }
        
        /**
         * Returns the XML text of this type.
         * 
         * @return the XML text
         */
        private String getText() {
            return text;
        }
    }

    /**
     * Not needed for now. Shall take the real output stream later on.
     */
    private ResultOutputStream() {
    }
    
    // checkstyle: stop parameter number check
    
    /**
     * Adds a message.
     * 
     * @param config the configuration instance
     * @param tool the tool (will be xmlified if needed)
     * @param type the message type
     * @param message the message (will be xmlified if needed)
     * @param file the name of the file the error occurred within (ignored 
     *     if <b>null</b>, xmlified if needed)
     * @param line the line within <code>file</code> (ignored 
     *     if <b>null</b>, xmlified if needed)
     */
    public static void addMessage(Configuration config, String tool, 
        MessageType type, String message, String file, String line) {
        String errorMessage;
        if (config.produceXmlOutput()) {
            tool = toAttribute("tool", tool);
            message = toAttribute("message", message);
            file = toAttribute("file", file);
            line = toAttribute("line", line);
            String t = toAttribute("type", type.getText());
            errorMessage = "  <message" + tool + t + message + file + line + "/>";
        } else {
            errorMessage = tool + " " + type.getText() + ": " + message;
            if (null == file) {
                errorMessage += "in file " + file;
            }
            if (null == line) {
                errorMessage += "in line " + line;
            }
        }
        config.getTestOutputStream().println(errorMessage);
    }

    // checkstyle: resume parameter number check

    /**
     * Returns an attribute representation.
     * 
     * @param attributeName the name of the attribute (may be <b>null</b>, 
     *   ignored then)
     * @param contents the contents (to be XMLified, may be <b>null</b>, 
     *   ignored then)
     * @return the attribute with leading space or an empty string
     */
    private static String toAttribute(String attributeName, String contents) {
        String result = "";
        if (null != attributeName && null != contents) {
            result = " " + attributeName + "=\"" + XmlUtilities.xmlify(contents) 
                + "\"";
        }
        return result;
    }
    
    /**
     * Adds an error message.
     * 
     * @param config the configuration instance
     * @param tool the tool (will be xmlified if needed)
     * @param message the message (will be xmlified if needed)
     */
    public static void addErrorMessage(Configuration config, String tool, String message) {
        addMessage(config, tool, MessageType.ERROR, message, null, null);
    }

    /**
     * Adds an error message for an exception and logs the exception as handled.
     * 
     * @param config the configuration instance
     * @param tool the tool (will be xmlified if needed)
     * @param prefix any text to be emitted before the message of 
     *    <code>exception</code>, ignored if empty or <b>null</b>, otherwise a 
     *    ":" is inserted before concatenating the <code>exception</code> message
     * @param exception the exception to be handled
     */
    public static void addErrorMessage(Configuration config, String tool, 
        String prefix, Throwable exception) {
        addMessage(config, tool, MessageType.ERROR, 
            (null == prefix || prefix.length() == 0 ? "" : prefix + ": ") 
            + exception.getMessage(), null, null);
        Logger.INSTANCE.logException(exception, true);
    }

    /**
     * Adds the leadin for the results if needed.
     * 
     * @param config the configuration instance
     */
    public static void addLeadIn(Configuration config) {
        if (config.produceXmlOutput()) {
            config.getTestOutputStream().println("<submitResults>");
        }
    }
    
    /**
     * Adds the leadout for the results if needed.
     * 
     * @param config the configuration instance
     */
    public static void addLeadOut(Configuration config) {
        if (config.produceXmlOutput()) {
            config.getTestOutputStream().println("</submitResults>");
        }
    }

}
