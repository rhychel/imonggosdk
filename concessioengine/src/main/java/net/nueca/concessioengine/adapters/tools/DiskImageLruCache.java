package net.nueca.concessioengine.adapters.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;
import com.jakewharton.disklrucache.DiskLruCache;

import net.nueca.imonggosdk.tools.NetworkTools;
import net.nueca.imonggosdk.tools.ProductListTools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This will save on both DISK and RAM.
 *
 * Implementation of DiskLruCache by Jake Wharton
 * modified from http://stackoverflow.com/questions/10185898/using-disklrucache-in-android-4-0-does-not-provide-for-opencache-method
 */
public class DiskImageLruCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024;
    private DiskLruCache mDiskCache;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private static int IO_BUFFER_SIZE = 8 * 1024;
    private int mCompressQuality = 70;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    private Context context;

    private static DiskImageLruCache diskImageLruCache = null;

    public static DiskImageLruCache getInstance(Context context) {
        if(diskImageLruCache == null)
            diskImageLruCache = new DiskImageLruCache(context);
        return diskImageLruCache;
    }

    public DiskImageLruCache(int maxSize) {
        super(maxSize);
    }

    public DiskImageLruCache(Context context) {
        this(context, "concessioengine", DISK_CACHE_SIZE, Bitmap.CompressFormat.JPEG, 70);
    }

    public DiskImageLruCache(Context context, String uniqueName, int diskCacheSize,
                             Bitmap.CompressFormat compressFormat, int quality) {
        super(getCacheSize());
        try {
            final File diskCacheDir = getDiskCacheDir(context, uniqueName);
            this.context = context;
            mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize);
            mCompressFormat = compressFormat;
            mCompressQuality = quality;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor )
            throws IOException, FileNotFoundException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream( editor.newOutputStream(0), IO_BUFFER_SIZE );
            return bitmap.compress( mCompressFormat, mCompressQuality, out );
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath = context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * This is for the RAM cache
     * @return
     */
    public static int getCacheSize() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        return cacheSize;
    }

    @Override
    public void putBitmap( String key, Bitmap data ) {
        put(key, data);
        DiskLruCache.Editor editor = null;
        try {
            String imageKey = ProductListTools.md5(key);
            editor = mDiskCache.edit( imageKey );
            if ( editor == null ) {
                return;
            }

            if( writeBitmapToFile( data, editor ) ) {
                mDiskCache.flush();
                editor.commit();
                Log.d("cache_test_DISK_", "image put on disk cache " + key);
            } else {
                editor.abort();
                Log.d("cache_test_DISK_", "ERROR on: image put on disk cache " + key);
            }
        } catch (IOException e) {
            Log.d( "cache_test_DISK_", "ERROR on: image put on disk cache " + key );
            try {
                if ( editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public Bitmap getBitmap( String key ) {
        if (context == null)
            return null;
        if (NetworkTools.isInternetAvailable(context))
            return get(key);
        else {
            Bitmap bitmap = null;
            DiskLruCache.Snapshot snapshot = null;
            try {
                // Retrieve the URL
                String imageKey = ProductListTools.md5(key);

                snapshot = mDiskCache.get(imageKey);
                if (snapshot == null) {
                    return null;
                }
                final InputStream in = snapshot.getInputStream(0);
                if (in != null) {
                    final BufferedInputStream buffIn =
                            new BufferedInputStream(in, IO_BUFFER_SIZE);
                    bitmap = BitmapFactory.decodeStream(buffIn);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (snapshot != null) {
                    snapshot.close();
                }
            }
            Log.d("cache_test_DISK_", bitmap == null ? "" : "image read from disk " + key);
            return bitmap;
        }
    }

    public boolean containsKey( String key ) {
        boolean contained = false;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get( key );
            contained = snapshot != null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( snapshot != null ) {
                snapshot.close();
            }
        }

        return contained;

    }

    public void clearDiskCache() {
        Log.d( "cache_test_DISK_", "disk cache CLEARED");
        try {
            mDiskCache.delete();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    public void clearMemoryCache() {
        Log.d( "cache_test_MEMORY_", "memory cache CLEARED");
        evictAll();
    }

    public File getCacheFolder() {
        return mDiskCache.getDirectory();
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return (value.getRowBytes() * value.getHeight())/1024;
    }
}