package com.mobelite.filesplitter.Splitter;

import java.io.File;
import java.io.IOException;

public interface Splitter {

    /**
     * Splits the given file into smaller chunks of the specified size.
     *
     * @param file          the file to be split
     * @param chunkSize     the size of each chunk in megabytes
     * @return an array of files representing the split chunks
     * @throws IOException if an I/O error occurs
     */
    File[] split(File file, double chunkSize) throws IOException;

    /**
     * Merges the array of files into a single file at the specxified file path.
     *
     * @param files    the array of files to be merged
     * @param filePath the path of the resulting merged file
     * @throws IOException if an I/O error occurs
     */
    File merge(File[] files, String filePath) throws IOException;


}
