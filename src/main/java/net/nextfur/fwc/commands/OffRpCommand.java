package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Display.TextDisplay;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.network.world.OffRpSyncPacket;

import java.util.*;

public class OffRpCommand {
    public static final List<UUID> activeHolograms = new ArrayList<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("offrp")
                .executes((context) -> {
                    execute(context);
                    return 1;
                }));
    }

    private static void execute(CommandContext<CommandSourceStack> context) {
        if(!context.getSource().isPlayer()) return;

        ServerPlayer player = context.getSource().getPlayer();
        UUID playerUUID = player.getUUID();

        if (activeHolograms.contains(playerUUID)) {
            activeHolograms.remove(playerUUID);
            player.sendSystemMessage(Component.literal("[FURSMP] | Voce saiu do modo OFFRP"));
        } else {
            activeHolograms.add(playerUUID);
            player.sendSystemMessage(Component.literal("[FURSMP] | Voce entrou no modo OFFRP"));
        }

        for(ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
            PacketDistributor.sendToPlayer(p, new OffRpSyncPacket(activeHolograms));
        }
    }
}