package org.dhbw.arwed_dominic.piccer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by arwed on 24.10.15.
 */
public class ImageList extends ListView {

    private List<ImageItem> items = new ArrayList<ImageItem>();
    private ImageItemAdapter adapter;

    public ImageList(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.init(context);
    }

    public ImageList(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        this.init(context);
    }

    public ImageList(Context context) {
        super(context);
        this.init(context);
    }

    public void init(Context context) {
        PiccerDatabaseHandler handler = new PiccerDatabaseHandler(context);

        this.adapter = new ImageItemAdapter(context, handler.getImageTableCursor(), 0);
        setAdapter(this.adapter);
    }

    public void addImage(Date created, String path) {
        ImageItem item  = new ImageItem(created, path);
        items.add(item);
        adapter.notifyDataSetChanged();
    }

}
