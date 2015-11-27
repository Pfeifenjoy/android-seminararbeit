package org.dhbw.arwed_dominic.piccer;

import org.dhbw.arwed_dominic.piccer.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.ViewAnimator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ImageDetailView extends AppCompatActivity {
    private PiccerDatabaseHandler handler;
    private Menu menu;
    private ImageItem imageItem;
    private ImageView contentView;
    private float rotation;
    private AsyncRotator rotator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_detail_view);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.handler = new PiccerDatabaseHandler(this);


        contentView = (ImageView) findViewById(R.id.fullscreenImageView);

        long id = Long.parseLong(getIntent().getStringExtra(Piccer.CLICKED_IMAGE));
        PiccerDatabaseHandler piccerDatabaseHandler = new PiccerDatabaseHandler(this);
        imageItem = piccerDatabaseHandler.getImage(this, id);

        contentView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        try {
            contentView.setImageURI(imageItem.getImageUri());
        } catch (OutOfMemoryError e) {
            //TODO
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_main_image_list, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_item:
                this.handler.deleteImage(imageItem.getId());
                finish();
                break;
            case R.id.saveToGallery:
                imageItem.saveToGallary();
                Toast.makeText(getBaseContext(), R.string.addToGalery , Toast.LENGTH_SHORT).show();
                break;
            case R.id.share_item:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_STREAM, imageItem.getImageUri());
                sendIntent.setType("image/png");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share)));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void rotateLeft(View _) {
        rotation -= 90;
        rotate();
    }

    public void rotateRight(View _) {
        rotation += 90;
        rotate();
    }

    public void rotate() {
        contentView.setPivotX(contentView.getWidth() / 2);
        contentView.setPivotY(contentView.getHeight() / 2);
        contentView.setRotation(rotation);
        if(rotator != null)
            rotator.cancel(true);
        rotator = new AsyncRotator(this, imageItem, (int) rotation);
        rotator.execute();
    }

    public void setTitle(View _) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pleaseSelectName);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected;
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString();
                imageItem.setTitle(title);
                handler.updateImageItem(imageItem);
            }
        });
        builder.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}
