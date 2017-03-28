package com.example.luismolina.practicabasesdedatos;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Bienvenida extends AppCompatActivity {

    TextView tvUsername, tvNombre,tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bienvenida);

        Bundle bundle = getIntent().getExtras();
        String email=bundle.getString("email");


        new loadDatos().execute("http://tienda3090.esy.es/loadDatosUsuario.php?email="+email+"");


    }


    public class loadDatos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                //Toast.makeText(getApplicationContext(),"AAAA",Toast.LENGTH_SHORT).show();
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result)
        {
            result = result.replace("[[","[");
            result = result.replace("]]","]");

            JSONArray ja = null;
            try
            {
                ja = new JSONArray(result);

                if(ja.length()>1)
                {
                    tvUsername = (TextView)findViewById(R.id.tvUsername);
                    tvNombre = (TextView)findViewById(R.id.tvNombre);
                    tvEmail = (TextView)findViewById(R.id.tvEmail);

                    tvNombre.setText(ja.getString(0));
                    tvUsername.setText(ja.getString(1));
                    tvEmail.setText(ja.getString(2));
                }
                else
                {
                    Toast.makeText(getApplicationContext(),ja.getString(0),Toast.LENGTH_SHORT).show();
                }


            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            //Toast.makeText(getApplicationContext(),"Se ingreso correctamente",Toast.LENGTH_SHORT).show();
        }
    }


    public String downloadUrl(String myurl) throws IOException {
        myurl = myurl.replace(" ","%20");
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("respuesta", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }



}
