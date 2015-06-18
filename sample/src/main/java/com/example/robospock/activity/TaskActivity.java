package com.example.robospock.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.robospock.R;
import com.example.robospock.tasks.BitmapAsycTask;
import com.example.robospock.tasks.StringAsycTask;
import com.example.robospock.tasks.TaskExecutorInterface;
import com.example.robospock.tasks.WebAsyncTask;
import com.google.inject.Inject;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class TaskActivity extends RoboActivity {
    static final String URL = "http://dev.polidea.pl/ext/szlif677557/text";
    static final String IMAGE_URL = "http://www.polidea.pl/CorporateIdentity/logo_100x60.png";
    @Inject
    TaskExecutorInterface taskExecutorInterface;
    @InjectView(R.id.load_button)
    Button loadBtn;
    @InjectView(R.id.async_text)
    TextView asyncText;
    @InjectView(R.id.web_text)
    TextView webTv;
    @InjectView(R.id.web_image)
    ImageView webIv;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        taskExecutorInterface.execute(new StringAsycTask(this, this));
        taskExecutorInterface.execute(new WebAsyncTask(this, URL));

        loadBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                taskExecutorInterface.execute(new BitmapAsycTask(TaskActivity.this, IMAGE_URL));
            }
        });
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
}
