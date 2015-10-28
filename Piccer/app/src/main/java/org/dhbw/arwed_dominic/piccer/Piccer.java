package org.dhbw.arwed_dominic.piccer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class Piccer extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final int REQUEST_CAMERA = 0;
    public static final String IMAGE_LIST_STATE = "imageList";
    public static final String CLICKED_IMAGE = "clickedImage";

    private ListView mainImageList;
    private ImageItemAdapter adapter;
    private PiccerDatabaseHandler handler;
    private static ImageItem tmpImage;
    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piccer);

        //Initialize the list
        this.handler = new PiccerDatabaseHandler(this);
        this.adapter = new ImageItemAdapter(this, handler.getImageTableCursor(), 0);
        mainImageList = (ListView)findViewById(R.id.mainImageList);
        mainImageList.setAdapter(adapter);
        mainImageList.setOnItemClickListener(this);
        mainImageList.setOnItemLongClickListener(this);

        if(savedInstanceState != null) {
            Parcelable state = savedInstanceState.getParcelable(IMAGE_LIST_STATE);
            mainImageList.onRestoreInstanceState(state);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_piccer, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_item:
                this.handler.deleteImages(this.adapter.getSelectedImageIds());
                this.adapter.changeCursor(this.handler.getImageTableCursor());
                this.adapter.notifyDataSetChanged();
                break;
            case R.id.share_item: break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void takePicture(View source) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null) {
            this.tmpImage = new ImageItem(this);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, this.tmpImage.getImageUri());
            startActivityForResult(intent, REQUEST_CAMERA);
        } else {
            Context c = getApplicationContext();
            Resources r = getResources();
            CharSequence text = r.getText(R.string.noCamera);
            Toast t = Toast.makeText(c, text, Toast.LENGTH_SHORT);
            t.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            this.tmpImage.updateCreated();
            this.handler.addImage(this.tmpImage);
            this.adapter.changeCursor(this.handler.getImageTableCursor());
            this.mainImageList.post(new Scroller(this.mainImageList, this.adapter.getCount()));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_LIST_STATE, mainImageList.onSaveInstanceState());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent detailView = new Intent(this, ImageDetailView.class);
        detailView.putExtra(CLICKED_IMAGE, "" + id);
        startActivity(detailView);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        this.menu.clear();
        if(this.adapter.toggleSelectForItem(id, view)) {
            getMenuInflater().inflate(R.menu.edit_main_image_list, this.menu);
        }
        else getMenuInflater().inflate(R.menu.menu_piccer, this.menu);
        return true;
    }
}
