package software.unf.dk.timetracker;

import java.io.*;

/**
 * Handles processing of XML Files.
 */

class IOHandler {

    final File file;

    /**
     * Creates a new IOHandler
     * @param file File the handler should write to
     */
    IOHandler(File file) {
        this.file = file;
    }
}