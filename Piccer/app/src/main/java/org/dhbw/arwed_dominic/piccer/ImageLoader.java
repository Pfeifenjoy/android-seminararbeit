package org.dhbw.arwed_dominic.piccer;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.lang.ref.WeakReference;


/**
 * Created by arwed on 25.10.15.
 */
public class ImageLoader extends AsyncTask<Integer, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private final WeakReference<ProgressBar> progressBarWeakReference;
    private final ImageItem imageItem;
    public int data;

    public ImageLoader(ImageView imageView, ImageItem imageItem, ProgressBar progressBar) {
        this.imageViewReference = new WeakReference<ImageView>(imageView);
        this.imageItem = imageItem;
        this.progressBarWeakReference = new WeakReference<ProgressBar>(progressBar);
    }

    @Override
    protected Bitmap doInBackground(Integer... params) {
        this.data = params[0];
        final Bitmap bitmap = this.imageItem.getThumbnail();
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
            final ProgressBar progressBar = progressBarWeakReference.get();
            if(progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onPreExecute() {
        final ImageView imageView = imageViewReference.get();
        if(imageView != null) {
            imageView.setVisibility(View.GONE);
        }
        final ProgressBar progressBar = progressBarWeakReference.get();
        if(progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }
}
