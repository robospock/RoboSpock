package pl.polidea.tddandroid.module;

import pl.polidea.tddandroid.tasks.ProductionTaskExecutor;
import pl.polidea.tddandroid.tasks.TaskExecutorInterface;

import com.google.inject.AbstractModule;

public class TaskExecutorModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TaskExecutorInterface.class).to(ProductionTaskExecutor.class);
    }
}