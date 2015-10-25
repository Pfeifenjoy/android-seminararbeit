package org.dhbw.arwed_dominic.piccer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

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
    public static final int THUMBNAIL_WIDTH = 100;
    public static final int THUMBNAIL_HEIGHT = 150;

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
        Bitmap bigImage = BitmapFactory.decodeFile(this.getFile().getAbsolutePath());
        return Bitmap.createScaledBitmap(bigImage, 100, 150, false);
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
