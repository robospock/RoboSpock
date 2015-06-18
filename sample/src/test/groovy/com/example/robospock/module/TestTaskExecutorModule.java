package com.example.robospock.module;

import com.google.inject.AbstractModule;
import com.example.robospock.tasks.TaskExecutorInterface;
import com.example.robospock.tasks.TestTaskExecutor;

public class TestTaskExecutorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TaskExecutorInterface.class).to(TestTaskExecutor.class);
    }

}
