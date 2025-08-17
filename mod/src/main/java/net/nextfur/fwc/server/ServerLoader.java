package net.nextfur.fwc.server;

import net.nextfur.fwc.FwMain;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = FwMain.MODID) 
public class ServerLoader {
    public static final Map<String, String> pendingTokens = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            String username = player.getName().getString();
            String token = pendingTokens.remove(username);

            if (token == null) {
                Component kickMessage = Component.literal("[FURSMP] Auth Failed: Client did not provide a token.");

                player.connection.disconnect(kickMessage);
                FwMain.LOGGER.warn("[FURSMP] User: " + username + " connected without an auth token and was kicked.");
                return;
            }

            if (!PlayerAuthenticator.authenticatePlayer(username, token)) {
                Component kickMessage = Component.literal("[FURSMP] Auth Failed: You are not Whitelisted or your token is invalid.");
                player.connection.disconnect(kickMessage);
                FwMain.LOGGER.warn("[FURSMP] User: " + username + " failed to authenticate and was kicked.");
            }
        }
    }
}