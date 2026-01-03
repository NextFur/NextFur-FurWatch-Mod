package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.network.gui.OpenSkyColorMenuPacket;

public class SkyColorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("skycolor")
                .requires(e -> e.hasPermission(2))
                .executes(ctx -> {
                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                        openClientMenu(player);
                    } else {
                        ctx.getSource().sendFailure(Component.literal("Somente jogadores podem usar este comando."));
                    }
                    return 1;
                }));
    }

    private static void openClientMenu(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new OpenSkyColorMenuPacket());
    }
}
