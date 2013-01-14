package pl.polidea.tddandroid.module;

import com.google.inject.AbstractModule;
import pl.polidea.tddandroid.tasks.ProductionTaskExecutor;
import pl.polidea.tddandroid.tasks.TaskExecutorInterface;

public class TaskExecutorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TaskExecutorInterface.class).to(ProductionTaskExecutor.class);
    }
}