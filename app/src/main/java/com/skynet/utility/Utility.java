package com.skynet.utility;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.*;

/**
 * <pre>
 *  Utility to copy files from asset folder to local storage
 *  Usage:
 *      Call copyAssets(context, filename) in app initialization.
 *      Will not copy file again if the file already exists.
 *      To replace old file of same file name with new file, re-installation of app is required.
 * </pre>
 *
 *  @author  Yap Boon Peng
 */

public class Utility {
    public static File copyAssets(Context context, String filename) {
        AssetManager assetManager = context.getAssets();

        InputStream in = null;
        OutputStream out = null;
        File outFile = null;
        try {
            in = assetManager.open(filename);
            File dir = context.getExternalFilesDir(null);

            if (dir == null) {
                Log.e("Utility", "context.getExternalFilesDir(null) return null");
                return null;
            }

            outFile = new File(dir, filename);
            if (!outFile.exists()) {
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                Log.i("Utility", filename + " is copied to " + dir.getName());
            } else {
                Log.i("Utility", filename + " already exists. No action needed");
            }
        } catch (IOException e) {
            Log.e("tag", "Failed to copy asset file: " + filename, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // NOOP
                }
            }
        }
        return outFile;
    }
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

}
