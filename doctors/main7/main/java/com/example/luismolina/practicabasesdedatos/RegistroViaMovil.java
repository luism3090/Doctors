package com.example.luismolina.practicabasesdedatos;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class RegistroViaMovil extends AppCompatActivity {

    EditText etNombre, etMovil;
    Button btnRegistrarse;
    ProgressDialog dialog;
    LinearLayout layRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_via_movil);

        etNombre = (EditText)findViewById(R.id.etNombre);
        etMovil = (EditText)findViewById(R.id.etMovil);
        btnRegistrarse = (Button)findViewById(R.id.btnRegistrarse);


        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nombre = etNombre.getText().toString().trim();
                String movil = etMovil.getText().toString().trim();

                boolean error = false;

                error = valida(nombre,movil);


                if (!error)
                {

                    AlertDialog.Builder alertaCorreo = new AlertDialog.Builder(RegistroViaMovil.this);

                    alertaCorreo.setTitle("Mensaje");
                    alertaCorreo.setMessage("¿Su número movil "+movil+" esta escrito correctamente?");
                    alertaCorreo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            String nombre = etNombre.getText().toString().trim();
                            String movil = etMovil.getText().toString().trim();

                            // enviar SMS en android

                            int n1 = (int) (Math.random()*9+1);
                            int n2 = (int) (Math.random()*9+1);
                            int n3 = (int) (Math.random()*9+1);
                            int n4 = (int) (Math.random()*9+1);


                            String no1,no2,no3,no4;

                            no1 = String.valueOf(n1);
                            no2 = String.valueOf(n2);
                            no3 = String.valueOf(n3);
                            no4 = String.valueOf(n4);

                            String codigo = no1+no2+no3+no4;

//                            Toast.makeText(getApplicationContext(),"Hola mundo 1",Toast.LENGTH_LONG).show();

                            new registro().execute("http://tienda3090.esy.es/registro.php?nombre=" + nombre + "&movil=" + movil + "&codigo=" + codigo + " ");

                        }
                    });
                    alertaCorreo.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertaCorreo.create();
                    alertaCorreo.show();

                }


            }
        });


    }


    public boolean valida(String nombre, String movil) {
        boolean error = false;

        if(nombre.length()<3)
        {
            error=true;
            Modals nuevaModal = new Modals("Mensaje","El nombre debe contener por lo menos 3 caracteres","OK",RegistroViaMovil.this);
            nuevaModal.createModal();
            etNombre.requestFocus();
        }
        else if(movil.length()<10)
        {
            error=true;
            Modals nuevaModal = new Modals("Mensaje","El número movil debe contener 10 dígitos","OK",RegistroViaMovil.this);
            nuevaModal.createModal();
            etNombre.requestFocus();
        }

        return error;
    }


    public class registro extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            dialog = new ProgressDialog(RegistroViaMovil.this);
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
        protected void onPostExecute(String result)
        {

            if (dialog.isShowing())
                dialog.dismiss();

            result = result.replace("[[","[");
            result = result.replace("]]","]");

            JSONArray data = null;
            try
            {
                data = new JSONArray(result);

               // Toast.makeText(getApplicationContext(),String.valueOf(data),Toast.LENGTH_LONG).show();
                //String phoneNumber = "+522731072050";
                String message = "Tu cógido de activacion es "+data.getString(1);


                SmsManager smsManager;
                smsManager = SmsManager.getDefault();

                try
                {
                    //smsManager.sendTextMessage(phoneNumber,null,message,null,null);
                    smsManager.sendTextMessage(data.getString(0),null,message,null,null);

                    SharedPreferences preferencias=getSharedPreferences("sesion", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor=preferencias.edit();
                    editor.putString("movil",data.getString(1));
                    editor.commit();

                    Intent intent = new Intent(getApplicationContext(), IngresarCodigo.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(),"Ha ocurrido un error al enviar el sms "+e.getMessage(),Toast.LENGTH_LONG).show();
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
