package com.example.robospock.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.robospock.R;
import com.google.inject.Inject;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class MainActivity extends RoboActivity implements DialogInterface.OnClickListener {
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @InjectView(R.id.hello)
    TextView helloTv;
    @InjectView(R.id.button)
    Button button;
    @InjectView(R.id.button_text)
    TextView buttonTv;
    @Inject
    ActivityManager activityManager;
    @InjectView(R.id.memory_text)
    TextView memoryTv;
    DialogInterface.OnClickListener dialogListener = this;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helloTv.setText("Test App - Basic");
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                buttonTv.setText("Clicked !");
                showDialog(1);
            }
        });

        memoryTv.setText("I have " + activityManager.getMemoryClass() / 4 + " MB");
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        return new AlertDialog.Builder(this).setTitle("title").setPositiveButton("Ok", dialogListener)
                .setNegativeButton("Cancel", dialogListener).setNeutralButton("Dismiss", dialogListener)
                .setMessage("Dialog Content").create();

    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        switch (which) {
            default:
                break;
        }
    }
}
