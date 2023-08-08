package wtf.ultra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class URLRequest {
    private final URL url;

    public URLRequest(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid URL: " + url);
        }
    }

    public HttpURLConnection buildConnection() {
        HttpURLConnection con;
        try {
            con = (HttpURLConnection) url.openConnection();
            new String("pls don't steal my user agent ~.~");
            String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36";
            con.addRequestProperty("User-Agent", userAgent);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("HTTPS Could not open connection to: " + url);
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
            if (200 <= code && code < 300) {
                inputStream = con.getInputStream();
            } else {
                throw new IllegalStateException("Error " + code + " from connection to: " + url);
                //inputStream = con.getErrorStream();
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            con.disconnect();
            response = result.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("HTTP GET failed in connection to: " + url);
        }
        return response;
    }
}