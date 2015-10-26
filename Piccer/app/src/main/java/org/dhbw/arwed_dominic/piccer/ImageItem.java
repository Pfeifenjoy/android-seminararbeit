package org.dhbw.arwed_dominic.piccer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.LruCache;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by arwed on 24.10.15.
 */
public class ImageItem {
    private Date created;
    private File file;
    private String name;
    private Context context;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy mm dd HH:mm:ss");
    public static final int THUMBNAIL_WIDTH = 50;
    public static final int THUMBNAIL_HEIGHT = 100;

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
    public Bitmap getImage() {
        return BitmapFactory.decodeFile(this.getFile().getAbsolutePath());
    }
    public Bitmap getThumbnail() {
        if(cache.get(this.name) == null) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            final String file = getFile().getAbsolutePath();
            BitmapFactory.decodeFile(file, options);
            options.inSampleSize = getInSampleSize(options, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
            options.inJustDecodeBounds = false;
            Bitmap thumbnail = BitmapFactory.decodeFile(file, options);
            cache.put(this.name, thumbnail);
            return thumbnail;
        } else return cache.get(this.name);
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
