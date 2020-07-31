package de.uni_hildesheim.sse.javaSvnHooks.tests.checkstyle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;
import de.uni_hildesheim.sse.javaSvnHooks.tests.ITestMessageFilter;
import de.uni_hildesheim.sse.javaSvnHooks.tests.javadoc.JavadocTestFilter;
import de.uni_hildesheim.sse.javaSvnHooks.util.MessageUtils;

/**
 * Implements a test filter according to the message format 
 * of the checkstyle program. Currently, this filter 
 * does not consider warnings. For the output XML format see 
 * {@link ITestMessageFilter}.
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public class CheckstyleTestFilter extends JavadocTestFilter {

    private static final String EXCEPTION_NAME = 
        "com.puppycrawl.tools.checkstyle.api.CheckstyleException";
    private static final Pattern EXC_PATTERN;
    private static final Pattern EXC_LINE_PATTERN;
    
    private static final StatusPrefix[] statusPrefix = {
        new StatusPrefix("[ERROR]", MessageUtils.TYPE_ERROR),
        new StatusPrefix("[WARNING]", MessageUtils.TYPE_WARNING),
        new StatusPrefix("[WARN]", MessageUtils.TYPE_WARNING)};
    
    static {
        Pattern p = null;
        Pattern l = null;
        try {
            p = Pattern.compile("^" + EXCEPTION_NAME 
                + ":.* while parsing file (.*).$");
            l = Pattern.compile("^(line )?(([^:]+):)?(\\d+):(\\d+): (.*)$");
        } catch (PatternSyntaxException e) {
            Logger.INSTANCE.logException(e, false);
        }
        EXC_PATTERN = p;
        EXC_LINE_PATTERN = l;
    }
    
    /**
     * Whether an exception was detected and a specific message filter shall be 
     * applied.
     */
    private boolean exceptionMode = false;

    /**
     * If an exception was detected, stores the file name extracted somewhere 
     * from the stacktrace.
     */
    private transient String exceptionFile;
    
    /**
     * Stores a parsed full exception trace line by line.
     */
    private transient String exceptionLines = "";
    
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
    public CheckstyleTestFilter(PathConfiguration pathConfiguration, String tool, 
        boolean checkType) {
        super(pathConfiguration, tool, checkType);
    }
    
    @Override
    protected String filterMessageDetails(int lineNr, String line) {
        if (line.startsWith(EXCEPTION_NAME)) {
            exceptionMode = true;
            exceptionLines += line;
            line = null;
        } else if (exceptionMode) {
            exceptionLines += line;
            line = filterExceptionMessageDetails(lineNr, line);
        } else {
            line = super.filterMessageDetails(lineNr, line);
        }
        return line;
    }
    
    @Override
    protected boolean shallLogMessageLine(String line) {
        return !exceptionMode && super.shallLogMessageLine(line);
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
    private String filterExceptionMessageDetails(int lineNr, String line) {
        String result = null;
        if (line.length() > 10 && line.startsWith("Caused by:")) {
            line = line.substring(10).trim();
            Matcher matcher = EXC_PATTERN.matcher(line);
            if (matcher.matches()) {
                exceptionFile = matcher.group(1);
                exceptionFile = getPathConfiguration().relativizeFileName(
                    exceptionFile);
            } else {
                matcher = EXC_LINE_PATTERN.matcher(line);
                if (matcher.matches() && null != exceptionFile) {
                    // group 1 is optional line, only emitted sometimes
                    // group 2 is optional file name with :
                    // group 3 is file name, only emitted sometimes 
                    String excLine = matcher.group(4);
                    // group 5 is character in line
                    String excCause = matcher.group(6);
                    result = MessageUtils.composeMessage(getConfiguration(), 
                        getTool(), MessageUtils.TYPE_ERROR, exceptionFile, 
                        excLine, excCause); 
                    exceptionFile = null;
                    setErrorDetected();
                }
            }
        }
        return result;
    }

    @Override
    public String done() {
        String msg = null;
        if (exceptionMode && !hasError()) {
            // if used in two different gobblers for the same process
            synchronized (this) {
                setErrorDetected();
                msg = MessageUtils.composeTutorMessage(getConfiguration(), 
                   getTool(), MessageUtils.TYPE_ERROR,  
                   "So far unknown internal error of \"" + getTool() + "\".");
                if (exceptionLines.length() > 0) {
                    Logger.INSTANCE.log(exceptionLines);    
                }
            }
        }
        return msg;
    }

    @Override
    protected StatusPrefix[] getStatusPrefixes() {
        return statusPrefix;
    }
    
    @Override
    protected String extractMsg(String line, int colonPos) {
        return line; // we need everything including status
    }

    @Override
    protected String extractFile(String line, int colonPos) {
        String result;
        int pos = line.indexOf(':');
        if (1 == pos && Character.isAlphabetic(line.charAt(0))) {
            // probably Windows path
            pos = line.indexOf(':', 2);
        }
        if (pos > 0) {
            result = line.substring(0, pos);
        } else {
            result = line;
        }
        return result;
    }
    
    @Override
    protected String extractFinalMessage(String msg) {
        int pos = msg.indexOf(':');
        if (pos > 0) {
            int mPos = msg.indexOf(':', pos + 1);
            if (mPos > 0) {
                int ePos = msg.indexOf(':', mPos + 1);
                if (ePos > 0) {
                    msg = msg.substring(ePos + 1, msg.length()).trim();
                } else {
                    msg = msg.substring(mPos + 1, msg.length()).trim();
                }
            }
        }
        if (msg.endsWith("]")) {
            pos = msg.lastIndexOf('[');
            if (pos > 0) {
                msg = msg.substring(0, pos).trim();
            }
        }
        return msg;
    }

}
