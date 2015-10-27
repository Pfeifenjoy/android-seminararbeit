package org.dhbw.arwed_dominic.piccer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.util.LruCache;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arwed on 24.10.15.
 */
public class ImageItem implements Serializable {
    private Date created;
    private File file;
    private String name;
    private Context context;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy MM dd HH:mm:ss");

    private static final LruCache<String, Bitmap> cache;
    static {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 4;
        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public ImageItem(Context context, Date created, String name) {
        this.context = context;
        this.name = name;
        this.created = created;
    }

    public ImageItem(Context context) {
        updateCreated();
        this.name = "image-" + getCreated() + ".png";
        this.context = context;
    }
    public void updateCreated () {
        this.created = new Date();
    }
    public String getCreated() {
        return DATE_FORMAT.format(this.created);
    }

    public Uri getImageUri() {
        return Uri.fromFile(getFile());
    }
    public Bitmap getImage(int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        final String file = getFile().getAbsolutePath();
        BitmapFactory.decodeFile(file, options);
        options.inSampleSize = getInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file, options);
    }
    public Bitmap getThumbnail(int width, int height) {
        if(cache.get(this.name) == null) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            final String file = getFile().getAbsolutePath();
            BitmapFactory.decodeFile(file, options);
            options.inSampleSize = getInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            Bitmap thumbnail = BitmapFactory.decodeFile(file, options);
            cache.put(this.name, thumbnail);
            return thumbnail;
        } else return cache.get(this.name);
    }

    /**
     * Loads a Thumbnail for the image item.
     * This method only return a part, which is cropped at the y axis, of the image.
     * @param width {Integer}
     * @param height {Integer}
     * @param offset {Double} The factor which will be cropped at the buttom and at the top.
     * @return {Bitmap}
     */
    public Bitmap getThumbnail(int width, int height, double offset) {
        if(cache.get(this.name) == null) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            final String file = getFile().getAbsolutePath();
            try {
                options.inJustDecodeBounds = true;
                BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(file, false);
                final int y = bitmapRegionDecoder.getHeight();
                final int x = bitmapRegionDecoder.getWidth();
                final Rect rect = new Rect(0, (int) (offset * y), x, (int) (y - offset * y));
                bitmapRegionDecoder.decodeRegion(rect, options);
                options.inSampleSize = getInSampleSize(options, width, height);
                options.inJustDecodeBounds = false;
                Bitmap thumbnail = bitmapRegionDecoder.decodeRegion(rect, options);
                cache.put(this.name, thumbnail);
                return thumbnail;
            } catch (IOException e) {
            } //TODO create image if it fails.
        } else return cache.get(this.name);

        return null;
    }

    private int getInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public File getFile() {
        this.file = this.file == null ?
                new File(this.context.getExternalFilesDir("img"), this.name) : this.file;
        return this.file;
    }


    public String getName() {
        return this.name;
    }
}
