package net.nextfur.fwc.util.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.world.phys.Vec3;
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
                TextDisplay hologram = OffRpCommand.activeHolograms.get(playerUUID);

                if (hologram != null && hologram.isAlive()) {
                    Vec3 playerPos = player.position();
                    double offsetY = player.getBbHeight() + 0.5;
                    
                    // Use teleport for instant position update
                    hologram.teleportTo(playerPos.x, playerPos.y + offsetY, playerPos.z);
                    
                    // Update the entity's motion to match the player's
                    hologram.setDeltaMovement(player.getDeltaMovement());
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
                TextDisplay hologram = OffRpCommand.activeHolograms.get(playerUUID);
                if (hologram != null) {
                    hologram.discard();
                }
                OffRpCommand.activeHolograms.remove(playerUUID);
            }
        }
    }
}