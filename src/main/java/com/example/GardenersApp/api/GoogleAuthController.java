package com.example.GardenersApp.api;

import com.example.GardenersApp.domain.GoogleAccount;
import com.example.GardenersApp.repository.CustomerRepository;
import com.example.GardenersApp.repository.GardenTaskRepository;
import com.example.GardenersApp.repository.GoogleAccountRepository;
import com.example.GardenersApp.service.GoogleHttp;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@RestController
@RequestMapping("/api/auth/google")
public class GoogleAuthController {
    private final GoogleAccountRepository accounts;
    private final CustomerRepository customers;
    private final GardenTaskRepository tasks;
    private final Gson json = new Gson();
    private final SecureRandom random = new SecureRandom();
    @Value("${google.oauth.client-id:}")
    private String clientId;
    @Value("${google.oauth.client-secret:}")
    private String clientSecret;

    public GoogleAuthController(GoogleAccountRepository accounts, CustomerRepository customers, GardenTaskRepository tasks) {
        this.accounts = accounts;
        this.customers = customers;
        this.tasks = tasks;
    }

    @PostMapping(value = "/exchange", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public AuthResponse exchange(@RequestHeader(value = "X-Requested-With", required = false) String requestOrigin,
                                 @Valid @RequestBody CodeRequest request) {
        if (!"XMLHttpRequest".equals(requestOrigin)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sign-in request");
        }
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google OAuth is not configured");
        }
        try {
            String form = "code=" + enc(request.code()) + "&client_id=" + enc(clientId)
                    + "&client_secret=" + enc(clientSecret) + "&redirect_uri="
                    + "&grant_type=authorization_code";
            JsonObject tokens = postForm("https://oauth2.googleapis.com/token", form);
            String accessToken = required(tokens, "access_token");
            JsonObject profile = getJson("https://openidconnect.googleapis.com/v1/userinfo", accessToken);
            String googleId = required(profile, "sub");
            String email = required(profile, "email");
            if (!profile.has("email_verified") || !profile.get("email_verified").getAsBoolean()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google email is not verified");
            }
            String sessionToken = newSessionToken();
            Instant expires = Instant.now().plus(30, ChronoUnit.DAYS);
            GoogleAccount account = accounts.findByGoogleId(googleId).orElse(null);
            String refresh = tokens.has("refresh_token") ? tokens.get("refresh_token").getAsString() : null;
            if (account == null) {
                account = new GoogleAccount(googleId, email, profile.has("name") ? profile.get("name").getAsString() : null, accessToken,
                        refresh, hash(sessionToken), expires);
            } else {
                account.updateProfile(email, profile.has("name") ? profile.get("name").getAsString() : null);
                account.updateTokens(accessToken, refresh);
                account.updateSession(hash(sessionToken), expires);
            }
            account = accounts.save(account);
            // Preserve existing single-user installs by assigning legacy rows to the first account ever created.
            GoogleAccount originalOwner = accounts.findFirstByOrderByIdAsc().orElse(account);
            customers.claimUnowned(originalOwner);
            tasks.claimUnowned(originalOwner);
            return new AuthResponse(sessionToken, account.getId(), googleId, email, account.getDisplayName());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Google authentication failed");
        }
    }

    @PostMapping("/logout")
    @Transactional
    public void logout(@RequestAttribute("authenticatedGoogleAccount") GoogleAccount account) {
        try {
            account.updateSession(hash(newSessionToken()), Instant.now());
            accounts.save(account);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not end session");
        }
    }

    private JsonObject postForm(String url, String body) throws Exception {
        GoogleHttp.Response response = GoogleHttp.postForm(url, body);
        if (response.statusCode() / 100 != 2) {
            String reason = "Google rejected the authorization code";
            try {
                JsonObject error = JsonParser.parseString(response.body()).getAsJsonObject();
                if (error.has("error_description")) reason += ": " + error.get("error_description").getAsString();
                else if (error.has("error")) reason += " (" + error.get("error").getAsString() + ")";
            } catch (Exception ignored) {
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, reason);
        }
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private JsonObject getJson(String url, String token) throws Exception {
        GoogleHttp.Response response = GoogleHttp.get(url, token);
        if (response.statusCode() / 100 != 2)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Could not retrieve Google profile");
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private String required(JsonObject node, String key) {
        String value = node.has(key) ? node.get(key).getAsString() : "";
        if (value.isBlank())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google response is missing " + key);
        return value;
    }

    private String newSessionToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    private String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record CodeRequest(@NotBlank String code) {
    }

    public record AuthResponse(String sessionToken, Long userId, String googleId, String email, String displayName) {
    }
}
