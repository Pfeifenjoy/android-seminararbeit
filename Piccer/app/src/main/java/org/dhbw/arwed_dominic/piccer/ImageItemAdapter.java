package org.dhbw.arwed_dominic.piccer;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

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
        String creationDate = cursor.getString(cursor.getColumnIndex(PiccerDatabaseHandler.DATE));
        tvCreationDate.setText(creationDate);
    }

}
