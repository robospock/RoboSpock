package com.example.robospock.web;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebInterfaceImpl implements WebInterface {
    @Override
    public String execute(final String resource) throws IllegalStateException, IOException {
        final InputStream content = getStream(resource);
        return convertStreamToString(content);
    }

    @Override
    public File downloadFile(final String resource, final String path) throws IOException {
        final InputStream content = getStream(resource);
        final File file = new File(path);
        saveStreamToFile(content, file);
        return file;

    }

    private InputStream getStream(final String resource) throws IOException {
        URL url = new URL(resource);
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        // expect HTTP 200 OK, so we don't mistakenly save error report
        // instead of the file
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Server returned HTTP " + connection.getResponseCode()
                    + " " + connection.getResponseMessage());
        }
        return connection.getInputStream();
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
