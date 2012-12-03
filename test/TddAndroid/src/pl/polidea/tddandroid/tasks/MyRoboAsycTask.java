package pl.polidea.tddandroid.tasks;

import roboguice.util.RoboAsyncTask;
import android.content.Context;

public abstract class MyRoboAsycTask<T> extends RoboAsyncTask<T> {

    protected MyRoboAsycTask(final Context context) {
        super(context);
    }

    public void onSuccess(final T t) throws Exception {
        super.onSuccess(t);
    };

}
