package pl.polidea.tddandroid.tasks;

import pl.polidea.tddandroid.activity.TaskActivity;
import android.content.Context;

public class StringAsycTask extends MyRoboAsycTask<String> {
    /**
     * 
     */
    private final TaskActivity taskActivity;

    public StringAsycTask(final TaskActivity taskActivity, final Context context) {
        super(context);
        this.taskActivity = taskActivity;
    }

    @Override
    public String call() throws Exception {
        return "WebText";
    }

    @Override
    public void onSuccess(final String t) throws Exception {
        super.onSuccess(t);
        this.taskActivity.setAsyncTest(t);
    }
}