package com.example.robospock.web;

import org.apache.http.client.ClientProtocolException;

import java.io.File;
import java.io.IOException;

public interface WebInterface {
    String execute(String resource) throws IllegalStateException, ClientProtocolException, IOException;

    File downloadFile(String resource, String path) throws ClientProtocolException, IOException;
}
