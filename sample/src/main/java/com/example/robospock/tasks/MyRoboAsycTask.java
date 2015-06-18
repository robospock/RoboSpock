package com.example.robospock.tasks;

import android.content.Context;
import roboguice.util.RoboAsyncTask;

public abstract class MyRoboAsycTask<T> extends RoboAsyncTask<T> {
    protected MyRoboAsycTask(final Context context) {
        super(context);
    }

    public void onSuccess(final T t) throws Exception {
        super.onSuccess(t);
    }

    ;

}
