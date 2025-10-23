package net.nextfur.fwc.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class BlockClientInteractions {
    private static boolean isNotAuthenticated(Player player) {
        return player instanceof ServerPlayer sp && !ServerAuthManager.isAuthenticated(sp.getUUID());
    }

    // Bloqueia movimentação
    @SubscribeEvent
    public void onPlayerMove(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (isNotAuthenticated(player)) {
            player.setDeltaMovement(0, 0, 0);
            player.teleportTo(player.getX(), player.getY(), player.getZ());
        }
    }

    // Bloqueia colocar blocos ou usar itens
    @SubscribeEvent
    public void onUse(PlayerInteractEvent.RightClickItem event) {
        if (isNotAuthenticated(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // Bloqueia RCLICK em blocos
    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (isNotAuthenticated(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onItemPickup(ItemEntityPickupEvent.Pre event) {
        if (isNotAuthenticated(event.getPlayer())) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    // Bloqueia quebrar blocos
    @SubscribeEvent
    public void onBlockBreak(PlayerEvent.BreakSpeed event) {
        if (isNotAuthenticated(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // Bloqueia interação com entidades (ex: villagers, mobs)
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isNotAuthenticated(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    // Bloqueia chat
    @SubscribeEvent
    public void onChatMessage(ServerChatEvent event) {
        if (isNotAuthenticated(event.getPlayer())) {
            event.setCanceled(true);
        }
    }
}
