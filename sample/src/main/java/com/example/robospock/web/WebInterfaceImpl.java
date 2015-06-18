package com.example.robospock.web;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class WebInterfaceImpl implements WebInterface {
    @Override
    public String execute(final String resource) throws IllegalStateException, ClientProtocolException, IOException {
        final InputStream content = getStream(resource);
        return convertStreamToString(content);
    }

    @Override
    public File downloadFile(final String resource, final String path) throws ClientProtocolException, IOException {
        final InputStream content = getStream(resource);
        final File file = new File(path);
        saveStreamToFile(content, file);
        return file;

    }

    private InputStream getStream(final String resource) throws IOException, ClientProtocolException {
        return new DefaultHttpClient().execute(new HttpGet(resource)).getEntity().getContent();
    }

    public static String convertStreamToString(final InputStream is) throws IOException {
        if (is == null) {
            throw new IOException("Empty stream");
        } else {
            final Writer writer = new StringWriter();

            final char[] buffer = new char[1024];
            try {
                final Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        }
    }

    public static void saveStreamToFile(final InputStream is, final File file) throws IOException {
        if (is == null) {
            throw new IOException("Empty stream");
        }
        final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
        final byte[] data = new byte[16384];
        int n;
        while ((n = is.read(data, 0, data.length)) != -1) {
            os.write(data, 0, n);
        }
        os.flush();
        os.close();
    }

}
