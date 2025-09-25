package net.nextfur.fwc.util.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge; 
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.nextfur.fwc.commands.OffRpCommand;

import java.util.UUID;

public class HologramEventHandler {

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            UUID playerUUID = player.getUUID();

            if (OffRpCommand.activeHolograms.containsKey(playerUUID)) {
                ArmorStand hologram = OffRpCommand.activeHolograms.get(playerUUID);

                if (hologram != null && hologram.isAlive()) {
                    double newX = player.getX();
                    double newY = player.getY() + player.getBbHeight() + 0.5;
                    double newZ = player.getZ();
                    
                    hologram.setPos(newX, newY, newZ);
                } else {
                    OffRpCommand.activeHolograms.remove(playerUUID);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player != null) {
            UUID playerUUID = player.getUUID();
            if (OffRpCommand.activeHolograms.containsKey(playerUUID)) {
                ArmorStand hologram = OffRpCommand.activeHolograms.get(playerUUID);
                if (hologram != null) {
                    hologram.discard();
                }
                OffRpCommand.activeHolograms.remove(playerUUID);
            }
        }
    }
}