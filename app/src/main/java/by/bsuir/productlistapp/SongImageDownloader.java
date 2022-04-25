package by.bsuir.productlistapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SongImageDownloader<T> extends HandlerThread {

    private static final String TAG = "SongImageDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private boolean hasQuit = false;

    private Handler requestHandler;
    private ConcurrentMap<T, String> requestMap = new ConcurrentHashMap<>();

    private Handler responseHandler;
    private SongImageDownloadListener<T> songImageDownloadListener;

    public interface SongImageDownloadListener<T>{
        void onSongImageDownloaded(T target, Bitmap songImage);
    }

    public void setSongImageDownloadListener(SongImageDownloadListener listener){
        songImageDownloadListener = listener;
    }

    public SongImageDownloader(Handler responseHandler) {
        super(TAG);
        this.responseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        requestHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD){
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " +
                            requestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    @Override
    public boolean quit() {
        hasQuit = true;
        return super.quit();
    }

    public void queueSongImage(T target, String url){
        Log.i(TAG, "Got a URL: " + url);

        if (url == null){
            requestMap.remove(target);
        } else{
            requestMap.put(target, url);
            requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }

    private void handleRequest(final T target){
        try{
            final String strUrl = requestMap.get(target);
            if (strUrl == null){
                return ;
            }

            URL url = new URL(strUrl);
            final Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            Log.i(TAG, "Bitmap created!");

            responseHandler.post(new Runnable(){

                @Override
                public void run() {
                    if (requestMap.get(target) != strUrl || hasQuit){
                        return;
                    }

                    requestMap.remove(target);
                    songImageDownloadListener.onSongImageDownloaded(target, bitmap);
                }
            });

        } catch (IOException ioe){
            Log.e(TAG, "Error downloading image");
        }
    }

    public void clearQueue(){
        requestHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
}
