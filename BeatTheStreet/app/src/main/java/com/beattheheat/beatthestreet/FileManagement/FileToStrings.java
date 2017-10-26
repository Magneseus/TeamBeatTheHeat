package com.beattheheat.beatthestreet.FileManagement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Matt on 26-Oct-17.
 *
 * Small convenience class to convert a file to a list of Strings.
 */

public class FileToStrings {
    private FileInputStream fis;

    public FileToStrings(FileInputStream fileInputStream) {
        fis = fileInputStream;
    }

    public ArrayList<String> toStringList() throws IOException {
        ArrayList<String> stringList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader( new InputStreamReader(fis))){
            String line;
            while ((line = br.readLine()) != null) {
                stringList.add(line);
            }
        }

        return stringList;
    }

    public String[] toStringArray() throws IOException {
        ArrayList<String> stringList = toStringList();
        return stringList.toArray(new String[stringList.size()]);
    }
}
