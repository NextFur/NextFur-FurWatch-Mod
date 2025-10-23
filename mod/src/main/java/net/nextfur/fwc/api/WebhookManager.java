package net.nextfur.fwc.api;

import com.google.gson.Gson;
import net.nextfur.fwc.ServerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WebhookManager {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private static final Gson GSON = new Gson();

    public static boolean postWebhook(String username, List<String> modlist) {
        try {
            List<String> allowedMods = ServerConfig.allowedMods != null
                    ? ServerConfig.allowedMods
                    : Collections.emptyList();

            List<String> safeModList = modlist != null
                    ? modlist
                    : Collections.emptyList();

            List<String> addedMods = safeModList.stream()
                    .filter(mod -> !allowedMods.contains(mod))
                    .toList();

            List<String> removedMods = allowedMods.stream()
                    .filter(mod -> !safeModList.contains(mod))
                    .toList();

            List<String> gaylist = List.of(";3", "OwO", "UwU", "^-^", "*rawr*", "*nuzzles*", "*nya~*", "x3", ":3", "-w-\"");

            String content = ":inbox_tray: **|** ` " + username + " ` entrou no servidor " + gaylist.get((int) (Math.random() * gaylist.size()));

            if (!addedMods.isEmpty()) {
                content += "\n\n**Mods extras**: ```" + String.join(", ", addedMods) + "```";
                LOGGER.info("User {} has added mods: {}", username, String.join(", ", addedMods));
            }

            if (!removedMods.isEmpty()) {
                content += "\n\n**Mods removidos**: ```" + String.join(", ", removedMods) + "```";
                LOGGER.info("User {} has removed mods: {}", username, String.join(", ", removedMods));
            }

            String json = GSON.toJson(Map.of("content", content));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ServerConfig.furwatch_webhook))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 204;
        } catch (Exception e) {
            return false;
        }
    }

}
