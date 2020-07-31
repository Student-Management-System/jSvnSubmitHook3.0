package de.uni_hildesheim.sse.javaSvnHooks.tests.file_name;

import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Simple file name scanner. Tests all files in a folder recursively against {@link #PATTERN}. The pattern
 * is intentionally in the code so that it can be copied as it is into a configuration file.
 * 
 * @author Holger Eichelberger
 */
public class FileNameScanner {
    
    private static final String PATTERN = "[a-zA-Z0-9_\\$\\.\\- ]+";

    /**
     * The main method.
     * 
     * @param args the first parameter may optionally be the folder to check. Otherwise the actual folder is being 
     * checked.
     */
    public static void main(String[] args) {
        File start = args.length > 0 ? new File(args[0]) : new File(".");
        try {
            Pattern pattern = Pattern.compile(PATTERN);
            checkFiles(start, pattern);
        } catch (PatternSyntaxException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Checks {@code file} and all contained files and folders.
     * 
     * @param file the file to check
     * @param pattern the pattern to check against
     */
    private static void checkFiles(File file, Pattern pattern) {
        checkFileName(file, pattern);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File f : files) {
                    checkFiles(f, pattern);
                }
            }
        }
    }
    
    /**
     * Checks the file name of {@code pattern}.
     * 
     * @param file the file to check
     * @param pattern the pattern to check against
     */
    private static void checkFileName(File file, Pattern pattern) {
        String name = file.getName();
        if (!pattern.matcher(name).matches()) {
            System.out.println(name + " does not match regex");
        }
    }

}
