package net.nextfur.fws.listener;

import net.nextfur.fws.FwMain;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockPlaceListener implements Listener {
    @EventHandler
    public void onBlockPlace(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(!FwMain.lobbyWorld.equals(player.getWorld().getName())) {
            event.setCancelled(true);
            return;
        }
    }
}