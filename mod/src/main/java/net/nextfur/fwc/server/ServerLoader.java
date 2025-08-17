package net.nextfur.fwc.server; 

import net.nextfur.fwc.FwMain;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber; 
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = FwMain.MODID)
public class ServerLoader {

    public static final Map<String, String> pendingTokens = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        String username = event.getEntity().getName().getString();

        String token = pendingTokens.remove(username);

        if (token == null) {
            Component kickMessage = Component.literal("[FURSMP] Authentication Failed: Client did not provide a token.");
            event.getEntity().connection.disconnect(kickMessage);
            FwMain.LOGGER.warn("[FURSMP] User: " + username + " connected without an auth token and was kicked.");
            return;
        }

        boolean isAuthorized = PlayerAuthenticator.authenticatePlayer(username, token);
        if (!isAuthorized) {
            Component kickMessage = Component.literal("[FURSMP] Authentication Failed. You are not Whitelisted or your token is invalid.");
            event.getEntity().connection.disconnect(kickMessage);
            FwMain.LOGGER.warn("[FURSMP] User: " + username + " failed to authenticate and was kicked from the server.");
        }
    }
}