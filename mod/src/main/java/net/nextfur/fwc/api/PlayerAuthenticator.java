package net.nextfur.fwc.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class PlayerAuthenticator {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private static final String API_URL = "https://api.nextfur.net/v2/client/authenticate";
    private static final Gson GSON = new Gson();

    public static boolean authenticatePlayer(String username, String token) {
        try {
            String json = GSON.toJson(Map.of("username", username, "token", token));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return false;

            JsonObject body = GSON.fromJson(response.body(), JsonObject.class);
            return body.has("authenticated") && body.get("authenticated").getAsBoolean()
                    && body.has("whitelisted") && body.get("whitelisted").getAsBoolean();

        } catch (Exception e) {
            return false;
        }
    }
}
