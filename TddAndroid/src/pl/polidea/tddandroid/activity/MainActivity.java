package pl.polidea.tddandroid.activity;

import pl.polidea.tddandroid.R;
import pl.polidea.tddandroid.tasks.BitmapAsycTask;
import pl.polidea.tddandroid.tasks.StringAsycTask;
import pl.polidea.tddandroid.tasks.TaskExecutorInterface;
import pl.polidea.tddandroid.tasks.WebAsyncTask;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;

public class MainActivity extends RoboActivity implements android.content.DialogInterface.OnClickListener {

    static final String URL = "http://dev.polidea.pl/ext/szlif677557/text";
    static final String IMAGE_URL = "http://www.polidea.pl/CorporateIdentity/logo_100x60.png";

    @InjectView(R.id.hello) TextView helloTv;
    @InjectView(R.id.button) Button button;
    @InjectView(R.id.button_text) TextView buttonTv;
    @Inject ActivityManager activityManager;
    @InjectView(R.id.memory_text) TextView memoryTv;
    @InjectView(R.id.async_text) TextView asyncText;
    @InjectView(R.id.web_text) TextView webTv;
    @InjectView(R.id.web_image) ImageView webIv;
    @InjectView(R.id.load_button) Button loadBtn;

    @Inject TaskExecutorInterface taskExecutorInterface;

    DialogInterface.OnClickListener dialogListener = this;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helloTv.setText("Hello Szlif!");
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                buttonTv.setText("Clicked !");
                showDialog(1);
            }
        });

        memoryTv.setText("I have " + activityManager.getMemoryClass() / 4 + " MB");

        taskExecutorInterface.execute(new StringAsycTask(this, this));
        taskExecutorInterface.execute(new WebAsyncTask(this, URL));

        loadBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(final View v) {
                taskExecutorInterface.execute(new BitmapAsycTask(MainActivity.this, IMAGE_URL));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void setAsyncTest(final String text) {
        asyncText.setText(text);
    }

    public void setWebText(final CharSequence text) {
        webTv.setText(text);
    }

    public void setImageBitmap(final Bitmap bitmap) {
        webIv.setImageBitmap(bitmap);
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
