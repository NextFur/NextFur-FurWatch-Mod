package net.nextfur.fws.commands;

import net.nextfur.fws.FwMain;
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
            sender.sendMessage("§cThis command is available for players only.");
            return true;
        }
        Player player = (Player) sender;

        if(FwMain.lobbyWorld.equals(player.getWorld())) {
            player.sendMessage("[FURSMP] Você já está neste mundo.");
            return true;
        }

        Location location = LocationManager.getLocation(FwMain.config.getString("spawn_location"));
        if(location != null) {
            player.teleport(location);
        }

        player.sendMessage("[FURSMP] Teleportando para o Lobby");
        return true;
    }
}