package org.dhbw.arwed_dominic.piccer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by arwed on 24.10.15.
 */
public class ImageItemAdapter extends CursorAdapter {
    private Context context;

    public ImageItemAdapter(Context context, Cursor cursor, int flag) {
        super(context, cursor, flag);
        this.context = context;
    }



    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvCreationDate = (TextView) view.findViewById(R.id.createdDate);
        ImageView ivThumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        String creationDate = cursor.getString(cursor.getColumnIndex(PiccerDatabaseHandler.DATE));
        Date created = null;
        try {
            created = ImageItem.DATE_FORMAT.parse(creationDate);
        } catch (ParseException e) {

        }
        String name = cursor.getString(cursor.getColumnIndex(PiccerDatabaseHandler.PATH));
        ImageItem imageItem = new ImageItem(context, created, name);
        tvCreationDate.setText(imageItem.getCreated());
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.imgProgressbar);

        if(cancelPotentialWork(ivThumbnail.getId(), ivThumbnail)) {
            final ImageLoader imageLoader = new ImageLoader(ivThumbnail, imageItem, progressBar);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), null, imageLoader);
            ivThumbnail.setImageDrawable(asyncDrawable);
            imageLoader.execute(ivThumbnail.getId());
        }


    }
    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final ImageLoader imageLoader = getImageLoader(imageView);

        if (imageLoader != null) {
            final int bitmapData = imageLoader.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != data) {
                // Cancel previous task
                imageLoader.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }
    private static ImageLoader getImageLoader(ImageView imageView) {
       if (imageView != null) {
           final Drawable drawable = imageView.getDrawable();
           if (drawable instanceof AsyncDrawable) {
               final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
               return asyncDrawable.getBitmapWorkerTask();
           }
        }
        return null;
    }

}
