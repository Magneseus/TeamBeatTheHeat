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
 */

public class Unzipper {
    private Context app_ctx;
    private String fileName;
    private String location;

    public Unzipper(Context ctx, String fileName, String location) {
        // Get application context to avoid static leaks
        this.app_ctx = ctx.getApplicationContext();

        this.fileName = fileName;
        this.location = location;
        checkDirectory("");
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
                    checkDirectory(ze.getName());
                    zis.closeEntry();
                }
                else {
                    // Open a file out stream to place contents on disk
                    FileOutputStream fos = app_ctx.openFileOutput(ze.getName(), Context.MODE_PRIVATE);

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

    // Checks if the specified directory exists, and creates it if not
    private void checkDirectory(String dir)
    {
        File f = new File(location + dir);
        if(!f.isDirectory())
        {
            f.mkdirs();
        }
    }
}
