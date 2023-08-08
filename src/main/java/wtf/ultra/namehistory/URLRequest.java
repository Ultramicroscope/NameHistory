package wtf.ultra.namehistory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class URLRequest {
    private final URL url;

    public URLRequest(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid URL format: " + url);
        }
    }

    public HttpURLConnection buildConnection() {
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) url.openConnection();
            String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36";
            con.addRequestProperty("User-Agent", userAgent);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("HTTPS Could not open connection to " + url);
        }
        return con;
    }

    public String getResponse() {
        HttpURLConnection con = buildConnection();
        String response;
        try {
            con.connect();
            InputStream inputStream;
            int code = con.getResponseCode();
            if (200 <= code && code <= 299) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            con.disconnect();
            response = result.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("HTTP GetResponse Could not open connection to " + url);
        }
        System.out.println("response:\n" + response);
        return response;
    }
}