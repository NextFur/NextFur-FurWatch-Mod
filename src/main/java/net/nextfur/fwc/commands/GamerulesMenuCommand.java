package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.network.gui.OpenGamerulesMenuPacket;

public class GamerulesMenuCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gmenu")
                .executes(ctx -> {
                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                        if(player.hasPermissions(2)) openClientMenu(player);
                        else {
                            ctx.getSource().sendFailure(Component.literal("Você não tem permissão para usar este comando."));
                        }
                    } else {
                        ctx.getSource().sendFailure(Component.literal("Somente jogadores podem usar este comando."));
                    }
                    return 1;
                }));
    }

    private static void openClientMenu(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new OpenGamerulesMenuPacket());
    }
}