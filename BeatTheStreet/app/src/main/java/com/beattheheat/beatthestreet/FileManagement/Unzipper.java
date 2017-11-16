package com.beattheheat.beatthestreet.FileManagement;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Matt on 24-Oct-17.
 *
 * Small convenience class to unzip packed zip files.
 */

public class Unzipper {
    private Context app_ctx;
    private String fileName;
    private String location;

    public Unzipper(Context ctx, String filename) {
        this(ctx, filename, "");
    }

    public Unzipper(Context ctx, String fileName, String location) {
        // Get application context to avoid static leaks
        this.app_ctx = ctx.getApplicationContext();
        this.location = location;

        this.fileName = fileName;
    }

    public void Unzip() {
        try {
            // Open the zip file
            ZipInputStream zis = new ZipInputStream(app_ctx.openFileInput(fileName));
            ZipEntry ze = null;

            // Loop through contents of zip file
            while ((ze = zis.getNextEntry()) != null) {
                // Check if the entry is a directory
                if (ze.isDirectory()) {
                    zis.closeEntry();
                }
                else {
                    // Open a file out stream to place contents on disk

                    FileOutputStream fos;
                    if (location.equals("")) {
                        fos = app_ctx.openFileOutput(ze.getName(), Context.MODE_PRIVATE);
                    } else {
                        fos = new FileOutputStream(new File(location, ze.getName()));
                    }

                    int len;
                    byte[] buf = new byte[1024];
                    while ((len = zis.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.close();
                    zis.closeEntry();
                }
            }
            zis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
