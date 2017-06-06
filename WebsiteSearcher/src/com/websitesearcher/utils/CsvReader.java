package com.websitesearcher.utils;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Utility to read a CSV from a file.
 *
 * @author gwalzer
 */
public class CsvReader {

    private BufferedReader bufferedReader;
    private String separator;

    public CsvReader(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
        this.separator = ","; // default
    }

    public CsvReader(BufferedReader bufferedReader, String separator) {
        this.bufferedReader = bufferedReader;
        this.separator = separator;
    }

    public String[] readLine() throws IOException {
        String line = bufferedReader.readLine();
        if (line == null) {
            return null;
        }
        return line.split(separator);
    }
}
