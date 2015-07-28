package net.nueca.concessioengine.adapters.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.LruCache;

import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageLoader;

/**
 *
 * LruCache to save only on RAM.
 *
 * Created by rhymart on 7/13/15.
 * imonggosdk (c)2015
 */
public class ImageLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

    public ImageLruCache(int maxSize) {
        super(maxSize);
    }

    public ImageLruCache(Context context) {
        super(getCacheSize(context));
    }

    // Returns a cache size equal to approximately three screens worth of images.
    public static int getCacheSize(Context context) {
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final int screenWidth = displayMetrics.widthPixels;
        final int screenHeight = displayMetrics.heightPixels;
        // 4 bytes per pixel
        final int screenBytes = screenWidth * screenHeight * 4;

        return screenBytes * 3;
    }

    public static int getCacheSize() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        return cacheSize;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return (value.getRowBytes() * value.getHeight())/1024;
    }
}