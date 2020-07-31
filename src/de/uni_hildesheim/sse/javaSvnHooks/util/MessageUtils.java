package de.uni_hildesheim.sse.javaSvnHooks.util;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;

/**
 * Message utilities.
 * 
 * @author eichelberger
 */
public class MessageUtils {

    public static final String TYPE_ERROR = "error";
    public static final String TYPE_WARNING = "warning";

    // checkstyle: stop parameter number check
    
    /**
     * Composes a XML/plain message.
     * 
     * @param config the actual configuration
     * @param tool the issuing tool name
     * @param type the message type
     * @param file the file (may be empty)
     * @param line the line in file (may be empty)
     * @param message the message
     * @return the composed XML/plain message
     */
    public static String composeMessage(Configuration config, String tool, String type, String file, String line, 
        String message) {
        String result;
        if (config.produceXmlOutput()) {
            result = "<message tool=\"" + XmlUtilities.xmlify(tool) + "\""
                + " type=\"" + XmlUtilities.xmlify(type) + "\""
                + " file=\"" + XmlUtilities.xmlify(file) + "\""
                + " line=\"" + XmlUtilities.xmlify(line) + "\""
                + " message=\"" + XmlUtilities.xmlify(message) + "\"></message>";
        } else {
            result = "";
            if (type.length() > 0) {
                result += type + ": ";
            }
            result += message;
            if (file.length() > 0) {
                result += " in " + file;
            }
            if (line.length() > 0) {
                result += "(" + line + ")";
            }
        }
        return result;
    }

    // checkstyle: resume parameter number check

    /**
     * Composes a XML/plain message of a tool problem in order to inform a tutor. Appends a tutor hint to the 
     * given <code>message</code>.
     * 
     * @param config the actual configuration
     * @param tool the issuing tool name
     * @param type the message type
     * @param message the message
     * @return the composed XML/plain message
     */
    public static String composeTutorMessage(Configuration config, String tool, String type,  
        String message) {
        return composeMessage(config, tool, MessageUtils.TYPE_ERROR, null, null, 
            message + " Please inform tutor including submission time/date.");        
    }

}
