package net.nextfur.fws.bukkit.common.events;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.nextfur.fws.bukkit.FurWatchBukkit;
import org.bukkit.Bukkit;

public class CommonEvents {
    private final FurWatchBukkit plugin;
    private final YamlDocument config;

    public CommonEvents (FurWatchBukkit plugin, YamlDocument config) {
        this.config = config;
        this.plugin = plugin;

        Bukkit.getServer().getPluginManager().registerEvents(new BannedItemsListener(plugin, config), plugin);
    }
}
