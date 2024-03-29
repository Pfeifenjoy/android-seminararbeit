package org.dhbw.arwed_dominic.piccer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;


public class Piccer extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public static final int REQUEST_CAMERA = 0;
    public static final int REQUEST_GALLARY = 1;
    public static final String IMAGE_LIST_STATE = "imageList";
    public static final String CLICKED_IMAGE = "clickedImage";
    public static final String ORDER = "listOrder";

    private ListView mainImageList;
    private ImageItemAdapter adapter;
    private PiccerDatabaseHandler handler;
    private static ImageItem tmpImage;
    private Menu menu;
    private Parcelable listState;
    private boolean order = PiccerDatabaseHandler.DESCENDING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piccer);

        //Initialize the list
        this.handler = new PiccerDatabaseHandler(this);

        ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mainImageList = (ListView)findViewById(R.id.mainImageList);

        adapter = new ImageItemAdapter(this, handler.getImageTableCursor("_id", order), 0);
        mainImageList.setAdapter(adapter);

        mainImageList.setOnItemClickListener(this);
        mainImageList.setOnItemLongClickListener(this);
        mainImageList.setAdapter(adapter);

        //Restore the old state of the list e.g. the position
        if(savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(IMAGE_LIST_STATE);
            mainImageList.onRestoreInstanceState(listState);
            order = savedInstanceState.getBoolean(ORDER);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_piccer, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Set<Long> ids= this.adapter.getSelectedImageIds();
        switch (item.getItemId()) {
            case R.id.delete_item:
                this.handler.deleteImages(this.adapter.getSelectedImageIds());
                updateList();
                break;
            case R.id.share_item:

                ArrayList<Uri> imageUris = new ArrayList<>();

                for (long id : ids) {
                    ImageItem imageItem = this.handler.getImage(this, id);
                    imageUris.add(imageItem.getImageUri());
                }

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                sendIntent.putParcelableArrayListExtra(sendIntent.EXTRA_STREAM, imageUris);
                sendIntent.putExtra(sendIntent.EXTRA_TEXT, R.string.sendMessage);
                sendIntent.setType("image/*");
                sendIntent.addFlags(sendIntent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share)));
                break;

            case R.id.saveToGallery:
                boolean exportedAll = true;
                for (long id : ids) {
                    ImageItem imageItem = this.handler.getImage(this, id);
                    try {
                        imageItem.saveToGallary();
                    } catch (FileNotFoundException e) {
                        Log.w("Piccer", "Export Issue", e);
                        exportedAll = false;
                    }
                }
                if(exportedAll)
                    Toast.makeText(getBaseContext(), R.string.addToGalery , Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getBaseContext(), R.string.couldNotExportImages, Toast.LENGTH_SHORT).show();
                break;

            case R.id.changeOrder:
                this.order = !this.order;
                updateList();
                break;

        }
        this.adapter.clearSelect();
        this.menu.clear();
        getMenuInflater().inflate(R.menu.menu_piccer, this.menu);
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
    public void loadPicture(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLARY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CAMERA && resultCode == RESULT_OK ) {
            addImage(this.tmpImage);

        }
        if(requestCode == REQUEST_GALLARY && resultCode == RESULT_OK){
            Uri selectedImageUri = data.getData();
            this.tmpImage = new ImageItem(this, selectedImageUri);
            addImage(this.tmpImage);
        }
    }

    private void saveImage(ImageItem imageItem) {
        File file = this.tmpImage.getFile();
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            String sDate = exif.getAttribute(ExifInterface.TAG_DATETIME);
            Date date;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            if(sDate == null) {
                date = new Date();
                exif.setAttribute(ExifInterface.TAG_DATETIME, dateFormat.format(date));
            }
            else date = dateFormat.parse(sDate);
            tmpImage.setDate(date);
            this.handler.addImage(imageItem);
            updateList();
            this.mainImageList.post(new Scroller(this.mainImageList, this.adapter.getCount(), order));

        } catch (IOException e) {
            Toast.makeText(this, R.string.couldNotLoadImage, Toast.LENGTH_SHORT);
            Log.w("Piccer", "An IOException occured. The image file has no exif.", e);
        } catch (ParseException e) {
            Log.e("Piccer", "The date of the exif file could not be parsed", e);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_LIST_STATE, mainImageList.onSaveInstanceState());
        outState.putBoolean(ORDER, order);
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

    public void addImage(final ImageItem imageItem){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pleaseSelectName);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected;
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString();
                imageItem.setTitle(title);
                saveImage(imageItem);
            }
        });
        builder.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveImage(imageItem);
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onBackPressed() {
        if(adapter.getSelectedImageIds().size() > 0) {
            adapter.clearSelect();
            updateList();
        }
        else finish();
    }

    private void updateList() {
        listState = mainImageList.onSaveInstanceState();
        adapter = new ImageItemAdapter(this, handler.getImageTableCursor("_id", order), 0);
        mainImageList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        mainImageList.refreshDrawableState();
        if(listState!=null) mainImageList.onRestoreInstanceState(listState);
    }
}
