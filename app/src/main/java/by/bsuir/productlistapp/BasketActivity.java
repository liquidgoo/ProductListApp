package by.bsuir.productlistapp;

import static by.bsuir.productlistapp.MainActivity.dbHelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class BasketActivity extends AppCompatActivity {

    public static ArrayList<Song> basketList = new ArrayList<>();
    private ListView basketListView;
    private TextView totalPriceTextView;
    private TextView totalCurrencyTextView;
    private Button buyButton;
    private double totalSum;

    private void getRidOfTopBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
    }

    private double getButifiedCoeffPrice(double price){
        return Math.round(price * 100 * MainActivity.GLOBAL_CURRENCY_COEFF) / 100.0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getRidOfTopBar();
        setContentView(R.layout.activity_basket);

        basketListView = findViewById(R.id.basketListView);
        basketList.clear();

        dbHelper = new DatabaseHelper(MainActivity.mainActivityContext);
        SQLiteDatabase sQlitedatabase = dbHelper.getWritableDatabase();

        double totalSum = 0.0;

        Cursor cursor = sQlitedatabase.query(
                DatabaseHelper.TABLE_BASKET,
                null, null,
                null, null,
                null, null);

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

            do{
                basketList.add(new Song(
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
                totalSum += cursor.getDouble(priceInd);
            }while (cursor.moveToNext());
        }
        dbHelper.close();


        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        totalPriceTextView.setText(String.valueOf(getButifiedCoeffPrice(totalSum)));
        totalCurrencyTextView = findViewById(R.id.totalCurrencyTextView);
        if (MainActivity.IS_USD) {
            totalCurrencyTextView.setText("$");
        } else{
            totalCurrencyTextView.setText("BYN");
        }


        SongAdapter adapter = new SongAdapter(MainActivity.mainActivityContext, 0, basketList);
        basketListView.setAdapter(adapter);


        buyButton = findViewById(R.id.buyButton);
        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper = new DatabaseHelper(MainActivity.mainActivityContext);
                SQLiteDatabase sQlitedatabase = dbHelper.getWritableDatabase();
                sQlitedatabase.execSQL("delete from " + DatabaseHelper.TABLE_BASKET);
                dbHelper.close();
                basketList.clear();
                basketListView.setAdapter(new SongAdapter(MainActivity.mainActivityContext, 0, basketList));
                totalPriceTextView.setText("0.0");
            }
        });
    }
}