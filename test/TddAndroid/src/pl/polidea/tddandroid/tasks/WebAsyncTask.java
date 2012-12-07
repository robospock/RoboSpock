package pl.polidea.tddandroid.tasks;

import com.google.inject.Inject;
import pl.polidea.tddandroid.activity.TaskActivity;
import pl.polidea.tddandroid.web.WebInterface;

public class WebAsyncTask extends MyRoboAsycTask<String> {
    private final String resource;
    @Inject WebInterface webInterface;
    private final TaskActivity actvity;

    public WebAsyncTask(final TaskActivity actvity, final String resource) {
        super(actvity);
        this.actvity = actvity;
        this.resource = resource;
    }

    @Override
    public String call() throws Exception {
        return webInterface.execute(resource);
    }

    @Override
    public void onSuccess(final String t) throws Exception {
        super.onSuccess(t);
        actvity.setWebText(t);

    }

}
