package net.nextfur.fws.bukkit.lobby.events;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.nextfur.fws.bukkit.FurWatchBukkit;
import net.nextfur.fws.bukkit.utils.PositionParser;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LobbyEvents implements Listener {
    // Eventos executados apenas no lobby com config do lobby

    private YamlDocument config;
    private FurWatchBukkit plugin;

    public LobbyEvents(FurWatchBukkit plugin, YamlDocument config) {
        this.config = config;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (config.getBoolean("Lobby.Spawn-On-Join", false) && !config.getString("Lobby.SpawnPos", null).isEmpty()) {
            Location spawnPos = PositionParser.deserialize(config.getString("Lobby.SpawnPos"));
            event.getPlayer().teleport(spawnPos);
        }
    }
}
