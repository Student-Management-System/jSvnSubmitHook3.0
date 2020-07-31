package de.uni_hildesheim.sse.javaSvnHooks.tests.eclipse_config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;

import de.uni_hildesheim.sse.javaSvnHooks.Configuration;
import de.uni_hildesheim.sse.javaSvnHooks.logging.Logger;

/**
 * A simple checksum utility class for file checksums.
 * 
 * @author Holger Eichelberger
 */
class FileChecksumUtil {

    /**
     * Stores the checksum instance.
     * 
     * @since 1.20
     */
    private CRC32 checksum = new CRC32();
    
    /**
     * Stores the buffer for checksum calculation.
     * 
     * @since 1.20
     */
    private byte[] crcBuffer = new byte[1024];
    
    /**
     * Returns if two files are considered as equal regarding
     * their checksums.
     * 
     * @param configuration used for logging
     * @param from the first file to be considered
     * @param to the second file to be considered
     * @return <code>true</code> if both files are considered
     *         as equal, <code>false</code> else
     * 
     * @since 1.20
     */
    public boolean equalByCheckSum(Configuration configuration, 
        File from, File to) {
        boolean equal = false;
        if (from.isFile() && to.isFile()) {
            try {
                long fromChecksum = getChecksum(from);
                long toChecksum = getChecksum(to);
                equal = toChecksum == fromChecksum;
                if (!equal) {
                    Logger.INSTANCE.log("Checksum mismatch:\n " 
                        + from + " " + from.length() + " " + fromChecksum + " "
                        + to + " " + to.length() + " " + toChecksum);
                }
            } catch (IOException ioe) {
                Logger.INSTANCE.logException(ioe, false);
            }
        } else {
            Logger.INSTANCE.log("Checksum filetype mismatch:\n " 
                + from + " " + from.length() + " " + from.isFile() + " "
                + to + " " + to.length() + " " + to.isFile());
        }
        return equal;
    }

    /**
     * Returns the checksum for a specified file.
     * 
     * @param file the file to calculate the checksum for
     * @return the checksum for the file
     * @throws IOException if any input/output related exception 
     *         occurred
     * 
     * @since 1.20
     */
    private long getChecksum(File file) throws IOException {
        FileInputStream fis = null;
        try {
            int len = 0;
            checksum.reset();
            fis = new FileInputStream(file);
            while ((len = fis.read(crcBuffer)) != -1) {
                checksum.update(crcBuffer, 0, len);
            }
            fis.close();
            return checksum.getValue();
        } catch (IOException ioe) {
            try {
                if (null != fis) {
                    fis.close();
                }
            } catch (IOException ioe1) {
            }
            throw ioe;
        }
    }
    
}
