package pl.polidea.tddandroid.tasks;

import pl.polidea.tddandroid.activity.MainActivity;
import android.content.Context;

public class StringAsycTask extends MyRoboAsycTask<String> {
    /**
     * 
     */
    private final MainActivity mainActivity;

    public StringAsycTask(final MainActivity mainActivity, final Context context) {
        super(context);
        this.mainActivity = mainActivity;
    }

    @Override
    public String call() throws Exception {
        return "WebText";
    }

    @Override
    public void onSuccess(final String t) throws Exception {
        super.onSuccess(t);
        this.mainActivity.setAsyncTest(t);
    }
}