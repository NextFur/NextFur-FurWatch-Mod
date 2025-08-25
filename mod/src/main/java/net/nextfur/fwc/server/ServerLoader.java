package net.nextfur.fwc.server;

import net.nextfur.fwc.FwMain;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Timer;
import java.util.TimerTask;

@EventBusSubscriber(modid = FwMain.MODID)
public class ServerLoader {

    public static final Map<String, String> pendingTokens = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String username = player.getName().getString();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    player.server.execute(() -> {
                        String token = pendingTokens.remove(username);

                        if (token == null) {
                            Component kickMessage = Component.literal("[FURSMP] Auth Failed: Client did not provide a token.");
                            player.connection.disconnect(kickMessage);
                            FwMain.LOGGER.warn("[FURSMP] User: {} connected without an auth token and was kicked.", username);
                            return; 
                        }
                        FwMain.LOGGER.info("[FURSMP] User: {} has a token. Starting async auth...", username);

                        CompletableFuture.supplyAsync(() -> PlayerAuthenticator.authenticatePlayer(username, token)).thenAccept(isAuthenticated -> {
                            if(!isAuthenticated) {
                                player.server.execute(() -> {
                                    Component kickMessage = Component.literal("[FURSMP] AUTH FAILED: Você não está com Whitelist ou seu Token está inválido! Abra um ticket!");
                                    player.connection.disconnect(kickMessage);
                                    FwMain.LOGGER.warn("[FURSMP] User: {} failed to authenticate and was kicked.", username);
                                });
                            } else {
                                FwMain.LOGGER.info("[FURSMP] User: {} successfully authenticated.", username);
                            }
                        }).exceptionally(ex -> {
                            FwMain.LOGGER.error("[FURSMP] An error occured during async auth for: {}: {}", username, ex.getMessage());
                            player.server.execute(() -> {
                                Component kickMessage = Component.literal("[FURSMP] Um erro interno ocorreu durante a autenticação. Código: 556");
                                player.connection.disconnect(kickMessage);
                            });
                            return null;
                        });
                    });
                }
            }, 1000);
        }
    }
}