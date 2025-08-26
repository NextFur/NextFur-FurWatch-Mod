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

        if(FwMain.lobbyWorld == null) {
            FwMain.getInstance().getLogger().warning("[FURSMP PLUGIN] O Mundo do Lobby não está configurado ou não foi encontrado!");
            event.setCancelled(true);
            return;
        }

        if(player.getWorld().equals(FwMain.lobbyWorld)) {
            if(!FwMain.build.contains(player)) {
                event.setCancelled(true);
            }
        }
    }
}