package net.nextfur.fws.commands;

import net.nextfur.fws.FwMain;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String Label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("[FURSMP] This command is availiable for player only");
            return true;
        }
        Player player = (Player) sender;
        if(player.hasPermission("fws.commands.build")) {
            if(!FwMain.build.contains(player)) {
                FwMain.build.add(player);

                player.sendMessage("[FURSMP] Você está no modo de Building");

                player.setGameMode(GameMode.CREATIVE);

                FwMain.buildInventory.put(player, player.getInventory().getContents());
                player.getInventory().clear();
                return true;
            }
            FwMain.build.remove(player);
            player.sendMessage("[FURSMP] Você saiu do modo de building");

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

            player.getInventory();
            player.getInventory().setContents(FwMain.buildInventory.get(player));
            return true;
        }
        player.sendMessage("[FURSMP] Você não tem permissão para executar este comando!");
        return true;
    }
}