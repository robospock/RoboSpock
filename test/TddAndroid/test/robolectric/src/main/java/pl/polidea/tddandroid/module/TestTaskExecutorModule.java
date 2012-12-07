package pl.polidea.tddandroid.module;

import com.google.inject.AbstractModule;
import pl.polidea.tddandroid.tasks.TaskExecutorInterface;
import pl.polidea.tddandroid.tasks.TestTaskExecutor;

public class TestTaskExecutorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TaskExecutorInterface.class).to(TestTaskExecutor.class);
    }
}