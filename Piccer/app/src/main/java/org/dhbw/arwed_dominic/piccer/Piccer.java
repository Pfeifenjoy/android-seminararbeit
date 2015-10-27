package org.dhbw.arwed_dominic.piccer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

public class Piccer extends AppCompatActivity {

    public static final int REQUEST_CAMERA = 0;
    public static final String IMAGE_LIST_STATE = "imageList";

    private ListView mainImageList;
    private ImageItemAdapter adapter;
    private PiccerDatabaseHandler handler;
    private static ImageItem tmpImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piccer);

        //Initialize the list
        this.handler = new PiccerDatabaseHandler(this);
        this.adapter = new ImageItemAdapter(this, handler.getImageTableCursor(), 0);
        mainImageList = (ListView)findViewById(R.id.mainImageList);
        mainImageList.setAdapter(adapter);

        if(savedInstanceState != null) {
            Parcelable state = savedInstanceState.getParcelable(IMAGE_LIST_STATE);
            mainImageList.onRestoreInstanceState(state);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_piccer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
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
            this.adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_LIST_STATE, mainImageList.onSaveInstanceState());
    }

}
