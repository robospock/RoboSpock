package com.example.robospock.tasks;

import com.example.robospock.activity.TaskActivity;
import com.example.robospock.web.WebInterface;
import com.google.inject.Inject;

public class WebAsyncTask extends MyRoboAsycTask<String> {
    private final String resource;
    @Inject
    WebInterface webInterface;
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
