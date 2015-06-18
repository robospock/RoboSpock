package com.example.robospock.tasks;

public class TestTaskExecutor implements TaskExecutorInterface {
    @Override
    public void execute(final MyRoboAsycTask task) {
        try {
            task.onSuccess(task.call());
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
