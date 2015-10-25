package org.dhbw.arwed_dominic.piccer;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;



/**
 * Created by arwed on 25.10.15.
 */
public class ImageLoader extends AsyncTask<Object, Void, Bitmap> {
    private ImageView imageView;
    private ImageItem imageItem;
    private String path;

    public ImageLoader(ImageView imageView, ImageItem imageItem) {
        this.imageView = imageView;
        this.imageItem = imageItem;
        //this.path = imageView.getTag().toString();
    }

    @Override
    protected Bitmap doInBackground(Object... params) {
        Bitmap bitmap = imageItem.getThumbnail();
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
//        if(!imageView.getTag().toString().equals(this.path)) {
//            return;
//        }
        if(result != null && imageView != null) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(result);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPreExecute() {
        imageView.setImageResource(R.drawable.ic_filter_black_18dp);
        imageView.setVisibility(View.VISIBLE);

    }
}