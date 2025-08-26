package net.nextfur.fws.commands;

//import net.nextfur.fws.FwMain;
import net.nextfur.fws.misc.LocationManager;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String Label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("[FURSMP] §cEste comando só pode ser usado por jogadores dentro do jogo.");
            return true;
        }
        Player player = (Player) sender;

        Location spawnLocation = LocationManager.getLocation("spawn");
        if (spawnLocation == null) {
            player.sendMessage("§f[§dFURSMP§f] §cSpawn location not set in config.yml. Please set it using /setspawn.");
            return false;
        }

        player.sendMessage("§f[§dFURSMP§f] §7Teleportando para o Lobby..");
        player.teleport(spawnLocation);
        return true;
    }
}