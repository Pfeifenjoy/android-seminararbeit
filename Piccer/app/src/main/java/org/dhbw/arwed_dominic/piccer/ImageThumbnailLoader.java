package org.dhbw.arwed_dominic.piccer;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;


/**
 * Created by arwed on 25.10.15.
 */
public class ImageThumbnailLoader extends AsyncTask<Integer, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private final ImageItem imageItem;
    public int data;
    public static int THUMBNAIL_WIDTH = 200;
    public static int THUMBNAIL_HEIGHT = 200;

    public ImageThumbnailLoader(ImageView imageView, ImageItem imageItem) {
        this.imageViewReference = new WeakReference<ImageView>(imageView);
        this.imageItem = imageItem;
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        this.data = params[0];
        final Bitmap bitmap = this.imageItem.getThumbnail(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, 0.38);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if(isCancelled()) return;
        if(result != null && this.imageViewReference != null) {
            final ImageView imageView = imageViewReference.get();
            if(imageView != null) {
                imageView.setImageBitmap(result);
                imageView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onPreExecute() {
    }
}
