package com.click.clicklib;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ServerCall extends AsyncTask<Void, Void, String> {
    private String url1;
    private ContentValues values;

    public ServerCall(String url, ContentValues values) {

        this.url1 = url;
        this.values = values;
    }

    @Override
    protected String doInBackground(Void... params) {

        URL url = null;
        HttpURLConnection urlConnection = null;
        int str = 0;
        try {
            url = new URL(url1);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            Log.i("text11",""+in.toString());
            str = in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        return String.valueOf(str);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);


    }
}

