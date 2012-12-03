package pl.polidea.tddandroid.module;

import pl.polidea.tddandroid.tasks.TaskExecutorInterface;
import pl.polidea.tddandroid.tasks.TestTaskExecutor;

import com.google.inject.AbstractModule;

public class TestTaskExecutorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TaskExecutorInterface.class).to(TestTaskExecutor.class);
    }
}