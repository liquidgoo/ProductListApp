package by.bsuir.productlistapp;

import static by.bsuir.productlistapp.MainActivity.dbHelper;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;


public class Song {
    private int id;
    private String name;
    private String album;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;

        dbHelper = new DatabaseHelper(MainActivity.mainActivityContext);
        SQLiteDatabase sQlitedatabase = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.KEY_comment, comment);
        String where = DatabaseHelper.KEY_id + " = " + String.valueOf(id);
        sQlitedatabase.update(DatabaseHelper.TABLE_SONGS, contentValues, where, null);
        dbHelper.close();
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }




    public Song(int id, String name, String album, long duration, int trackNumber, String artist, double price, boolean isFavourite, String comment, String coverUrl, String blabla) {
        this.id = id;
        this.name = name;
        this.album = album;
        this.duration = duration;
        this.trackNumber = trackNumber;
        this.artist = artist;
        this.price = price;
        this.isFavourite = isFavourite;
        this.comment = comment;
        this.coverUrl = coverUrl;
        this.webPagePartID = blabla;
    }

    private long duration;
    private int trackNumber;
    private String artist;
    private double price;
    private boolean isFavourite;
    private String comment;
    private String coverUrl;
    private String webPagePartID;

    public String getWebPagePartID() {
        return webPagePartID;
    }

    public void setWebPagePartID(String webPagePartID) {
        this.webPagePartID = webPagePartID;
    }
}
