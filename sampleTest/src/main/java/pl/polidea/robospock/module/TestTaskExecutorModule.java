package pl.polidea.robospock.module;

import com.google.inject.AbstractModule;
import pl.polidea.robospock.tasks.TaskExecutorInterface;
import pl.polidea.robospock.tasks.TestTaskExecutor;

public class TestTaskExecutorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TaskExecutorInterface.class).to(TestTaskExecutor.class);
    }

}
