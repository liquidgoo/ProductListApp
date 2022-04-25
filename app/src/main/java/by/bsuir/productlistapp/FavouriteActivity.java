package by.bsuir.productlistapp;

import static by.bsuir.productlistapp.MainActivity.dbHelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class FavouriteActivity extends AppCompatActivity {

    public static void updateSongFavouriteness(int id, boolean isFav){
        dbHelper = new DatabaseHelper(MainActivity.mainActivityContext);
        SQLiteDatabase sQlitedatabase = dbHelper.getWritableDatabase();
        sQlitedatabase.execSQL("update " + DatabaseHelper.TABLE_SONGS +
                " set " + DatabaseHelper.KEY_isFavourite + " = " +
                (isFav ? String.valueOf(1) : String.valueOf(0)) +
                " where " +
                DatabaseHelper.KEY_id + " = " + String.valueOf(id));
        dbHelper.close();

        if (favouriteListView != null) setAdapterForList();
    }

    public static void addFavouriteSong(Song song){
        favouriteList.add(song);
        updateSongFavouriteness(song.getId(), true);
    }

    public static void removeFavouriteSong(Song song){
        for (int i = 0; i < favouriteList.size(); ++i){
            if (favouriteList.get(i).getId() == song.getId()){
                favouriteList.remove(favouriteList.get(i));
                break;
            }
        }
        updateSongFavouriteness(song.getId(), false);
    }

    private void getRidOfTopBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
    }

    private static void setAdapterForList(){
        SongAdapter adapter = new SongAdapter(MainActivity.mainActivityContext, 0, favouriteList);
        favouriteListView.setAdapter(adapter);
    }

    public static ArrayList<Song> favouriteList = new ArrayList<>();
    private static ListView favouriteListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getRidOfTopBar();
        setContentView(R.layout.activity_favourite);

        favouriteListView = findViewById(R.id.favouriteListView);
        SongAdapter adapter = new SongAdapter(MainActivity.mainActivityContext, 0, favouriteList);
        favouriteListView.setAdapter(adapter);
        favouriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Song selectedSong = (Song) favouriteListView.getItemAtPosition(position);
                Intent showDetail = new Intent(MainActivity.mainActivityContext, DetailActivity.class);
                showDetail.putExtra("id", selectedSong.getId() - 1);
                showDetail.putExtra("favourite", true);
                startActivity(showDetail);
            }
        });
    }
}