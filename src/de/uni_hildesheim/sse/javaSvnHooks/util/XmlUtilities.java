package de.uni_hildesheim.sse.javaSvnHooks.util;

/**
 * Some XML related utility methods.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class XmlUtilities {

    /**
     * Prevents this class from being instantiated from
     * outside.
     * 
     * @since 1.00
     */
    private XmlUtilities() {
    }

    /**
     * Reduces the specified string to a readable one.
     * 
     * @param string the string to be made readable
     * @return the readable string
     * 
     * @since 1.00
     */
    public static final String reduceToReadable(String string) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c < 128) {
                builder.append(c);
            }
        }
        return builder.toString();
    }
    
    /**
     * Does typical XML escapting on the given string.
     * 
     * @param string the string to be xmlified (may be <b>null</b>)
     * @return the xmlified string, empty if <code>string</code> is <b>null</b>
     * 
     * @since 1.00
     */
    public static final String xmlify(String string) {
        StringBuilder builder = new StringBuilder();
        if (null != string) {
            for (int i = 0; i < string.length(); i++) {
                char c = string.charAt(i);
                String tmp = xmlifyChar(c, false);
                if (null != tmp) {
                    builder.append(tmp);
                }
            }
        }
        return builder.toString();
    }
    
    /**
     * Does typical XML escaping on the given String.
     * Contrary to {@link #xmlify(String)} this method is designed to be used
     * to create String for a XML attribute. Linefeeds need a special treatment.
     * 
     * @param string the string to be xmlified
     * @return the xmlified string
     * 
     * @since 1.00
     */
    public static final String xmlifyForAttributes(String string) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            builder.append(xmlifyChar(c, true));
        }
        return builder.toString();
    }
    
    /**
     * Does typical XML escaping for the given character.
     * 
     * @param ch The character to escape.
     * @param escapeNewline Whether newline characters should be escaped.
     * @return A String escaping the character or the character as String.
     */
    private static String xmlifyChar(char ch, boolean escapeNewline) {
        String result = null;
        switch (ch) {
        case '\'':
            result = "";
            break;
        case '\"':
            result = "&quot;";
            break;
        case '\n':
            if (escapeNewline) {
                result = "&#10;";
            }
            break;
        case '<':
            result = "&lt;";
            break;
        case '>':
            result = "&gt;";
            break;
        case '&':
            result = "&amp;";
            break;
        // German umlauts
        case 'Ä':
            result = "&#196;";
            break;
        case 'Ö':
            result = "&#214;";
            break;
        case 'Ü':
            result = "&#220;";
            break;
        case 'ß':
            result = "&#223;";
            break;
        case 'ä':
            result = "&#228;";
            break;
        case 'ö':
            result = "&#246;";
            break;
        case 'ü':
            result = "&#252;";
            break;
        default:
            result = String.valueOf(ch);
            break;
        }
        return result;
    }

}