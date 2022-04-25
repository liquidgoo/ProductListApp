package by.bsuir.productlistapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ListView songsListView;

    public static Button peripheryButton;
    public static Button allButton;

    private String mParam1;
    private String mParam2;

    public HomeFragment() {
    }
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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


    public static void quickFixFilterButtonsText() {
        peripheryButton.setText("Periphery (" + MainActivity.PERIPHERY_SONGS_COUNT + ")");
        allButton.setText("All (" + MainActivity.ALL_SONGS_COUNT + ")");
    }

    public static void handleFilterButtons() {
        if (MainActivity.peripheryIsSelectedFilter) {
            peripheryButton.setBackgroundColor(Color.GREEN);
            allButton.setBackgroundColor(Color.WHITE);
        } else {
            peripheryButton.setBackgroundColor(Color.WHITE);
            allButton.setBackgroundColor(Color.GREEN);
        }
        peripheryButton.setTextColor(Color.BLACK);
        allButton.setTextColor(Color.BLACK);

        peripheryButton.setText("Periphery (" + MainActivity.PERIPHERY_SONGS_COUNT + ")");
        allButton.setText("All (" + MainActivity.ALL_SONGS_COUNT + ")");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        songsListView = view.findViewById(R.id.songsListView);
        filterList(MainActivity.peripheryIsSelectedFilter);
        songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Song selectedSong = (Song) songsListView.getItemAtPosition(position);
                Intent showDetail = new Intent(MainActivity.mainActivityContext, DetailActivity.class);
                showDetail.putExtra("id", selectedSong.getId() - 1);
                startActivity(showDetail);
            }
        });


        peripheryButton = view.findViewById(R.id.PeripheryFilter);
        peripheryButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                filterList(true);
                peripheryButton.setBackgroundColor(Color.GREEN);
                allButton.setBackgroundColor(Color.WHITE);
            }
        });

        allButton = view.findViewById(R.id.AllFilter);
        allButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                filterList(false);
                peripheryButton.setBackgroundColor(Color.WHITE);
                allButton.setBackgroundColor(Color.GREEN);
            }
        });

        handleFilterButtons();

        if (!MainActivity.isNetworkAvailable()) {
            TextView internetConnectionLost = (TextView) view.findViewById(R.id.warningTextView);
            internetConnectionLost.setVisibility(View.VISIBLE);
        }

        return view;
    }


    private void filterList(boolean status) {
        MainActivity.peripheryIsSelectedFilter = status;
        ArrayList<Song> filteredSongs = new ArrayList<>();

        for (Song song : MainActivity.songsList) {
            final String searchString = "periphery";

            if (MainActivity.peripheryIsSelectedFilter) {
                if (song.getArtist().toLowerCase(Locale.ROOT).contains(searchString)) {
                    filteredSongs.add(song);
                }
            } else {
                filteredSongs.add(song);
            }
        }

        SongAdapter adapter = new SongAdapter(MainActivity.mainActivityContext, 0, filteredSongs);
        songsListView.setAdapter(adapter);
    }
}