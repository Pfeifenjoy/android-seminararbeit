package org.dhbw.arwed_dominic.piccer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by arwed on 24.10.15.
 */
public class ImageItemAdapter extends ArrayAdapter<ImageItem> {
    private List<ImageItem> imageItems;
    private Context context;
    private int resource;
    public ImageItemAdapter(Context context, int resource, List<ImageItem> data) {
        super(context, resource, data);
        this.imageItems = data;
        this.context = context;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View template, ViewGroup parent) {
        ImageItem imageItem = this.getItem(position);

        if(template == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            template = inflater.inflate(this.resource, parent, false);
        }

        TextView listItem = (TextView) template.findViewById(R.id.createdDate);

        listItem.setText(imageItem.getCreated());

        return template;
    }

    @Override
    public void add(ImageItem item) {
        imageItems.add(item);
        notifyDataSetChanged();


    }
}
