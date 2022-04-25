package by.bsuir.productlistapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DetailActivity extends AppCompatActivity {

    Song selectedSong;
    private final String URL_PART = "https://open.spotify.com/track/";

    private void getRidOfTopBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getRidOfTopBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSelectedShape();
        setValues();
    }

    private void setSongImage(ImageView songImage, String strUrl){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL(strUrl);
                final Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                songImage.setImageBitmap(bitmap);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                MainActivity.network_available = false;
            }
        });

        try {
            executor.shutdown();
            executor.awaitTermination(7, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getFormattedTime(long duration){
        String toReturn = "";

        duration /= 1000;

        long hours = duration / 60 / 60;
        long minutes = duration / 60;
        long seconds = duration - minutes * 60;

        if (hours > 0){
            toReturn += String.valueOf(hours) + " h ";
        }

        if (minutes > 0){
            toReturn += String.valueOf(minutes) + " m ";
        }

        if (seconds > 0){
            toReturn += String.valueOf(seconds) + " s";
        }

        return toReturn;
    }

    private double getButifiedCoeffPrice(double price){
        return Math.round(price * 100 * MainActivity.GLOBAL_CURRENCY_COEFF) / 100.0;
    }

    private void setValues() {
        ImageView songImage = (ImageView) findViewById(R.id.detailCoverImage);
        TextView songName = (TextView) findViewById(R.id.detailTrackName);
        TextView albumName = (TextView) findViewById(R.id.detailAlbumName);

        TextView duration = (TextView) findViewById(R.id.detailDuration);
        TextView trackNumber = (TextView) findViewById(R.id.detailTrackNumber);
        TextView artists = (TextView) findViewById(R.id.detailArtistName);

        TextView songPrice = (TextView) findViewById(R.id.detailPrice);
        TextView currency = (TextView) findViewById(R.id.detailCurrency);

        songImage.setImageDrawable(MainActivity.mainActivityContext.getDrawable(R.drawable.ic_default_image));
        if (MainActivity.isNetworkAvailable()) setSongImage(songImage, selectedSong.getCoverUrl());
        songName.setText(selectedSong.getName());
        albumName.setText(selectedSong.getAlbum());


        duration.setText(getFormattedTime(selectedSong.getDuration()));
        trackNumber.setText("#" + String.valueOf(selectedSong.getTrackNumber()));

        artists.setText(selectedSong.getArtist());


        songPrice.setText(String.valueOf(getButifiedCoeffPrice(selectedSong.getPrice())));
        if (MainActivity.IS_USD){
            currency.setText("$");
        } else {
            currency.setText("BYN");
        }
    }

    private void getSelectedShape() {
        Intent previousIntent = getIntent();
        int id = previousIntent.getIntExtra("id", 0);
        boolean isFavouriteActivity = previousIntent.getBooleanExtra("favourite", false);
        selectedSong = MainActivity.songsList.get(id);

        if (isFavouriteActivity){
            handleAdditionalViews();
        }
    }

    private void handleAdditionalViews(){
        TextView commentLabel = findViewById(R.id.commentLabel);
        TextView userCommentLabel = findViewById(R.id.userCommentLabel);
        EditText addNewComment = findViewById(R.id.addNewComment);
        Button sumbitButton = findViewById(R.id.submitButton);
        WebView webView = findViewById(R.id.browser);

        userCommentLabel.setText(selectedSong.getComment());
        sumbitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedSong.setComment(addNewComment.getText().toString());
                userCommentLabel.setText(selectedSong.getComment());
            }
        });

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(URL_PART + selectedSong.getWebPagePartID());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        commentLabel.setVisibility(View.VISIBLE);
        userCommentLabel.setVisibility(View.VISIBLE);
        addNewComment.setVisibility(View.VISIBLE);
        sumbitButton.setVisibility(View.VISIBLE);
        webView.setVisibility(View.VISIBLE);
    }
}
