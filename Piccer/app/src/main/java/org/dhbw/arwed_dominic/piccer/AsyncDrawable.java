package org.dhbw.arwed_dominic.piccer;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/**
 * Created by arwed on 26.10.15.
 */
class AsyncDrawable extends BitmapDrawable {
    private final WeakReference<ImageThumbnailLoader> imageItemWeakReference;

    public AsyncDrawable(Resources res, Bitmap bitmap, ImageThumbnailLoader imageThumbnailLoader) {
        super(res, bitmap);
        this.imageItemWeakReference = new WeakReference<ImageThumbnailLoader>(imageThumbnailLoader);
    }

    public ImageThumbnailLoader getBitmapWorkerTask() {
        return this.imageItemWeakReference.get();
    }
}
