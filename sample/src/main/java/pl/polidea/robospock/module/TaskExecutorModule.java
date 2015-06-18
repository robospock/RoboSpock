package pl.polidea.robospock.module;

import com.google.inject.AbstractModule;
import pl.polidea.robospock.tasks.ProductionTaskExecutor;
import pl.polidea.robospock.tasks.TaskExecutorInterface;

public class TaskExecutorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TaskExecutorInterface.class).to(ProductionTaskExecutor.class);
    }
}