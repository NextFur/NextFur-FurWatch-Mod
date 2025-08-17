package net.nextfur.fwc.server;

import net.nextfur.fwc.FwMain;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class PlayerAuthenticator {
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    private static final String API_ENDPOINT = "https://api.nextfur.net/v2/client/authenticate";
    private static final Gson gson = new Gson();

    public static boolean authenticatePlayer(String username, String token) {
        Map<String, String> requestBody = new HashMap<>();
        data.put("username", username);
        data.put("token", token);
        String jsonRequest = gson.toJson(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200) {
                FwMain.LOGGER.error("[FURSMP] User: " + username + " Authentication Failed, with error: "  + response.statusCode());
                return false;
            }

            JsonObject apiResponse = gson.fromJson(response.body(), JsonObject.class);
            boolean isAuthenticated = apiResponse.get("authenticated") && apiResponse.get("authenticated").getAsBoolean();
            boolean isWhitelisted = apiResponse.get("whitelisted") && apiResponse.get("whitelisted").getAsBoolean();

            if(isAuthenticated && isWhitelisted) {
                FwMain.LOGGER.info("[FURSMP] User: " + username + " authenticated successfully.");
                return true;
            } else {
                FwMain.LOGGER.warn("[FURSMP] User: " + username + " is not whitelisted or authentication failed.");
                return false;
            }

        } catch (Exception e) {
            FwMain.LOGGER.error("[FURSMP] User: " + username + " Authentication Failed with exception: ", e);
            e.printStackTrace();
            return false;
        }
    }
}
