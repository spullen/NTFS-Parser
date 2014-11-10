package com.spullen.ntfs;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileReader {

    /**
     * Reads in b.length of data from the offset of a file
     *
     * @param b
     * @param offset
     * @param filename
     * @return b byte[]
     */
    public static void seekAndRead(byte[] b, int offset, String filename) throws IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(new File(filename), "r");
            raf.seek((long) offset); // set the file pointer to the offset
            raf.readFully(b); // read in the bytes from where the file pointer is
        } finally {
            if (raf != null){
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
