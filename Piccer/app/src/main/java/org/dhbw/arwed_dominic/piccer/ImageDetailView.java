package org.dhbw.arwed_dominic.piccer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

/**
 * An fullscreen activity which displays a single image an an ImageView.
 * It gives the user the possibility of manipulating the image.
 */
public class ImageDetailView extends AppCompatActivity {
    private PiccerDatabaseHandler handler;
    /**
     * Holds the current information about the image which is displayed.
     */
    private ImageItem imageItem;
    private ImageView contentView;
    /**
     * Saves the last rotation of the image.
     */
    private float rotation;
    /**
     * A task which rotates the image.
     */
    private AsyncRotator rotator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail_view);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        this.handler = new PiccerDatabaseHandler(this);
        contentView = (ImageView) findViewById(R.id.fullscreenImageView);

        //Load the image.
        long id = Long.parseLong(getIntent().getStringExtra(Piccer.CLICKED_IMAGE));
        imageItem = handler.getImage(this, id);

        if(!imageItem.getFile().exists()) {
            Toast.makeText(this, R.string.couldNotLoadImage, Toast.LENGTH_SHORT).show();
            finish();
        }
        //Place the image.
        contentView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        try {
            contentView.setImageURI(imageItem.getImageUri());
        } catch (OutOfMemoryError e) {
            Toast.makeText(this, R.string.couldNotLoadImage, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_main_image_list, menu);
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
                boolean exported= true;
                try {
                    imageItem.saveToGallary();
                } catch (IOException e) {
                    Log.w("Piccer", "Export Gallary exception", e);
                    exported = false;
                }
                if(exported)
                    Toast.makeText(getBaseContext(), R.string.addToGalery , Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getBaseContext(), R.string.couldNotExport, Toast.LENGTH_SHORT).show();
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

    /**
     * Change the title of an image.
     * @param _
     */
    public void setTitle(View _) {
        //TODO

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
