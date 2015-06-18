package com.example.robospock.module;

import com.example.robospock.tasks.ProductionTaskExecutor;
import com.example.robospock.tasks.TaskExecutorInterface;
import com.google.inject.AbstractModule;

public class TaskExecutorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TaskExecutorInterface.class).to(ProductionTaskExecutor.class);
    }
}