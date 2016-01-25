package net.nueca.imonggosdk.interfaces;

/**
 * Created by Jn on 20/01/16.
 */
public interface SynServiceAsyncTaskListener {

    String doInBackground(String... params);

    void onPostExecute(String result);

}
