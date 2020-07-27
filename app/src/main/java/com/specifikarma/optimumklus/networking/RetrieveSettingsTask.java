package com.specifikarma.optimumklus.networking;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class RetrieveSettingsTask extends AsyncTask<URL, Void, String> {

    private StringBuffer sb;

    @Override
    protected String doInBackground(final URL... urls) {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(urls[0].toString()).openStream(), "UTF-8"));
            sb = new StringBuffer();
            String str;
            while((str = reader.readLine())!= null){
                sb.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}