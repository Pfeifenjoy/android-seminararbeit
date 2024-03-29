package org.dhbw.arwed_dominic.piccer;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    public static final String TITLE = "title";

    public static boolean ASCENDING = true;
    public static boolean DESCENDING = !ASCENDING;

    private final Context context;

    public PiccerDatabaseHandler(Context context) {
        super(context, DBNAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_IMAGES + " ( "
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + DATE + " TEXT, "
                + PATH + " TEXT, "
                + TITLE + " TEXT "
                + " );";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES + ";");
        onCreate(db);
    }

    public Cursor getImageTableCursor(String field, boolean order) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_IMAGES + " ORDER BY " + field + " " +
                        (order ? "ASC" : "DESC") + ";";
        Cursor c = db.rawQuery(query, null);
        return c;
    }

    public ImageItem getImage(Context context, long id) throws Resources.NotFoundException {
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
        String title = c.getString(c.getColumnIndex(TITLE));

        db.close();
        return new ImageItem(context, date, name, title, id);
    }

    public void addImage(ImageItem imageItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATE, imageItem.getCreated());
        values.put(PATH, imageItem.getName());
        values.put(TITLE, imageItem.getTitle());
        db.insert(TABLE_IMAGES, null, values);
        db.close();
    }

    public void deleteImage(long imageId) {
        Set<Long> images = new HashSet<Long>();
        images.add(imageId);
        deleteImages(images);
    }

    public void deleteImages(Set<Long> imageIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + PATH + " FROM " + TABLE_IMAGES
                + " WHERE _id in (";
        String subquery = "";
        for(long id : imageIds) {
            subquery += id + ", ";
        }
        subquery = subquery.substring(0, subquery.length() - 2);
        query += subquery;
        query += ")";
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        do {
            File file = new File(this.context.getExternalFilesDir("img"), c.getString(c.getColumnIndex(PATH)));
            file.delete();
        } while (c.moveToNext());
        query = "DELETE FROM " + TABLE_IMAGES + " WHERE _id in (" + subquery + ")";
        db.execSQL(query);
        db.close();
    }

    public void updateImageItem(ImageItem imageItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DATE, imageItem.getCreated());
        values.put(PATH, imageItem.getName());
        values.put(TITLE, imageItem.getTitle());
        db.update(TABLE_IMAGES, values, "_id=" + imageItem.getId(), null);
        db.close();
    }
}
