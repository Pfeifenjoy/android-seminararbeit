package org.dhbw.arwed_dominic.piccer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Loads the data via the DataBaseHandle (PiccerDatabaseHandler)
 * and fills the List where the images are displayed.
 */
public class ImageItemAdapter extends CursorAdapter {

    private final Context context;
    /**
     * A Set which marks all items which have been marked by the User.
     * This will happen if the user presses on an Item for a long time.
     */
    private Set<Long> selected = new HashSet<Long>();

    private static final SimpleDateFormat ITEM_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat ITEM_TIME_FORMAT = new SimpleDateFormat("hh:mm");


    public ImageItemAdapter(Context context, Cursor cursor, int flag) {
        super(context, cursor, flag);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
    }

    /**
     * Sets all view elements of a ListItem like the image or the Date when it was created.
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvCreationDate = (TextView) view.findViewById(R.id.createdDate);
        TextView tvCreationTime = (TextView) view.findViewById(R.id.createdTime);
        TextView tvTitleLabel = (TextView) view.findViewById(R.id.titleLabel);
        TextView tvTitle = (TextView) view.findViewById(R.id.titleDescription);
        ImageView ivThumbnail = (ImageView) view.findViewById(R.id.thumbnail);


        String creationDate = cursor.getString(cursor.getColumnIndex(PiccerDatabaseHandler.DATE));
        Date created = null;
        try {
            created = ImageItem.DATE_FORMAT.parse(creationDate);
        } catch (ParseException e) {
            //created will be null
        }
        creationDate = ITEM_DATE_FORMAT.format(created);
        tvCreationDate.setText(creationDate);
        String creationTime = ITEM_TIME_FORMAT.format(created);
        tvCreationTime.setText(creationTime);


        long id = cursor.getLong(cursor.getColumnIndex("_id"));
        if(selected.contains(id))
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.black_overlay));
        else view.setBackgroundColor(0);
        String name = cursor.getString(cursor.getColumnIndex(PiccerDatabaseHandler.PATH));

        String title = cursor.getString(cursor.getColumnIndex(PiccerDatabaseHandler.TITLE));
        if(title == null) {
            tvTitle.setVisibility(View.GONE);
            tvTitleLabel.setVisibility(View.GONE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitleLabel.setVisibility(View.VISIBLE);
            tvTitle.setText(title);
        }

        ImageItem imageItem = new ImageItem(context, created, name, title, id);

        if(cancelPotentialWork(ivThumbnail.getId(), ivThumbnail)) {
            final ImageThumbnailLoader imageThumbnailLoader =
                    new ImageThumbnailLoader(ivThumbnail, imageItem);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(context.getResources(), null, imageThumbnailLoader);
            ivThumbnail.setImageDrawable(asyncDrawable);
            imageThumbnailLoader.execute(ivThumbnail.getId());
        }


    }

    /**
     * Checks weather there is already an ImageThumbnailLoader in action.
     * If so this will be canceled.
     * If it is the same ImageThumbnailLoader like you want to add it will not cancel it.
     * @param data
     * @param imageView
     * @return "True" it the no loader is active.
     */
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

    /**
     * Checks wheater the ImageView has an ImageThumbnailLoader. If so this will be returned.
     * @param imageView
     * @return
     */
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

    /**
     * Checks weather an Item was Selected. If so it will deselect it.
     * Otherwise the item  will be selected.
     * @param id
     * @param view
     * @return
     */
    public boolean toggleSelectForItem(long id, View view) {
        if(selected.contains(id)) {
            selected.remove(id);
            view.setBackgroundColor(0);
        }
        else {
            selected.add(id);
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.black_overlay));
        }
        return !selected.isEmpty();
    }

    public void clearSelect() {
        this.selected.clear();
    }

    public Set<Long> getSelectedImageIds() {
        return selected;
    }


}
