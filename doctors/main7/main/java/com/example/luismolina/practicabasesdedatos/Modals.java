package com.example.luismolina.practicabasesdedatos;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by luis.molina on 22/11/2016.
 */

public class Modals
{
    public String title;
    public String msgBody;
    public String msgPositiveButton;
    public Activity someActivity;


    public Modals(String title, String msgBody, String msgPositiveButton, Activity someActivity) {
        this.title = title;
        this.msgBody = msgBody;
        this.msgPositiveButton = msgPositiveButton;
        this.someActivity = someActivity;
    }


    public void createModal()
    {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(someActivity);

        builder.setMessage(msgBody)
                .setTitle(title)
                .setPositiveButton(msgPositiveButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {

                    }
                });

        builder.create();
        builder.show();


    }

}
