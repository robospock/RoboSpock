package com.example.robospock.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.robospock.activity.TaskActivity;
import com.example.robospock.web.WebInterface;
import com.google.inject.Inject;

import java.io.File;

public class BitmapAsycTask extends MyRoboAsycTask<Bitmap> {
    private final TaskActivity taskActivity;
    @Inject
    WebInterface webInterface;
    private final String url;

    public BitmapAsycTask(final TaskActivity taskActivity, final String url) {
        super(taskActivity);
        this.taskActivity = taskActivity;
        this.url = url;
    }

    @Override
    public Bitmap call() throws Exception {
        final String path = taskActivity.getCacheDir().getPath() + "/image";
        final File file = webInterface.downloadFile(url, path);
        return BitmapFactory.decodeFile(file.getPath());
    }

    @Override
    public void onSuccess(final Bitmap t) throws Exception {
        super.onSuccess(t);
        taskActivity.setImageBitmap(t);
    }
}
