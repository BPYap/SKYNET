package com.skynet.utility;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.*;

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
        } catch(IOException e) {
            Log.e("tag", "Failed to copy asset file: " + filename, e);
        }
        finally {
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
