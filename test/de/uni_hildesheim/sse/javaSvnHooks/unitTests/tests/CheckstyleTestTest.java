package de.uni_hildesheim.sse.javaSvnHooks.unitTests.tests;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.javaSvnHooks.PathConfiguration;
import de.uni_hildesheim.sse.javaSvnHooks.tests.checkstyle.CheckstyleTest;
import de.uni_hildesheim.sse.javaSvnHooks.tests.checkstyle.CheckstyleTestFilter;
import de.uni_hildesheim.sse.javaSvnHooks.tests.javadoc.JavadocTestFilter;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.AbstractTest;
import de.uni_hildesheim.sse.javaSvnHooks.unitTests.utils.TestConfiguration;

/**
 * Tests the {@link CheckstyleTest}.
 * 
 * @author Holger Eichelberger
 */
public class CheckstyleTestTest extends AbstractTest {

    private static final File BASE = new File("testdata/checkstyleTest");

    @Override
    protected File getBase() {
        return BASE;
    }

    /**
     * Tests both test folders (ok, fail) with a proper configuration including 
     * regex and message.
     */
    @Test
    public void testFilesFail() {
        TestConfiguration config = createConfig("hook.properties", 
            new CheckstyleTest());
        String res = executeTest("/filesCharsetFail", false, config);
        Assert.assertNotNull(res);
        Assert.assertTrue(res.indexOf("Weihnachtsmann.java") > 0);
        // character differs Win/Linux :(
        Assert.assertTrue(res.indexOf("Unexpected character") > 0); 
    }
    
    /**
     * Tests whether a warning is parsed correctly, by configuration turned into
     * an error message and contains the correct file name.
     */
    @Test
    public void testExpressionFail() {
        TestConfiguration config = createConfig("hook.properties", 
            new CheckstyleTest());
        String res = executeTest("/simplifyExpressionFail", true, config);
        Assert.assertNotNull(res);
        Assert.assertTrue(res.indexOf("type=\"warning\"") > 0);
        Assert.assertTrue(res.indexOf("file=\"Expression.java\"") > 0);
    }
    
    /**
     * Tests the message parsing for Java module-info.java files if Checkstyle 
     * does not know anything about modules.
     * This test does not use directly the Checkstyle test, because more recent 
     * versions of Checkstyle could fix that
     * problem. We are just interested in the parsing capabilities 
     * of {@link JavadocTestFilter}.
     */
    @Test
    public void testModuleProblem() {
        String[] lines = {
            "com.puppycrawl.tools.checkstyle.api.CheckstyleException: "
                + "Exception was thrown while processing "
                + "/tmp/rev_6269/JavaBlatt8Aufgabe1/JP122/module-info.java",
            "    at com.puppycrawl.tools.checkstyle.Checker.processFiles("
                + "Checker.java:295)",
            "    at com.puppycrawl.tools.checkstyle.Checker.process("
                + "Checker.java:213)",
            "    at com.puppycrawl.tools.checkstyle.Main.runCheckstyle("
                + "Main.java:581)",
            "    at com.puppycrawl.tools.checkstyle.Main.runCli(Main.java:472)",
            "    at com.puppycrawl.tools.checkstyle.Main.main(Main.java:226)",
            "Caused by: com.puppycrawl.tools.checkstyle.api."
                + "CheckstyleException: NoViableAltException occurred "
                + "while parsing file /tmp/rev_6269/JavaBlatt8Aufgabe1/JP122/"
                + "module-info.java.",
            "    at com.puppycrawl.tools.checkstyle.JavaParser.parse("
                + "JavaParser.java:98)",
            "    at com.puppycrawl.tools.checkstyle.TreeWalker.processFiltered("
                + "TreeWalker.java:181)",
            "    at com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck."
                + "process(AbstractFileSetCheck.java:81)",
            "    at com.puppycrawl.tools.checkstyle.Checker.processFile("
                + "Checker.java:316)",
            "    at com.puppycrawl.tools.checkstyle.Checker.processFiles("
                + "Checker.java:286)",
            "    ... 4 more",
            "Checkstyle ends with 1 errors.",
            "Caused by: /tmp/rev_6269/JavaBlatt8Aufgabe1/JP122/module-info."
                + "java:8:1: unexpected token: module",
            "    at com.puppycrawl.tools.checkstyle.grammar."
                + "GeneratedJavaRecognizer.compilationUnit("
                + "GeneratedJavaRecognizer.java:181)",
            "    at com.puppycrawl.tools.checkstyle.JavaParser.parse("
                + "JavaParser.java:92)",
            "    ... 8 more"
        };
        String result = fakeTestWithText(lines, new File("/tmp"), 
            new File("/tmp/rev_6269/JavaBlatt8Aufgabe1/JP122"));
        Assert.assertTrue(result.indexOf("unexpected token: module") > 0);
    }

    /**
     * Tests whether the typical checkstyle prologue and epilogue are
     * correctly skipped in output.
     */
    @Test
    public void testOutputProblem() {
        String[] lines = {
            "Starting audit...",
            "[ERROR] /tmp/rev_370/JavaBlatt1Aufgabe1/JP126/Taschenrechner.java:"
                + "7: 'method def modifier' has incorrect indentation "
                + "level 2, expected level should be 4. [Indentation]",
            "[ERROR] /tmp/rev_370/JavaBlatt1Aufgabe1/JP126/Taschenrechner.java:"
                + "9: 'method def' child has incorrect indentation level 4, "
                + "expected level should be 8. [Indentation]",
            "[ERROR] /tmp/rev_370/JavaBlatt1Aufgabe1/JP126/Taschenrechner.java:"
                + "11: 'method def' child has incorrect indentation "
                + "level 4, expected level should be 8. [Indentation]",
            "[ERROR] /tmp/rev_370/JavaBlatt1Aufgabe1/JP126/Taschenrechner.java:"
                + "13: 'method def' child has incorrect indentation level 4"
                + ", expected level should be 8. [Indentation]",
            "[ERROR] /tmp/rev_370/JavaBlatt1Aufgabe1/JP126/Taschenrechner.java:"
                + "15: 'method def' child has incorrect indentation "
                + "level 4, expected level should be 8. [Indentation]",
            "[ERROR] /tmp/rev_370/JavaBlatt1Aufgabe1/JP126/Taschenrechner.java:"
                + "17: 'method def' child has incorrect indentation level 4, "
                + "expected level should be 8. [Indentation]",
            "[ERROR] /tmp/rev_370/JavaBlatt1Aufgabe1/JP126/Taschenrechner.java:"
                + "19: 'method def' child has incorrect indentation level 4, "
                + "expected level should be 8. [Indentation]",
            "[ERROR] /tmp/rev_370/JavaBlatt1Aufgabe1/JP126/Taschenrechner.java:"
                + "21: 'method def' child has incorrect indentation level 4, "
                + "expected level should be 8. [Indentation]",
            "[ERROR] /tmp/rev_370/JavaBlatt1Aufgabe1/JP126/Taschenrechner.java:"
                + "22: 'method def rcurly' has incorrect indentation level 2, "
                + "expected level should be 4. [Indentation]",
            "Audit done.",
            "Checkstyle ends with 9 errors."};
        String result = fakeTestWithText(lines, new File("/tmp"), 
            new File("/tmp/rev_370/JavaBlatt1Aufgabe1/JP126")).trim();
        Assert.assertTrue("must start with message", 
            result.startsWith("<message"));
        Assert.assertTrue("must end with message", 
            result.endsWith("/>"));
    }
    
    /**
     * Tests a simple message with/out checkstyle logging status.
     */
    @Test
    public void simpleMessageTest() {
        String[] lines = {
            "Starting audit...",
            "[ERROR] /tmp/rev_7875/JavaBlatt13Aufgabe1/JPTest/kasse/Kasse.java:"
                + "28:5: Missing a Javadoc comment. [JavadocMethod]",
            "Audit done.",
            "Checkstyle ends with 1 errors."
        };
        String result = fakeTestWithText(lines, new File("/tmp"), 
                new File("/tmp/rev_7875/JavaBlatt13Aufgabe1/JPTest"))
            .trim();
        Assert.assertTrue("message not correct", result.equals(
            "<message tool=\"checkstyle\" type=\"error\" "
            + "file=\"kasse/Kasse.java\" line=\"28\" "
            + "message=\"Missing a Javadoc comment.\"/>"));
    }
   
    /**
     * Supports testing checkstyle output given as array of output lines.
     * 
     * @param lines the output lines
     * @param tmpDir the temporary directory
     * @param workingDir the path configuration working directory
     * @return the checkstyle filter result
     */
    private String fakeTestWithText(String[] lines, File tmpDir, File workingDir) {
        TestConfiguration config = createConfig("hook.properties");
        // just for testing the parser/message creation
        config.setTempDirectory(tmpDir); 
        // concrete path configuration is irrelevant
        PathConfiguration pathConf = findPathConfiguration("/filesCharsetFail", 
            config);
        pathConf.setWorkingDir(workingDir);
        Assert.assertNotNull(pathConf);
        CheckstyleTestFilter filter = new CheckstyleTestFilter(pathConf, 
            "checkstyle", false);
        String result = "";
        String tmp;
        for (int lineNr = 0; lineNr < lines.length; lineNr++) {
            tmp = filter.filterMessage(lineNr + 1, lines[lineNr]);
            if (null != tmp) {
                result += tmp;
            }
        }
        tmp = filter.done();
        if (null != tmp) {
            result += tmp;
        }
        return result;
    }

}
