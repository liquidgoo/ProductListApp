package by.bsuir.productlistapp;

import static by.bsuir.productlistapp.MainActivity.dbHelper;
import static by.bsuir.productlistapp.MainActivity.lru;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SongAdapter extends ArrayAdapter<Song> {

    TextView songName;
    TextView authorName;
    Button isFavourite;
    TextView songPrice;
    TextView currency;
    Button addToCart;


    public SongAdapter(@NonNull Context context, int resource, @NonNull List<Song> songsList) {
        super(context, resource, songsList);
    }


    private double getButifiedCoeffPrice(double price){
        return Math.round(price * 100 * MainActivity.GLOBAL_CURRENCY_COEFF) / 100.0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Song song = getItem(position);

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.track_item, parent, false);
        }



        songName = (TextView) convertView.findViewById(R.id.itemSongName);
        authorName = (TextView) convertView.findViewById(R.id.itemAuthorName);
        isFavourite = (Button) convertView.findViewById(R.id.isFavourite);
        songPrice = (TextView) convertView.findViewById(R.id.itemPrice);
        currency = (TextView) convertView.findViewById(R.id.itemCurrency);
        addToCart = (Button) convertView.findViewById(R.id.itemAddToCart);
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper = new DatabaseHelper(MainActivity.mainActivityContext);
                SQLiteDatabase sQlitedatabase = dbHelper.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(DatabaseHelper.KEY_name, song.getName());
                contentValues.put(DatabaseHelper.KEY_album, song.getAlbum());
                contentValues.put(DatabaseHelper.KEY_duration, song.getDuration());
                contentValues.put(DatabaseHelper.KEY_trackNumber, song.getTrackNumber());
                contentValues.put(DatabaseHelper.KEY_artist, song.getArtist());
                contentValues.put(DatabaseHelper.KEY_price, song.getPrice());
                contentValues.put(DatabaseHelper.KEY_isFavourite, song.isFavourite());
                contentValues.put(DatabaseHelper.KEY_comment, song.getComment());
                sQlitedatabase.insert(DatabaseHelper.TABLE_BASKET, null, contentValues);
                dbHelper.close();
            }
        });


        songName.setText(song.getName());
        authorName.setText(song.getArtist());

        if (song.isFavourite()){
            isFavourite.setBackground(
                    MainActivity.mainActivityContext.
                            getDrawable(R.drawable.ic_favourite_chosen));
        } else {
            isFavourite.setBackground(
                    MainActivity.mainActivityContext.
                            getDrawable(R.drawable.ic_favourite_not_chosen));
        }

        songPrice.setText(String.valueOf(getButifiedCoeffPrice(song.getPrice())));
        if (MainActivity.IS_USD){
            currency.setText("$");
        } else {
            currency.setText("BYN");
        }

        Button btnFavourite = (Button) convertView.findViewById(R.id.isFavourite);
        btnFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (song.isFavourite()) {
                    btnFavourite.setBackgroundResource(R.drawable.ic_favourite_not_chosen);
                    song.setFavourite(false);
                    FavouriteActivity.removeFavouriteSong(song);
                }
                else {
                    btnFavourite.setBackgroundResource(R.drawable.ic_favourite_chosen);
                    song.setFavourite(true);
                    FavouriteActivity.addFavouriteSong(song);
                }
            }
        });

        handleBehaviourSpecifics(((View)parent).getId());

        return convertView;
    }

    private void handleBehaviourSpecifics(int id){
        switch (id){
            case R.id.basketListView:{
                isFavourite.setVisibility(View.GONE);
                addToCart.setVisibility(View.GONE);
                break;
            }
        }
    }
}
