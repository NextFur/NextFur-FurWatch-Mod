package net.nextfur.fws.listener;

import net.nextfur.fws.FwMain;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class PlayerHungerListener implements Listener {
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            if(FwMain.lobbyWorld == null) {
                FwMain.getInstance().getLogger().warning("[FURSMP PLUGIN] O Mundo do Lobby não está configurado ou não foi encontrado!");
                event.setCancelled(true);
                return;
            }

            if(player.getWorld().equals(FwMain.lobbyWorld)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}