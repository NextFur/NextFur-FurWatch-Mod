package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OffRpCommand {
    public static final Map<UUID, ArmorStand> activeHolograms = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("offrp")
                .executes(OffRpCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            UUID playerUUID = player.getUUID();

            if (activeHolograms.containsKey(playerUUID)) {
                ArmorStand existingHologram = activeHolograms.get(playerUUID);
                if (existingHologram != null) {
                    existingHologram.discard(); 
                }
                activeHolograms.remove(playerUUID);
                player.sendSystemMessage(Component.literal("[FURSMP] Você saiu do modo OFF RP!"));
            } else {
                ArmorStand hologram = new ArmorStand(player.level(),
                        player.getX(),
                        player.getY() + player.getBbHeight() + 0.5,
                        player.getZ()
                );

                CompoundTag nbt = new CompoundTag();
                hologram.saveWithoutId(nbt);
                nbt.putBoolean("Marker", true);
                hologram.load(nbt);

                hologram.setInvisible(true);
                hologram.setNoGravity(true);
                hologram.setCustomNameVisible(true);
                hologram.setCustomName(Component.literal("Off RP"));

                player.level().addFreshEntity(hologram);
                activeHolograms.put(playerUUID, hologram);

                player.sendSystemMessage(Component.literal("[FURSMP] Você está no modo OFF RP!"));
            }

            return 1;
        }
        return 0;
    }
}