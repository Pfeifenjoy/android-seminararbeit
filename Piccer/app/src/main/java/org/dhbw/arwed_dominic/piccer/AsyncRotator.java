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
 * Created by arwed on 26.11.15.
 */
public class AsyncRotator extends AsyncTask<File, Void, Void> {
    private ImageItem imageItem;
    private int rotation;
    private FileOutputStream out;
    private Context context;
    private static int tmpCount = 0;
    private File tmpLocation;
    private ImageItemAdapter adapter;

    public AsyncRotator (Context context, ImageItem imageItem, int rotation, ImageItemAdapter adapter) {
        this.imageItem = imageItem;
        this.adapter = adapter;
        this.rotation = rotation;
        this.context = context;
        this.tmpLocation = new File(this.context.getExternalFilesDir("tmp"), "tmpRotation" + tmpCount);
    }
    @Override
    protected Void doInBackground(File... params) {
        if(rotation == 0) return null;
        tmpCount++;
        Bitmap bitmap = BitmapFactory.decodeFile(imageItem.getFile().getAbsolutePath());
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        try {
            out = new FileOutputStream(tmpLocation);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            //TODO
        }
        finally {
            try {
                if (out != null) out.close();
            } catch (IOException e) {}
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void _) {
        tmpLocation.delete();
        tmpCount--;
        tmpLocation.renameTo(imageItem.getFile());
        imageItem.notifyCache();
        adapter.notifyDataSetChanged();
    }
    @Override
    protected void onCancelled() {
        try {
            if(out != null) out.close();
            tmpLocation.delete();
            tmpCount--;
        } catch (IOException e) {}
    }
}
