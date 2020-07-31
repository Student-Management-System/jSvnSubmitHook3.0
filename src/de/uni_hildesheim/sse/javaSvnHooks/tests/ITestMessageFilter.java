package de.uni_hildesheim.sse.javaSvnHooks.tests;

/**
 * Defines a message filter to read output from a test
 * and to transform it into individual messages in an 
 * XML error stream. The XML format is as follows:
 * 
 * <pre>
 * &lt;submitResults&gt;
 *   &lt;message tool=&quot;&quot; type=&quot;&quot; file=&quot;&quot; 
 *     line=&quot;&quot; message=&quot;&quot;&gt;
 *     &lt;example position=&quot;&quot;&gt; &lt;!-- optional--&gt;
 *     &lt;!-- arbitrary text --&gt;
 *     &lt;/example&gt;
 *   &lt;/message&gt;
 * &lt;/submitResults&gt;
 * </pre>
 * 
 * @author Holger Eichelberger
 * @since 1.00
 * @version 1.00
 */
public interface ITestMessageFilter {

    /**
     * Filters a given <code>line</code>.
     * 
     * @param lineNr the line number of <code>line</code> in the
     *        entire output of the test
     * @param line the line with number <code>lineNr</code> of the 
     *        test output
     * @return the filtered message, may be in in XML format
     * 
     * @since 1.00
     */
    public String filterMessage(int lineNr, String line);

    /**
     * Test execution has been done, the filter shall clean up in case of unsent messages.
     * 
     * @return a message or <b>null</b> for none
     */
    public String done();
    
}