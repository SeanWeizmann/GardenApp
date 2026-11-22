package com.example.GardenersApp.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public final class GoogleHttp {
    private GoogleHttp() {
    }

    public static Response postForm(String url, String body) throws IOException {
        return send(url, "POST", "application/x-www-form-urlencoded", null, body);
    }

    public static Response postJson(String url, String bearer, String body) throws IOException {
        return send(url, "POST", "application/json", bearer, body);
    }

    public static Response get(String url, String bearer) throws IOException {
        return send(url, "GET", null, bearer, null);
    }

    private static Response send(String url, String method, String contentType, String bearer, String body) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(15_000);
        if (contentType != null) connection.setRequestProperty("Content-Type", contentType);
        if (bearer != null) connection.setRequestProperty("Authorization", "Bearer " + bearer);
        if (body != null) {
            connection.setDoOutput(true);
            try (var output = connection.getOutputStream()) {
                output.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }
        int status = connection.getResponseCode();
        try {
            var stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            String response;
            if (stream == null) response = "";
            else try (stream) {
                response = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
            return new Response(status, response);
        } finally {
            connection.disconnect();
        }
    }

    public record Response(int statusCode, String body) {
    }
}
