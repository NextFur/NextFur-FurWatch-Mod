package net.nextfur.fwc.api;

import com.google.gson.Gson;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.ServerConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PlayerAuthenticator {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final String API_URL = ServerConfig.login_url;
    private static final Gson GSON = new Gson();

    public static CompletableFuture<Integer> authenticatePlayerAsync(String username, String token) {
        if (API_URL == null || API_URL.isBlank()) {
            FwMain.LOGGER.error("URL de login não está configurada no server config!");
            return CompletableFuture.completedFuture(500);
        }

        try {
            String json = GSON.toJson(Map.of("username", username, "token", token));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode())
                    .exceptionally(e -> 500);
        } catch (Exception e) {
            FwMain.LOGGER.error("Erro ao preparar autenticacao de {}: {}", username, e.getMessage());
            return CompletableFuture.completedFuture(500);
        }
    }
}