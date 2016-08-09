package com.example.rishad.train;

import android.os.AsyncTask;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


class Api extends AsyncTask<String, Void, JSONObject>
//class Api extends AsyncTask<Void, Void, String> {
{
    Exception mException = null;

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        this.mException = null;
    }

    @Override
    protected JSONObject doInBackground(String... params)
    {
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://192.168.50.5:8082/whereami?");
        urlString.append("latitude=").append(params[0]);
        urlString.append("&longitude=").append(params[1]);

        HttpURLConnection urlConnection = null;
        URL url = null;
        JSONObject object = null;

        try
        {
            url = new URL(urlString.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.connect();
            InputStream inStream = null;
            inStream = urlConnection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp, response = "";
            while ((temp = bReader.readLine()) != null)
                response += temp;
            bReader.close();
            inStream.close();
            urlConnection.disconnect();
            object = (JSONObject) new JSONTokener(response).nextValue();
        }
        catch (Exception e)
        {
            this.mException = e;
        }

        return (object);
    }

    @Override
    protected void onPostExecute(JSONObject result)
    {
        super.onPostExecute(result);

        //if (this.mException != null)
        //    ErrorHelper.report(this.mException, "Error # NearbySearchRequest");
    }
}

