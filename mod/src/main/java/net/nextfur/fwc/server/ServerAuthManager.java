package net.nextfur.fwc.server;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.api.PlayerAuthenticator;
import net.nextfur.fwc.network.nextfur.ServerAuthRequestPacket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ServerAuthManager {
    private static final Map<UUID, AuthenticationState> playerStates = new ConcurrentHashMap<>();
    private static final int AUTH_TIMEOUT_SECONDS = 30; // Timeout duration in seconds

    private static class AuthenticationState {
        final long joinTime;
        boolean authenticated;

        AuthenticationState() {
            this.joinTime = System.currentTimeMillis();
            this.authenticated = false;
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        playerStates.put(player.getUUID(), new AuthenticationState());
        PacketDistributor.sendToPlayer(player, new ServerAuthRequestPacket());
        FwMain.LOGGER.info("Solicitando token: " + player.getGameProfile().getName());
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        long currentTime = System.currentTimeMillis();
        playerStates.forEach((uuid, state) -> {
            if (!state.authenticated &&
                    TimeUnit.MILLISECONDS.toSeconds(currentTime - state.joinTime) >= AUTH_TIMEOUT_SECONDS) {

                ServerPlayer player = event.getServer().getPlayerList().getPlayer(uuid);
                if (player != null) {
                    kickPlayer(player, ChatFormatting.RED + "[FURSMP] Tempo de resposta para login expirado.\nCaso este erro persista, abra um ticket em nosso Discord.");
                }
                playerStates.remove(uuid);
            }
        });
    }

    @SubscribeEvent
    public void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        playerStates.remove(event.getEntity().getUUID());
    }

    public static void handleAuthResponse(ServerPlayer player, String token, String username) {
        AuthenticationState state = playerStates.get(player.getUUID());
        if (state == null) return;

        if (PlayerAuthenticator.authenticatePlayer(username, token)) {
            state.authenticated = true;
            FwMain.LOGGER.info("Jogador autenticado: " + username);
        } else {
            kickPlayer(player, ChatFormatting.RED + "[FURSMP] Falha no login\nCaso este erro persista, abra um ticket em nosso Discord.");
            playerStates.remove(player.getUUID());
        }
    }

    private static void kickPlayer(ServerPlayer player, String reason) {
        player.connection.disconnect(Component.literal(reason));
    }

    public static boolean isAuthenticated(UUID playerId) {
        AuthenticationState state = playerStates.get(playerId);
        return state != null && state.authenticated;
    }
}
