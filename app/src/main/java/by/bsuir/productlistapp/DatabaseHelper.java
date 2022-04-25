package by.bsuir.productlistapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "dbProducts";
    public static final String TABLE_SONGS = "songs";
    public static final String TABLE_BASKET = "basket";

    public static String KEY_id = "_id";
    public static String KEY_name = "name";
    public static String KEY_album = "album";
    public static String KEY_duration = "duration";
    public static String KEY_trackNumber = "trackNumber";
    public static String KEY_artist = "artist";
    public static String KEY_price = "price";
    public static String KEY_isFavourite = "isFavourite";
    public static String KEY_comment = "comments";
    public static String KEY_cover = "cover";
    public static String KEY_webviewid = "webviewid";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + TABLE_SONGS + "(" +
                        KEY_id + " integer primary key," +
                        KEY_name + " text," +
                        KEY_album + " text," +
                        KEY_duration + " integer," +
                        KEY_trackNumber + " integer," +
                        KEY_artist + " text," +
                        KEY_price + " real, " +
                        KEY_isFavourite + " integer," +
                        KEY_comment + " text," +
                        KEY_cover + " text," +
                        KEY_webviewid + " text"
                + ")");

        sqLiteDatabase.execSQL("create table " + TABLE_BASKET + "(" +
                KEY_id + " integer primary key," +
                KEY_name + " text," +
                KEY_album + " text," +
                KEY_duration + " integer," +
                KEY_trackNumber + " integer," +
                KEY_artist + " text," +
                KEY_price + " real, " +
                KEY_isFavourite + " integer," +
                KEY_comment + " text," +
                KEY_cover + " text," +
                KEY_webviewid + " text"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_SONGS);
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_BASKET);
        onCreate(sqLiteDatabase);
    }
}
