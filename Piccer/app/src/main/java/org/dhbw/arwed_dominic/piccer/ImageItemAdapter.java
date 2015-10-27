package org.dhbw.arwed_dominic.piccer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Created by arwed on 24.10.15.
 */
public class ImageItemAdapter extends CursorAdapter {
    private Context context;

    public ImageItemAdapter(Context context, Cursor cursor, int flag) {
        super(context, cursor, flag);
        this.context = context;
    }

    private Set<Long> selected = new HashSet<Long>();


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
        long id = cursor.getLong(cursor.getColumnIndex("_id"));
        if(selected.contains(id))
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.black_overlay));
        else view.setBackgroundColor(0);
        String name = cursor.getString(cursor.getColumnIndex(PiccerDatabaseHandler.PATH));
        ImageItem imageItem = new ImageItem(context, created, name);
        tvCreationDate.setText(imageItem.getCreated());

        if(cancelPotentialWork(ivThumbnail.getId(), ivThumbnail)) {
            final ImageThumbnailLoader imageThumbnailLoader = new ImageThumbnailLoader(ivThumbnail, imageItem);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), null, imageThumbnailLoader);
            ivThumbnail.setImageDrawable(asyncDrawable);
            imageThumbnailLoader.execute(ivThumbnail.getId());
        }


    }
    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final ImageThumbnailLoader imageThumbnailLoader = getImageLoader(imageView);

        if (imageThumbnailLoader != null) {
            final int bitmapData = imageThumbnailLoader.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != data) {
                // Cancel previous task
                imageThumbnailLoader.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }
    private static ImageThumbnailLoader getImageLoader(ImageView imageView) {
       if (imageView != null) {
           final Drawable drawable = imageView.getDrawable();
           if (drawable instanceof AsyncDrawable) {
               final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
               return asyncDrawable.getBitmapWorkerTask();
           }
        }
        return null;
    }

    public void toggleSelectForItem(long id) {
        if(selected.contains(id)) selected.remove(id);
        else selected.add(id);
        notifyDataSetChanged();
    }

}
