package org.dhbw.arwed_dominic.piccer;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/**
 * Created by arwed on 26.10.15.
 */
class AsyncDrawable extends BitmapDrawable {
    private final WeakReference<ImageLoader> imageItemWeakReference;

    public AsyncDrawable(Resources res, Bitmap bitmap, ImageLoader imageLoader) {
        super(res, bitmap);
        this.imageItemWeakReference = new WeakReference<ImageLoader>(imageLoader);
    }

    public ImageLoader getBitmapWorkerTask() {
        return this.imageItemWeakReference.get();
    }
}
