package net.nextfur.fwc.server;

import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.network.common.FlashlightStateS2CPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FlashlightServerState {

    private static final Map<UUID, Boolean> STATES = new HashMap<>();

    private FlashlightServerState() {
    }

    public static void setPlayerState(UUID playerId, boolean enabled) {
        if (enabled) {
            STATES.put(playerId, true);
        } else {
            STATES.remove(playerId);
        }

        PacketDistributor.sendToAllPlayers(new FlashlightStateS2CPacket(playerId, enabled));
    }

    public static Map<UUID, Boolean> snapshot() {
        return new HashMap<>(STATES);
    }

    public static void clearPlayer(UUID playerId) {
        if (STATES.remove(playerId) != null) {
            PacketDistributor.sendToAllPlayers(new FlashlightStateS2CPacket(playerId, false));
        }
    }
}
