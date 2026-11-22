package com.example.GardenersApp.api;

import com.example.GardenersApp.domain.GoogleAccount;
import com.example.GardenersApp.repository.GoogleAccountRepository;
import com.example.GardenersApp.service.GoogleHttp;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
    private final GoogleAccountRepository accounts;
    private final Gson json = new Gson();
    @Value("${google.oauth.client-id:}")
    private String clientId;
    @Value("${google.oauth.client-secret:}")
    private String clientSecret;

    public CalendarController(GoogleAccountRepository accounts) {
        this.accounts = accounts;
    }

    @PostMapping("/events")
    public Map<String, Object> createEvent(@RequestHeader(value = "Authorization", required = false) String authorization,
                                           @RequestBody Map<String, Object> event) {
        GoogleAccount account = authenticatedAccount(authorization);
        if (!(event.get("summary") instanceof String summary) || summary.isBlank()
                || !hasDateTime(event.get("start")) || !hasDateTime(event.get("end"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event needs summary, start.dateTime, and end.dateTime");
        }
        try {
            GoogleHttp.Response response = sendEvent(account.getAccessToken(), event);
            if (response.statusCode() == 401) {
                String refreshed = refresh(account);
                response = sendEvent(refreshed, event);
            }
            if (response.statusCode() / 100 != 2)
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, googleError(response.body(), "Google Calendar rejected the event"));
            return json.fromJson(response.body(), Map.class);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Calendar request was interrupted");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Calendar request failed");
        }
    }

    private GoogleHttp.Response sendEvent(String accessToken, Map<String, Object> event) throws Exception {
        return GoogleHttp.postJson("https://www.googleapis.com/calendar/v3/calendars/primary/events",
                accessToken, json.toJson(event));
    }

    private String refresh(GoogleAccount account) throws Exception {
        String refreshToken = account.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google Calendar permission must be granted again");
        String body = "client_id=" + enc(clientId) + "&client_secret=" + enc(clientSecret)
                + "&refresh_token=" + enc(refreshToken) + "&grant_type=refresh_token";
        GoogleHttp.Response response = GoogleHttp.postForm("https://oauth2.googleapis.com/token", body);
        if (response.statusCode() / 100 != 2)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, googleError(response.body(), "Could not refresh Google Calendar access"));
        JsonObject tokenResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        String accessToken = tokenResponse.has("access_token") ? tokenResponse.get("access_token").getAsString() : "";
        if (accessToken.isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google returned no access token");
        account.updateTokens(accessToken, null);
        accounts.save(account);
        return accessToken;
    }

    private GoogleAccount authenticatedAccount(String header) {
        if (header == null || !header.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in is required");
        try {
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(header.substring(7).getBytes(StandardCharsets.UTF_8)));
            return accounts.findBySessionTokenHashAndSessionExpiresAtAfter(hash, Instant.now())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired"));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String googleError(String body, String fallback) {
        try {
            JsonObject response = JsonParser.parseString(body).getAsJsonObject();
            if (response.has("error") && response.get("error").isJsonObject()) {
                JsonObject error = response.getAsJsonObject("error");
                if (error.has("message")) return fallback + ": " + error.get("message").getAsString();
            }
            if (response.has("error_description"))
                return fallback + ": " + response.get("error_description").getAsString();
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private boolean hasDateTime(Object value) {
        if (!(value instanceof Map<?, ?> map)) return false;
        return map.get("dateTime") instanceof String dateTime && !dateTime.isBlank();
    }
}
