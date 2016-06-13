package com.example.robospock.web;

import java.io.File;
import java.io.IOException;

public interface WebInterface {
    String execute(String resource) throws IllegalStateException, IOException;

    File downloadFile(String resource, String path) throws IOException;
}
