package net.nextfur.fwc.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.nextfur.fwc.FwMain; // Importe o FwMain para usar o LOGGER
import net.nextfur.fwc.ServerConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture; // Importação necessária

public class PlayerAuthenticator {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final String API_URL = ServerConfig.login_url;
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
            FwMain.LOGGER.warn("Falha na autenticacao de {}: {}", username, e.getMessage());
            return false;
        }
    }

    public static CompletableFuture<Boolean> authenticatePlayerAsync(String username, String token) {
        if (API_URL == null || API_URL.isBlank()) {
            FwMain.LOGGER.error("URL de login não está configurada no server config!");
            return CompletableFuture.completedFuture(false);
        }

        try {
            String json = GSON.toJson(Map.of("username", username, "token", token));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() != 200) {
                            FwMain.LOGGER.warn("API de login retornou status {} para {}", response.statusCode(), username);
                            return false;
                        }

                        JsonObject body = GSON.fromJson(response.body(), JsonObject.class);
                        boolean authenticated = body.has("authenticated") && body.get("authenticated").getAsBoolean();
                        boolean whitelisted = body.has("whitelisted") && body.get("whitelisted").getAsBoolean();

                        return authenticated && whitelisted;
                    })
                    .exceptionally(e -> {
                        FwMain.LOGGER.error("Erro na autenticacao assincrona de {}: {}", username, e.getMessage());
                        return false;
                    });

        } catch (Exception e) {
            FwMain.LOGGER.error("Erro ao preparar autenticacao de {}: {}", username, e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
}