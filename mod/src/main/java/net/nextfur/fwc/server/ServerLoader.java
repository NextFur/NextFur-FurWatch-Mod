package net.nextfur.fwc.server; 

import net.nextfur.fwc.FwMain;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Mod.EventBusSubscriber(modid = FwMain.MODID)
public class ServerLoader {
    public static final Map<String, String> pendingTokens = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.ConnectionFromClient event) {
        String username = event.getPlayerName(); 
        String token = pendingTokens.remove(username); 

        if (token == null) {
            Component kickMessage = Component.literal("[FURSMP] Authentication Failed: Client did not provide a token.");
            event.getConnection().disconnect(kickMessage);
            FwMain.LOGGER.warn("[FURSMP] User: " + username + " connected without an auth token and was kicked.");
            return; 
        }

        boolean isAuthorized = PlayerAuthenticator.authenticatePlayer(username, token);
        if(!isAuthorized) {
            Component kickMessage = Component.literal("[FURSMP] Authentication Failed. You are not Whitelisted or your token is invalid.");
            event.getConnection().disconnect(kickMessage);
            FwMain.LOGGER.warn("[FURSMP] User: " + username + " failed to authenticate and was kicked from the server.");
        }
    }
}