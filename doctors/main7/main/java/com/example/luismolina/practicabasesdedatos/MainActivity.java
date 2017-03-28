package com.example.luismolina.practicabasesdedatos;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


//import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.attr.phoneNumber;

public class MainActivity extends AppCompatActivity {


    EditText etEmail, etPassword;
    Button btnEntrar, btnRegistroViaEmail,btnRegistroViaMovil;
    ProgressDialog dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        SharedPreferences prefe=getSharedPreferences("sesion", Context.MODE_PRIVATE);
        String InicioSesionMovil = prefe.getString("movil","");
        String InicioSesionMail = prefe.getString("mail","");

        //Toast.makeText(getApplicationContext(),InicioSesionMovil,Toast.LENGTH_LONG).show();

        if(InicioSesionMovil.equals(""))
        {
            if(InicioSesionMail.equals("")){}
            else
            {
                Intent intent = new Intent(getApplicationContext(), IngresarCodigo.class);
                startActivity(intent);
            }
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(), IngresarCodigo.class);
            startActivity(intent);
        }



        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);

        btnEntrar = (Button) findViewById(R.id.btnEntrar);
        btnRegistroViaEmail = (Button) findViewById(R.id.btnRegistroViaEmail);
        btnRegistroViaMovil = (Button) findViewById(R.id.btnRegistroViaMovil);


        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                boolean error = false;

                error = valida(email, password);

                if (!error) {

                    new login().execute("http://tienda3090.esy.es/login.php?password=" + password + "&email=" + email + "");
                }

            }
        });


        btnRegistroViaEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getApplicationContext(), RegistroViaEmail.class );
                startActivity(i);

            }
        });


        btnRegistroViaMovil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent  = new Intent(getBaseContext(), RegistroViaMovil.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

//                Intent i = new Intent(getApplicationContext(), RegistroViaMovil.class );
//                startActivity(i);

            }
        });





    }

    public boolean valida(String email, String password) {
        boolean error = false;

        Pattern pattern = Pattern
                .compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

        Matcher mather = pattern.matcher(email);

        if(mather.find()==false)
        {

            error=true;
            Modals nuevaModal = new Modals("Mensaje","El correo electrónico ingresado no es válido","OK",MainActivity.this);
            nuevaModal.createModal();
            etEmail.requestFocus();
        }
        else if(password.length()<5)
        {
            error=true;
            Modals nuevaModal = new Modals("Mensaje","La contraseña debe contener por lo menos 5 caracteres","OK",MainActivity.this);
            nuevaModal.createModal();
            etPassword.requestFocus();
        }

        return error;
    }


    public class login extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Espere por favor...");
            dialog.setCancelable(false);
            dialog.show();

        }

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            if (dialog.isShowing())
                dialog.dismiss();

            result = result.replace("[[", "[");
            result = result.replace("]]", "]");

            JSONArray data = null;
            try {
                data = new JSONArray(result);

                if (data.getString(0).equals("OK")) {
                    String email = etEmail.getText().toString().trim();
                    Intent i = new Intent(getApplicationContext(), Bienvenida.class);
                    i.putExtra("email", email);
                    startActivity(i);
                    finish();
                } else {
                    Modals nuevaModal = new Modals("Mensaje", data.getString(0), "OK", MainActivity.this);
                    nuevaModal.createModal();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }


    public String downloadUrl(String myurl) throws IOException {
        myurl = myurl.replace(" ", "%20");
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
