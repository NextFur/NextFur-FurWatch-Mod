package net.nextfur.fws;

import net.nextfur.fws.commands.*;
import net.nextfur.fws.listener.*;

import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//Lobby Manager inspired by AdvancedLobby by Cyne79

public final class FwMain extends JavaPlugin {

    private static FwMain instance;

    public static File file = new File("plugins/FWS", "config.yml");
    public static FileConfiguration config = YamlConfiguration.loadConfiguration(file);

    public static ArrayList<Player> build = new ArrayList<>();
    public static HashMap<Player, ItemStack[]> buildInventory = new HashMap<>();

    public static World lobbyWorld;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.createFiles();
        this.loadFiles();

        for (World world : Bukkit.getWorlds()) {
            if(FwMain.config.getString("lobby_world").contains(world.getName())) {
                lobbyWorld = world;
            }
        }

        this.prepareLobbyWorld();
        this.registerCommands();
        this.registerListeners();

    }

    private void prepareLobbyWorld() {
        for (World world : Bukkit.getWorlds()) {
            String weatherType = FwMain.config.getString("lobby_world_weather").toUpperCase();
            switch (weatherType) {
                case "CLEAR" -> world.setStorm(false);
                case "RAIN" -> world.setStorm(true);
                case "THUNDER" -> {
                    world.setStorm(true);
                    world.setThundering(true);
                }
                default -> world.setStorm(false);
            }
        }
    }

    private void registerCommands() {
        FwMain.getInstance().getCommand("lobby").setExecutor(new LobbyCommand());
        FwMain.getInstance().getCommand("build").setExecutor(new BuildCommand());
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), FwMain.getInstance());
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(), FwMain.getInstance());
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), FwMain.getInstance());
    }

    public void createFiles() {
        if(!FwMain.file.exists()) {
            FwMain.getInstance().getLogger().info("[FURSMP] Config not Found, creating...");
            FwMain.getInstance().saveResource("config.yml", false);
        }
    }

    public void loadFiles() {
        try {
            FwMain.config.load(FwMain.file);
            FwMain.getInstance().getLogger().info("[FURSMP] Config loaded successfully.");
        } catch (IOException | InvalidConfigurationException e) {
            FwMain.getInstance().getLogger().severe("[FURSMP] Could not load config.yml");
            e.printStackTrace();
        }
    }

    public void saveFile() {
        try {
            FwMain.config.save(FwMain.file);
            FwMain.getInstance().getLogger().info("[FURSMP] Config saved successfully.");
        } catch (IOException e) {
            FwMain.getInstance().getLogger().severe("[FURSMP] Could not save config.yml");
            e.printStackTrace();
        }
    }

    private static FwMain getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        FwMain.getInstance().getLogger().info("[FURSMP] Shutting down plugin");
    }
}
