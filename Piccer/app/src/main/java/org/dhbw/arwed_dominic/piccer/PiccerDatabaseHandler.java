package org.dhbw.arwed_dominic.piccer;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Created by arwed on 25.10.15.
 */
public class PiccerDatabaseHandler extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DBNAME = "piccer";
    public static final String TABLE_IMAGES = "images";

    //Images Table
    public static final String KEY_ID = "_id";
    public static final String DATE = "date";
    public static final String PATH = "path";

    public PiccerDatabaseHandler(Context context) {
        super(context, DBNAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_IMAGES + " ( "
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + DATE + " TEXT, "
                + PATH + " TEXT "
                + " );";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES + ";");
        onCreate(db);
    }

    public Cursor getImageTableCursor() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_IMAGES + ";", null);
        return c;
    }

    public ImageItem getImage(Context context, int id) throws Resources.NotFoundException {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_IMAGES + " WHERE _id = " + id + ";", null);
        if(c.getCount() <= 0) throw new Resources.NotFoundException("Could not find database entry of image.");
        c.moveToFirst();
        DateFormat dateFormat = ImageItem.DATE_FORMAT;
        Date date = null;
        try {
            date = dateFormat.parse(c.getString(c.getColumnIndex(DATE)));
        } catch (ParseException e) {}
        String name = c.getString(c.getColumnIndex(PATH));

        return new ImageItem(context, date, name);
    }

    public void addImage(ImageItem imageItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATE, imageItem.getCreated());
        values.put(PATH, imageItem.getName());
        db.insert(TABLE_IMAGES, null, values);
        db.close();
    }

}
