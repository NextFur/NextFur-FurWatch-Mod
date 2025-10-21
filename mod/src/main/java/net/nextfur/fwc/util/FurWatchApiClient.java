package net.nextfur.fwc.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.nextfur.fwc.CommonConfig;
import net.nextfur.fwc.FwMain;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FurWatchApiClient {
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    private static final String API_BASE_URL = "https://api.nextfur.net/v2/client/furwatch/";
    private static final Gson gson = new Gson();

    public static CompletableFuture<ApiResponse> setLoveLevel(String username, int level) {
        return makeApiRequest("set", "set", username, "lovelevel", level);
    }

    public static CompletableFuture<ApiResponse> getLoveLevel(String username) {
        return makeApiRequest("get", "get", username, "lovelevel", 0);
    }

    public static CompletableFuture<ApiResponse> addLoveLevel(String username, int amount) {
        return makeApiRequest("set", "add", username, "lovelevel", amount);
    }

    public static CompletableFuture<ApiResponse> subtractLoveLevel(String username, int amount) {
        return makeApiRequest("set", "subtract", username, "lovelevel", amount);
    }

    private static CompletableFuture<ApiResponse> makeApiRequest(String action, String args, String username, String type, int loveLevel) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String token = CommonConfig.getAuthToken();
                if (token == null || token.isEmpty()) {
                    FwMain.LOGGER.error("[FURSMP] No auth token available for API request");
                    return new ApiResponse(false, "No authentication token available", -1);
                }

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("token", token);
                requestBody.put("args", args);
                requestBody.put("username", username);
                requestBody.put("type", type);
                requestBody.put("lovelevel", loveLevel);

                String jsonRequest = gson.toJson(requestBody);
                String url = API_BASE_URL + action;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                        .timeout(Duration.ofSeconds(30))
                        .build();

                if (CommonConfig.debugMode) {
                    FwMain.LOGGER.info("[FURSMP] Making API request to: {}", url);
                    FwMain.LOGGER.info("[FURSMP] Request body: {}", jsonRequest);
                }

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (CommonConfig.debugMode) {
                    FwMain.LOGGER.info("[FURSMP] API response status: {}", response.statusCode());
                    FwMain.LOGGER.info("[FURSMP] API response body: {}", response.body());
                }

                if (response.statusCode() != 200) {
                    FwMain.LOGGER.error("[FURSMP] API request failed with status: {}", response.statusCode());
                    return new ApiResponse(false, "API request failed with status: " + response.statusCode(), -1);
                }

                JsonObject apiResponse = gson.fromJson(response.body(), JsonObject.class);

                boolean success = apiResponse.has("error") && apiResponse.get("error").isJsonNull();
                String message = apiResponse.has("message") ? apiResponse.get("message").getAsString() : "No message";
                int currentLevel = apiResponse.has("lovelevel") ? apiResponse.get("lovelevel").getAsInt() : -1;

                return new ApiResponse(success, message, currentLevel);

            } catch (Exception e) {
                FwMain.LOGGER.error("[FURSMP] Exception during API request: ", e);
                return new ApiResponse(false, "Request failed: " + e.getMessage(), -1);
            }
        });
    }

    public static class ApiResponse {
        private final boolean success;
        private final String message;
        private final int loveLevel;

        public ApiResponse(boolean success, String message, int loveLevel) {
            this.success = success;
            this.message = message;
            this.loveLevel = loveLevel;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getLoveLevel() {
            return loveLevel;
        }
    }
}