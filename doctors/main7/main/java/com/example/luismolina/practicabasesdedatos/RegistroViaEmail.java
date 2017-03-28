package com.example.luismolina.practicabasesdedatos;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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


public class RegistroViaEmail extends AppCompatActivity {

    EditText etMail,etNombre, etContrasena;
    Button btnRegistrarse;
    ProgressDialog dialog;

    Session session = null;
    ProgressDialog pdialog = null;
    Context context = null;
    EditText reciep, sub, msg;
    String rec, subject, textMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_via_email);

        etMail = (EditText)findViewById(R.id.etMail);
        etNombre = (EditText)findViewById(R.id.etNombre);
        etContrasena = (EditText)findViewById(R.id.etContrasena);




        btnRegistrarse = (Button) findViewById(R.id.btnRegistrarse);

        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                final String nombre = etNombre.getText().toString().trim();
                final String contrasena = etContrasena.getText().toString().trim();
                String mail = etMail.getText().toString().trim();

                boolean error=false;

                error = valida(nombre,mail,contrasena);

                if(!error)
                {

                    new validaMailDisponible().execute("http://tienda3090.esy.es/validaMailDisponible.php?email="+mail+"");

                }


            }
        });


    }


    public boolean valida(String nombre,String mail,String contrasena)
    {
        boolean error=false;


        Pattern pattern = Pattern
                .compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

        Matcher mather = pattern.matcher(mail);


        if(nombre.length()<=2)
        {
            error=true;
            Modals nuevaModal = new Modals("Mensaje","El nombre debe contener por lo menos 3 caracteres","OK",RegistroViaEmail.this);
            nuevaModal.createModal();
            etNombre.requestFocus();
        }
        else if(mather.find()==false)
        {

            error=true;
            Modals nuevaModal = new Modals("Mensaje","El correo electrónico ingresado no es válido","OK",RegistroViaEmail.this);
            nuevaModal.createModal();
            etMail.requestFocus();
        }
        else if(contrasena.length()<5)
        {
            error=true;
            Modals nuevaModal = new Modals("Mensaje","La contraseña debe contener por lo menos 5 caracteres","OK",RegistroViaEmail.this);
            nuevaModal.createModal();
            etContrasena.requestFocus();
        }
        return error;
    }


    public class validaMailDisponible extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            dialog = new ProgressDialog(RegistroViaEmail.this);
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

                final String mail = etMail.getText().toString().trim();

                if(data.getString(0).equals("No disponible pero falta activacion"))
                {
                    Modals nuevaModal = new Modals("Mensaje","Ya existe una cuenta con el correo electrónico "+mail+" pero no se encuentra activada, si en un plazo de 24 horas no se ha activado será eliminada y podrás usar el correo "+mail+" ","OK",RegistroViaEmail.this);
                    nuevaModal.createModal();

                }
                else
                {

                    // Toast.makeText(getApplicationContext(),data.getString(0), Toast.LENGTH_LONG).show();

                    Log.i("DDDDDDDDDDDDD",data.getString(0));

                    if(data.getString(0).equals("No disponible"))
                    {
                        Modals nuevaModal = new Modals("Mensaje","Ya existe una cuenta con el correo electrónico "+mail+" ","OK",RegistroViaEmail.this);
                        nuevaModal.createModal();
                    }
                    else
                    {

                        final String nombre = etNombre.getText().toString().trim();
                        final String contrasena = etContrasena.getText().toString().trim();

                        AlertDialog.Builder alertaCorreo = new AlertDialog.Builder(RegistroViaEmail.this);

                        alertaCorreo.setTitle("Mensaje");
                        alertaCorreo.setMessage("¿Su correo electrónico "+mail+" esta escrito correctamente?");
                        alertaCorreo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //new registro().execute("http://192.168.0.5/practica/PDO/pdo/registro.php?username="+username+"&nombre="+nombre+"&contrasena="+contrasena+"&mail="+mail+"");
                                new registro().execute("http://tienda3090.esy.es/registro.php?nombre="+nombre+"&password="+contrasena+"&email="+mail+"");
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

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

        }
    }


    public class registro extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            dialog = new ProgressDialog(RegistroViaEmail.this);
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

            result = result.replace("[[","[");
            result = result.replace("]]","]");

            JSONArray data = null;
            try
            {
                data = new JSONArray(result);

                if(data.length()>1)
                {

                    String  activar_via_email = data.getString(0);
                    String mail =data.getString(1);

                    Properties props = new Properties();
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.socketFactory.port", "465");
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.port", "465");


                    try {

                        session = Session.getDefaultInstance(props, new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication("luisame90@gmail.com", "merkurialbwin1");
                            }
                        });

                        RetreiveFeedTask task = new RetreiveFeedTask();
                        task.execute(mail,activar_via_email);


                    }
                    catch (Exception e)
                    {
                        Log.i("Error Message",e.getMessage());
                        //Toast.makeText(getApplicationContext(),"Fallo el envio de correo "+e.getMessage(),Toast.LENGTH_LONG).show();
                    }

                }
                else
                {
                    Toast.makeText(RegistroViaEmail.this,"Ocurrio un error a la hora de registrarte",Toast.LENGTH_SHORT).show();
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


    
    class RetreiveFeedTask extends AsyncTask<String, Void, String> {



        @Override
        protected String doInBackground(String... params) {

            try{

                //Log.i("Error Message","HOLA MUNDO");

                String mail = params[0];
                String activar_via_email = params[1];

                String asunto = "Activación de tu cuenta";
                String texto = "Por favor activa tu cuenta haciendo click en la siguiente dirección: <br><br> <a href=";


                String href = "'http://tienda3090.esy.es/formActivarCuenta.php?activar_via_email="+activar_via_email+"'>Activación de tu cuenta</a>";


                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(mail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mail));
                message.setSubject("Activación de cuenta");
                message.setContent(texto+href, "text/html; charset=utf-8");
                Transport.send(message);

            } catch(MessagingException e) {
                e.printStackTrace();
                Log.i("Error Message",e.getMessage());
            } catch(Exception e) {
                e.printStackTrace();
                Log.i("Error Message",e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //pdialog.dismiss();

            if (dialog.isShowing())
                dialog.dismiss();

            String mail = etMail.getText().toString().trim();

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(RegistroViaEmail.this);

            builder.setMessage("Te hemos enviado un correo a "+mail+" para poder activar tu cuenta entra a tu bandeja de correo y sigue las instrucciones")
                    .setTitle("Mensaje")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {


                            finish();
                        }
                    });

            builder.create();
            builder.show();

            //Toast.makeText(getApplicationContext(), "Mensaje enviado", Toast.LENGTH_LONG).show();
        }
    }





}
