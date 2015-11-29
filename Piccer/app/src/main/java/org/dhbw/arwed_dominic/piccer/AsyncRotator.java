package org.dhbw.arwed_dominic.piccer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Rotates an ImageItem Asynchronously on a different thread and saves it into a file.
 */
public class AsyncRotator extends AsyncTask<File, Void, Void> {
    private ImageItem imageItem;
    private int rotation;
    private FileOutputStream out;
    /**
     * A counter for creating temorary files.
     * This prevents interfearance of AsyncRotators.
     */
    private static int tmpCount = 0;
    /**
     * A temporary Location where the image is saved and then copied to the final location.
     * This prevents the possibility that an Image file breaks.
     */
    private File tmpLocation;
    /**
     * The final location of the image.
     */
    private File destination;

    private static String TMP = "tmp";
    private static String TMP_FILE = "tmpRotation";

    public AsyncRotator (Context context, ImageItem imageItem, int rotation) {
        this.imageItem = imageItem;
        this.rotation = rotation;
        this.destination = imageItem.getFile();
        tmpCount++;
        this.tmpLocation = new File(context.getExternalFilesDir(TMP), TMP_FILE + tmpCount);
    }

    @Override
    protected Void doInBackground(File... params) {
        if(rotation == 0) return null;
        Bitmap bitmap = BitmapFactory.decodeFile(imageItem.getFile().getAbsolutePath());
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        try {
            out = new FileOutputStream(tmpLocation);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            //Rotation did not work.
        }
        finally {
            try {
                if (out != null) out.close();
            } catch (IOException e) {}
        }

        return null;
    }

    /**
     * Copies the temporary file to the final location.
     * @param _
     */
    @Override
    protected void onPostExecute(Void _) {
        tmpCount--;
        tmpLocation.renameTo(destination);
        imageItem.notifyCache();
        tmpLocation.delete();
    }

    /**
     * Cancelles all remaining processes.
     * Closes Streams and deletes temporary files.
     */
    @Override
    protected void onCancelled() {
        try {
            if(out != null) out.close();
            tmpLocation.delete();
            tmpCount--;
        } catch (IOException e) {}
    }
}
