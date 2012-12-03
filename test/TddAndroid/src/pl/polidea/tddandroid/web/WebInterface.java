package pl.polidea.tddandroid.web;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

public interface WebInterface {

    String execute(String resource) throws IllegalStateException, ClientProtocolException, IOException;

    File downloadFile(String resource, String path) throws ClientProtocolException, IOException;
}
