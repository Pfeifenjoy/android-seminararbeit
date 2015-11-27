package org.dhbw.arwed_dominic.piccer;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by arwed on 24.10.15.
 */
public class ImageItem implements Serializable {
    private Date created;
    private File file;
    private String name;
    private String title;
    private long id;
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

    public ImageItem(Context context, Uri uri) {
        this.context = context;
        generateName();
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            InputStream src = context.getContentResolver().openInputStream(uri);
            File dest = getFile();

            in = new BufferedInputStream(src);
            out = new BufferedOutputStream(new FileOutputStream(dest));

            byte[] buffer = new byte[1024];
            in.read(buffer);
            do {
                out.write(buffer);
            } while (in.read(buffer) != -1);

            notifyCache();
        } catch (FileNotFoundException e) {
            Toast.makeText(context, R.string.couldNotLoadImage, Toast.LENGTH_SHORT).show();
            Log.w("Piccer", "error", e);
        } catch (IOException e) {
            Toast.makeText(context, R.string.couldNotLoadImage, Toast.LENGTH_SHORT).show();
            Log.w("Piccer", "error", e);
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e){}
        }
    }

    public ImageItem(Context context, Date created, String name, String title, long id) {
        this.context = context;
        this.name = name;
        this.created = created;
        this.id = id;
        this.title = title;
    }

    public ImageItem(Context context) {
        generateName();
        this.context = context;
    }

    public void setDate(Date date) {
        this.created = date;
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
            Bitmap thumbnail = null;
            try {
                options.inJustDecodeBounds = true;
                BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(file, false);
                final int y = bitmapRegionDecoder.getHeight();
                final int x = bitmapRegionDecoder.getWidth();
                final Rect rect = y > x ?
                        new Rect(0, (int) (offset * y), x, (int) (y - offset * y))
                        : new Rect(0, 0, x, y);
                bitmapRegionDecoder.decodeRegion(rect, options);
                options.inSampleSize = getInSampleSize(options, width, height);
                options.inJustDecodeBounds = false;
                thumbnail = bitmapRegionDecoder.decodeRegion(rect, options);
            } catch (IOException e) {
                thumbnail = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_broken_image_black_24dp);
            }
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

    public long getId() {
        return this.id;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void notifyCache() {
        if(cache.get(this.name) != null) {
            cache.remove(this.name);
        }
    }

    public void saveToGallary() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, getTitle());
        values.put(MediaStore.Images.Media.DATE_TAKEN, getCreated());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.MediaColumns.DATA, getFile().getAbsolutePath());
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
    private void generateName() {
        this.name = "image-" + UUID.randomUUID() + ".png";
    }
}
