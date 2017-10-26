package com.beattheheat.beatthestreet.FileManagement;

import android.util.Log;

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

    public String toStringFast(int bufferSize) throws IOException {
        StringBuilder sb = new StringBuilder();
        byte[] buf = new byte[bufferSize];
        while ((fis.read(buf)) != -1) {
            sb.append(new String(buf, "UTF-8"));
        }

        return sb.toString();
    }

    public int toStringsFast(String[] output, int bufferSize, char split) throws IOException {
        String str = toStringFast(bufferSize);

        int start_ind = 0;
        int ind = 0;
        int arrayInd = 0;
        while ((ind = str.indexOf(split, start_ind)) != -1) {



            output[arrayInd] = str.substring(start_ind, ind+1);
            start_ind = ind+1;
            arrayInd++;
        }

        return arrayInd;
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
