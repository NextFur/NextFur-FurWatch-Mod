package net.nextfur.fws.bukkit;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.nextfur.fws.api.NextFurAPI;
import net.nextfur.fws.bukkit.common.commands.CommonCommands;
import net.nextfur.fws.bukkit.common.events.CommonEvents;
import net.nextfur.fws.bukkit.generic.commands.GenericCommands;
import net.nextfur.fws.bukkit.generic.events.GenericEvents;
import net.nextfur.fws.bukkit.lobby.commands.LobbyCommands;
import net.nextfur.fws.bukkit.lobby.events.LobbyEvents;
import net.nextfur.fws.bukkit.utils.Logger;

public class FurWatchBukkit extends JavaPlugin implements PluginMessageListener {
    private static final String CHANNEL = "furwatch:main";

    public FurWatchBukkit plugin;
    public Logger LOGGER = new Logger(ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "FurWatch" + ChatColor.DARK_GRAY + "] ");

    private String server = "generic";
    private YamlDocument config;

    private List<String> bannedItems = new ArrayList<>();
    public NextFurAPI api;

    @Override
    public void onEnable() {
        plugin = this;
        LOGGER.info("Inicializando plugin...");

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, CHANNEL);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, CHANNEL, this);

        Bukkit.getScheduler().runTaskLater(this, this::requestRole, 20L);

        LOGGER.info("Inicializado!");
    }

    @Override
    public void onDisable() {

    }

    private void requestRole() {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("whoami");
            getServer().sendPluginMessage(this, CHANNEL, out.toByteArray());
            LOGGER.info("Iniciando ponte com velocity...");
        } catch (Exception err) {
            LOGGER.info("Falha ao estabelecer conexão com velocity! Desligando plugin...");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if(!channel.equals(CHANNEL)) return;
        if(this.server != null) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String role = in.readUTF();

        initialize(role);
    }

    private void initialize(String role) {
        this.server = role;

        try {
            String configFileName = role.equals("lobby") ? "lobby_config.yml" : "bukkit_config.yml";

            this.config = YamlDocument.create(
                    new File(getDataFolder(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/"+configFileName)),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("Config-Version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                            .build()
            );

            this.api = new NextFurAPI(config.getString("FurGuard.ApiUrl"), config.getString("FurGuard.ApiKey"));
            LOGGER.info("Config carregada com sucesso!");

            syncBannedItems();
        } catch (Exception err) {
            LOGGER.error("Erro ao carregar config!");
            err.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        new CommonCommands(this, this.config);
        new CommonEvents(this, config);

        if(role.equals("lobby")) {
            new LobbyCommands(this, this.config);
            getServer().getPluginManager().registerEvents(new LobbyEvents(this, config), this);
        } else {
            new GenericCommands(this, this.config);
            getServer().getPluginManager().registerEvents(new GenericEvents(this, config), this);
        }
    }

    private void syncBannedItems() {
        LOGGER.info("Sincronizando lista de itens banidos com a API NextFur...");
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                @SuppressWarnings("unchecked")
                List<String> apiItems = (List<String>) api.get("banitem", null);
                if (apiItems == null) return;

                this.setBannedItems(apiItems);
            } catch (Exception ignored) {}
        });
    }

    public List<String> getBannedItems() {
        return this.bannedItems;
    }

    public void setBannedItems(List<String> items) {
        this.bannedItems = items;
    }

    public NextFurAPI getApi() {
        return this.api;
    }
}
