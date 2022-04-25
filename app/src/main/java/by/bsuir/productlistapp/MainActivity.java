package by.bsuir.productlistapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.textclassifier.ConversationActions;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.UserPrivate;
import kaaes.spotify.webapi.android.models.UserPublic;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "469f12a3207c40e887694495ba3fe2cb";
    public static final String REDIRECT_URI = "http://localhost:3005/";
    public static final int REQUEST_CODE = 1337;
    public static String ACCESS_TOKEN;
    public static SpotifyApi API;

    private static HttpURLConnection connection;
    private static final String USD_IN_BYN_URL = "https://www.nbrb.by/api/exrates/rates/431";
    private StringBuffer responseContent;

    public static double USD_IN_BYN = 0.0;
    public static String userProfileName = "";
    public static String userEmail = "";
    public static Bitmap userProfileBitmap = null;

    public static double GLOBAL_CURRENCY_COEFF = 1.0;

    public static final double GLOBAL_CURRENCY_USD = 1.0;
    public static double GLOBAL_CURRENCY_BYN;

    public static DatabaseHelper dbHelper;
    public static ArrayList<Song> songsList = new ArrayList<Song>();


    public static boolean IS_USD = true;


    public static boolean peripheryIsSelectedFilter = true;

    public static long PERIPHERY_SONGS_COUNT = 0;
    public static long ALL_SONGS_COUNT = 0;
    private boolean notAllTracks = false;
    private long totalTracks;

    public static SongCoverImageLRUCache lru = new SongCoverImageLRUCache(1024 * 1024 / 8);

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ProfileFragment.getCurrentLocation();
            }
        }
    }

    public static Context mainActivityContext;

    private void getRidOfTopBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
    }

    private void setupBottomMenu() {
        BottomNavigationView bottomNavigationItemView = findViewById(R.id.bottomNavigationView);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationItemView, navController);
    }

    private void setStaticMainActivityReference() {
        mainActivityContext = this;
    }

    private void handleDayNightTheme() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivityContext);
        boolean dayTheme = sharedPrefs.getBoolean("IS_DAY_THEME", true);

        if (dayTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    private void handleCurrencyState() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivityContext);
        boolean isUsd = sharedPrefs.getBoolean("IS_USD", true);

        if (isUsd) {
            MainActivity.IS_USD = true;
            GLOBAL_CURRENCY_COEFF = GLOBAL_CURRENCY_USD;
        } else {
            MainActivity.IS_USD = false;
            GLOBAL_CURRENCY_COEFF = GLOBAL_CURRENCY_BYN;
        }
    }

    private void handleSpotifyAuthorization() {
        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"streaming", "user-read-email", "user-library-read"});
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);

    }

    private void handleUsdInBynCurrency() {
        network_available = true;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL(USD_IN_BYN_URL);
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int status = connection.getResponseCode();
                Log.e("CONNECTION STATUS", String.valueOf(status));

                BufferedReader reader;
                String line;
                responseContent = new StringBuffer();

                if (status > 299) {
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                } else {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        responseContent.append(line);
                    }
                    reader.close();
                }
                Log.e("JSON RESPONSE", responseContent.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }
        });

        try {
            executor.shutdown();
            executor.awaitTermination(7, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            JSONObject jsonObject = new JSONObject(responseContent.toString());
            USD_IN_BYN = jsonObject.getDouble("Cur_OfficialRate");
            GLOBAL_CURRENCY_BYN = USD_IN_BYN;
            Log.e("USDINBYN", String.valueOf(USD_IN_BYN));
        } catch (Exception e) {
            network_available = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStaticMainActivityReference();
        getRidOfTopBar();
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        handleUsdInBynCurrency();
        handleCurrencyState();

        if (isNetworkAvailable()) {
            handleSpotifyAuthorization();
        }


        handleDayNightTheme();
        setupBottomMenu();
        fillSongArrayList();
        dbHelper.close();

        Log.i("MainActivity", "Background thread started");
    }

    private void fillSongArrayList() {
        SQLiteDatabase sQlitedatabase = dbHelper.getWritableDatabase();

        Cursor cursor = sQlitedatabase.query(
                DatabaseHelper.TABLE_SONGS,
                null, null,
                null, null,
                null, null);

        //ВНИМАНИЕ! КОСТЫЛЬ
        songsList.clear();
        PERIPHERY_SONGS_COUNT = 0;
        ALL_SONGS_COUNT = 0;
        FavouriteActivity.favouriteList.clear();
        //ВНИМАНИЕ! КОСТЫЛЬ
        if (cursor.moveToFirst()) {
            int idInd = cursor.getColumnIndex(DatabaseHelper.KEY_id);
            int nameInd = cursor.getColumnIndex(DatabaseHelper.KEY_name);
            int albumInd = cursor.getColumnIndex(DatabaseHelper.KEY_album);
            int durationInd = cursor.getColumnIndex(DatabaseHelper.KEY_duration);
            int trackNumberInd = cursor.getColumnIndex(DatabaseHelper.KEY_trackNumber);
            int artistInd = cursor.getColumnIndex(DatabaseHelper.KEY_artist);
            int priceInd = cursor.getColumnIndex(DatabaseHelper.KEY_price);
            int isFavouriteInd = cursor.getColumnIndex(DatabaseHelper.KEY_isFavourite);
            int commentInd = cursor.getColumnIndex(DatabaseHelper.KEY_comment);
            int coverInd = cursor.getColumnIndex(DatabaseHelper.KEY_cover);
            int webviewidInd = cursor.getColumnIndex(DatabaseHelper.KEY_webviewid);

            do {
                songsList.add(new Song(
                        cursor.getInt(idInd),
                        cursor.getString(nameInd),
                        cursor.getString(albumInd),
                        cursor.getLong(durationInd),
                        cursor.getInt(trackNumberInd),
                        cursor.getString(artistInd),
                        cursor.getDouble(priceInd),
                        (cursor.getInt(isFavouriteInd) == 1) ? true : false,
                        cursor.getString(commentInd),
                        cursor.getString(coverInd),
                        cursor.getString(webviewidInd)
                ));

                if (cursor.getString(artistInd).toLowerCase(Locale.ROOT).contains("periphery")) {
                    PERIPHERY_SONGS_COUNT++;
                }
                ALL_SONGS_COUNT++;


                if (cursor.getInt(isFavouriteInd) == 1) {
                    FavouriteActivity.addFavouriteSong(songsList.get(songsList.size() - 1));
                }

            } while (cursor.moveToNext());
        }
    }

    //private Bitmap downloadBitmapFromUrl(String Url){
    //    return null;
    //}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);


        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    ACCESS_TOKEN = response.getAccessToken();
                    Log.d("MyActivity", ACCESS_TOKEN);
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }

            API = new SpotifyApi();
            API.setAccessToken(ACCESS_TOKEN);


            handleSpotifyUserProfile();
        }

    }


    static int indexID = 1;
    private boolean requestDone;

    private void handleSpotifyUserProfile() {
        SpotifyService spotify = MainActivity.API.getService();

        spotify.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                userProfileName = userPrivate.display_name;
                userEmail = userPrivate.email;
                Image userImage = userPrivate.images.get(0);

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    try {
                        URL url = new URL(userImage.url);
                        userProfileBitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        userProfileBitmap = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                        userProfileBitmap = null;
                    }
                });

                try {
                    executor.shutdown();
                    executor.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        SQLiteDatabase sQlitedatabase = dbHelper.getWritableDatabase();
        Cursor favouriteRecords = sQlitedatabase.query(
                DatabaseHelper.TABLE_SONGS, new String[]{DatabaseHelper.KEY_id, DatabaseHelper.KEY_comment},
                DatabaseHelper.KEY_isFavourite + " = 1",
                null, null, null, null);


        Set<Integer> unique_ids = new HashSet<Integer>();
        HashMap<Integer, String> favComments = new HashMap<>();
        if (favouriteRecords.moveToFirst()) {
            int idInd = favouriteRecords.getColumnIndex(DatabaseHelper.KEY_id);
            int commentInd = favouriteRecords.getColumnIndex(DatabaseHelper.KEY_comment);
            do {
                int id = favouriteRecords.getInt(idInd);
                String comment = favouriteRecords.getString(commentInd);

                unique_ids.add(id);
                favComments.put(id, comment);
                Log.i("FUCK", comment);
            } while (favouriteRecords.moveToNext());
        }
        Log.i("FUCK", unique_ids.toString());


        sQlitedatabase.execSQL("delete from " + DatabaseHelper.TABLE_SONGS);
        ContentValues contentValues = new ContentValues();

        indexID = 1;
        getSetOfTracks(contentValues, spotify, unique_ids, sQlitedatabase, favComments, 50, 0);
    }

    private void getSetOfTracks(ContentValues contentValues, SpotifyService spotify,
                                Set<Integer> unique_ids, SQLiteDatabase sQlitedatabase,
                                HashMap<Integer, String> favComments, int limit, int offset) {
        spotify.getMySavedTracks(new HashMap<String, Object>() {
            {
                put("limit", limit);
                put("offset", offset);
            }
        }, new Callback<Pager<SavedTrack>>() {

            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {

                List<SavedTrack> songs = savedTrackPager.items;
                notAllTracks = songs.size() >= 50;

                Random random = new Random();

                for (SavedTrack song : songs) {
                    Track track = song.track;

                    contentValues.put(DatabaseHelper.KEY_name, track.name);
                    contentValues.put(DatabaseHelper.KEY_album, track.album.name);
                    contentValues.put(DatabaseHelper.KEY_duration, track.duration_ms);
                    contentValues.put(DatabaseHelper.KEY_trackNumber, track.track_number);
                    contentValues.put(DatabaseHelper.KEY_artist, track.artists.get(0).name);
                    contentValues.put(DatabaseHelper.KEY_price, random.nextInt(4) + 1);

                    if (unique_ids.contains(indexID)) {
                        contentValues.put(DatabaseHelper.KEY_isFavourite, 1);
                        contentValues.put(DatabaseHelper.KEY_comment, favComments.get(indexID));
                    } else {
                        contentValues.put(DatabaseHelper.KEY_isFavourite, 0);
                        contentValues.put(DatabaseHelper.KEY_comment, "No comments");
                    }
                    indexID++;

                    contentValues.put(DatabaseHelper.KEY_cover, track.album.images.get(0).url);
                    contentValues.put(DatabaseHelper.KEY_webviewid, track.id);

                    sQlitedatabase.insert(DatabaseHelper.TABLE_SONGS, null, contentValues);
                }
                if (songs.size() >= 50)
                    getSetOfTracks(contentValues, spotify, unique_ids, sQlitedatabase, favComments, limit, offset + limit);
            }

            @Override
            public void failure(RetrofitError error) {
                notAllTracks = false;
            }
        });
    }

    public static boolean isNetworkAvailable() {
        return network_available;
    }

    public static boolean network_available = true;
}
