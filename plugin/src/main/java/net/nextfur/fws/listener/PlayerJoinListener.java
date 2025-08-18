package net.nextfur.fws.listener;

import net.nextfur.fws.FwMain;
import net.nextfur.fws.misc.LocationManager;

//import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.setJoinMessage(null);

        double health = FwMain.config.getDouble("default_health");
        player.setHealth(health);

        player.setFoodLevel(20);

        player.setAllowFlight(false);
        player.setFlying(false);

        player.setFireTicks(0);

        player.setLevel(0);
        player.setExp(0);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        GameMode gameMode = GameMode.valueOf(FwMain.config.getString("default_gamemode"));
        
        switch (gameMode) {
            case SURVIVAL:
                gameMode = GameMode.SURVIVAL;
                break;
            case CREATIVE:
                gameMode = GameMode.CREATIVE;
                break;
            case ADVENTURE:
                gameMode = GameMode.ADVENTURE;
                break;
            case SPECTATOR:
                gameMode = GameMode.SPECTATOR;
                break;
            default:
                player.sendMessage("[FURSMP] §cInvalid game mode set in config.yml. Defaulting to SURVIVAL.");
                gameMode = GameMode.ADVENTURE;
                break;
        }

        player.setGameMode(gameMode);

        Location spawnLocation = LocationManager.getLocation("spawn");
        if (spawnLocation == null) {
            player.sendMessage("[FURSMP] §cSpawn location not set in config.yml. Please set it using /setspawn.");
            return;
        }

        player.teleport(spawnLocation);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
    }
}