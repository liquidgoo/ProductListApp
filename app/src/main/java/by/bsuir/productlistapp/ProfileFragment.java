package by.bsuir.productlistapp;

import static by.bsuir.productlistapp.MainActivity.GLOBAL_CURRENCY_BYN;
import static by.bsuir.productlistapp.MainActivity.GLOBAL_CURRENCY_USD;
import static by.bsuir.productlistapp.MainActivity.IS_USD;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.UserPrivate;
import kaaes.spotify.webapi.android.models.UserPublic;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProfileFragment extends Fragment {

    private Switch switchTheme;
    private Switch switchCurrency;
    public static SupportMapFragment supportMapFragment;
    public static FusedLocationProviderClient client;

    private TextView userNameTextView;
    private TextView userEmailTextView;
    private ImageView userProfileImageView;
    private TextView currencyTextView;

    private Button basketButton;
    private Button favouriteButton;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    private void findCurrencyTextView(View view) {
        currencyTextView = view.findViewById(R.id.currencyTextView);
    }
    private void findSwitchTheme(View view){
        switchTheme = view.findViewById(R.id.switchTheme);
    }
    private void findMapFragment() { supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map); }

    private void findSwitchCurrency(View view) {  switchCurrency = view.findViewById(R.id.switchCurrency); }

    public static void getCurrentLocation() {
        @SuppressLint("MissingPermission")
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions options = new MarkerOptions().position(latLng).title("I'm right here!");
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

                            googleMap.addMarker(options).showInfoWindow();
                        }
                    });
                }
            }
        });
    }

    private void handleFABButton(View view){
        View fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view,
                        "Developer: Yakshuk V. D. Group: 951005\nAssignment #2 overseen by: Petrovskaya V. V.",
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
            }
        });
    }

    private void handleDayNightTheme() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivityContext);
        boolean dayTheme = sharedPrefs.getBoolean("IS_DAY_THEME", true);

        if (dayTheme){
            switchTheme.setChecked(false);
        } else{
            switchTheme.setChecked(true);
        }
    }

    private void handleCurrencySwitch() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivityContext);
        boolean isUsd = sharedPrefs.getBoolean("IS_USD", true);

        if (isUsd){
            switchCurrency.setChecked(false);
        } else{
            switchCurrency.setChecked(true);
        }
    }

    private void handleCurrencyTextView(){
        currencyTextView.setText(String.valueOf(MainActivity.USD_IN_BYN));
    }

    private void handleProfileInfoTextViews(){
        userNameTextView.setText(MainActivity.userProfileName);
        userEmailTextView.setText(MainActivity.userEmail);
        userProfileImageView.setImageBitmap(MainActivity.userProfileBitmap);
    }

    private void findSpotifyUserViews(View v){
        userNameTextView = v.findViewById(R.id.userNameTextView);
        userEmailTextView = v.findViewById(R.id.emailTextView);
        userProfileImageView = v.findViewById(R.id.profileImageView);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        findSwitchTheme(view);
        findCurrencyTextView(view);
        switchTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivityContext);
                if (isChecked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("IS_DAY_THEME", false);
                    editor.apply();
                } else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("IS_DAY_THEME", true);
                    editor.apply();
                }
            }
        });

        findSwitchCurrency(view);
        switchCurrency.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainActivityContext);
                if (isChecked){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("IS_USD", false);
                    MainActivity.IS_USD = false;

                    MainActivity.GLOBAL_CURRENCY_COEFF = GLOBAL_CURRENCY_BYN;

                    editor.apply();
                } else{
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("IS_USD", true);
                    MainActivity.IS_USD = true;

                    MainActivity.GLOBAL_CURRENCY_COEFF = GLOBAL_CURRENCY_USD;

                    editor.apply();
                }
            }
        });

        findMapFragment();
        findSpotifyUserViews(view);

        if (MainActivity.isNetworkAvailable()) {
            client = LocationServices.getFusedLocationProviderClient(MainActivity.mainActivityContext);
            if (ActivityCompat.checkSelfPermission(MainActivity.mainActivityContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                ActivityCompat.requestPermissions((Activity) MainActivity.mainActivityContext,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
        }

        handleFABButton(view);
        handleDayNightTheme();
        handleCurrencySwitch();
        handleCurrencyTextView();
        handleProfileInfoTextViews();


        basketButton = view.findViewById(R.id.getBasketActivity);
        basketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toBasketActivity = new Intent(MainActivity.mainActivityContext, BasketActivity.class);
                startActivity(toBasketActivity);
            }
        });


        favouriteButton = view.findViewById(R.id.getFavouritesActivity);
        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toFavouriteActivity = new Intent(MainActivity.mainActivityContext, FavouriteActivity.class);
                startActivity(toFavouriteActivity);
            }
        });

        return view;
    }
}