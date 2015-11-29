package org.dhbw.arwed_dominic.piccer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
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
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * A representation of an image, with the date etc.
 */
public class ImageItem implements Serializable {
    private Date created;
    private File file;
    /**
     * Name of the file.
     */
    private String fileName;
    private String title;
    private long id;
    private Context context;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy MM dd HH:mm:ss");

    /**
     * A cache to speed up loading thumbnails.
     */
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

    /**
     * Create an Image item by a given uri. This will copy the provided file
     * and create a new Image item of it.
     * @param context
     * @param uri
     */
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

    /**
     * Create a model of an already existing image item.
     * @param context
     * @param created
     * @param fileName
     * @param title
     * @param id
     */
    public ImageItem(Context context, Date created, String fileName, String title, long id) {
        this.context = context;
        this.fileName = fileName;
        this.created = created;
        this.id = id;
        this.title = title;
    }

    /**
     * Create a new image item.
     * @param context
     */
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
        if(cache.get(this.fileName) == null) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            final String file = getFile().getAbsolutePath();
            BitmapFactory.decodeFile(file, options);
            options.inSampleSize = getInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            Bitmap thumbnail = BitmapFactory.decodeFile(file, options);
            cache.put(this.fileName, thumbnail);
            return thumbnail;
        } else return cache.get(this.fileName);
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
        if(cache.get(this.fileName) == null) {
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
            cache.put(this.fileName, thumbnail);
            return thumbnail;
        } else return cache.get(this.fileName);

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
                new File(this.context.getExternalFilesDir("img"), this.fileName) : this.file;
        return this.file;
    }


    public String getName() {
        return this.fileName;
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

    /**
     * Tell the cache that a new version of the imageItem exists.
     */
    public void notifyCache() {
        if(cache.get(this.fileName) != null) {
            cache.remove(this.fileName);
        }
    }

    /**
     * Exports the image into the gallary
     * @throws IOException
     */
    public void saveToGallary() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(getFile());
            File destination = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    + File.separator + fileName + ".png"
                    );
            out = new FileOutputStream(destination);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            try {
                if(in != null) in.close();
                if(out != null) out.close();
            } catch(IOException e) {}
        }
    }

    /**
     * Create a unique file name.
     */
    private void generateName() {
        this.fileName = "image-" + UUID.randomUUID() + ".png";
    }
}
