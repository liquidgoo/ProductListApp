package by.bsuir.productlistapp;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.LruCache;

public class SongCoverImageLRUCache extends LruCache<String, Drawable> {

    public SongCoverImageLRUCache(int maxSize) {
        super(maxSize);
    }

    public Drawable getBitmapFromMemory(String key) {
        return this.get(key);
    }

    public void setBitmapToMemory(String key, Drawable drawable) {
        if (getBitmapFromMemory(key) == null) {
            this.put(key, drawable);
        }
    }
}
