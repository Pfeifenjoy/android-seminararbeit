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
import android.widget.Toast;

import java.util.Date;

public class Piccer extends AppCompatActivity {

    public static final int REQUEST_CAMERA = 0;
    public static final String IMAGE_LIST_STATE = "imageList";

    private ImageList mainImageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piccer);

        mainImageList = (ImageList)findViewById(R.id.mainImageList);
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
            Bundle extras = data.getExtras();
            mainImageList.addImage(new Date(), "");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_LIST_STATE, mainImageList.onSaveInstanceState());
    }

}
