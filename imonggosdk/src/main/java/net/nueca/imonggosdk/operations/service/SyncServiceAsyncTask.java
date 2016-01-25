package net.nueca.imonggosdk.operations.service;

import android.os.AsyncTask;
import android.util.Log;

import net.nueca.imonggosdk.interfaces.SynServiceAsyncTaskListener;


/**
 * Created by Jn on 20/01/16.
 */
public class SyncServiceAsyncTask extends AsyncTask<String, Void, String> {

    private SynServiceAsyncTaskListener synServiceAsyncTaskListener;

    public SyncServiceAsyncTask() {
    }

    public SyncServiceAsyncTask(SynServiceAsyncTaskListener synServiceAsyncTaskListener) {
        this.synServiceAsyncTaskListener = synServiceAsyncTaskListener;
    }

    public SynServiceAsyncTaskListener getSyncServiceAsyncTaskListener() {
        return synServiceAsyncTaskListener;
    }

    public void setSyncServiceAsyncTaskListener(SynServiceAsyncTaskListener synServiceAsyncTaskListener) {
        this.synServiceAsyncTaskListener = synServiceAsyncTaskListener;
    }

    @Override
    protected String doInBackground(String... params) {
        if(synServiceAsyncTaskListener != null ) {
            synServiceAsyncTaskListener.doInBackground(params);
        }
        return "";
    }


    @Override
    protected void onPostExecute(String result) {
        if(synServiceAsyncTaskListener != null) {
            synServiceAsyncTaskListener.onPostExecute(result);
        }
    }
}
