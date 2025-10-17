package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Display.TextDisplay;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OffRpCommand {
    public static final Map<UUID, TextDisplay> activeHolograms = new HashMap<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("offrp")
                .executes(OffRpCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getEntity() instanceof Player player) {
            UUID playerUUID = player.getUUID();

            if (activeHolograms.containsKey(playerUUID)) {
                TextDisplay existingHologram = activeHolograms.get(playerUUID);
                if (existingHologram != null) {
                    existingHologram.discard(); 
                }
                activeHolograms.remove(playerUUID);
                player.sendSystemMessage(Component.literal("[FURSMP] Você saiu do modo OFF RP!"));
            } else {
                TextDisplay hologram = new TextDisplay(EntityType.TEXT_DISPLAY, player.level());
                
                CompoundTag nbt = new CompoundTag();
                hologram.saveWithoutId(nbt);
                
                nbt.putString("text", Component.Serializer.toJson(Component.literal("Off RP"), context.getSource().registryAccess()));
                nbt.putInt("background", 0x40000000); // Semi-transparent background
                nbt.putString("billboard", "center");
                nbt.putByte("text_opacity", (byte) 255);
                nbt.putInt("line_width", 200);
                nbt.putBoolean("see_through", true);
                
                hologram.load(nbt);
                
                hologram.setPos(player.getX(), player.getY() + player.getBbHeight() + 0.5, player.getZ());
                hologram.setNoGravity(true);
                
                player.level().addFreshEntity(hologram);
                activeHolograms.put(playerUUID, hologram);

                player.sendSystemMessage(Component.literal("[FURSMP] Você está no modo OFF RP!"));
            }

            return 1;
        }
        return 0;
    }
}