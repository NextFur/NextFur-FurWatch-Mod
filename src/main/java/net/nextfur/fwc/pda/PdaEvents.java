package net.nextfur.fwc.pda;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.pda.data.PdaContact;
import net.nextfur.fwc.pda.data.PdaData;
import net.nextfur.fwc.pda.items.PdaItem;

import java.util.UUID;

public class PdaEvents {
    public static void register() {
        NeoForge.EVENT_BUS.register(PdaEvents.class);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof Player targetPlayer) {
            ItemStack stack = event.getItemStack();
            if (stack.getItem() instanceof PdaItem pdaItem) {
                Player player = event.getEntity();
                if (!player.level().isClientSide()) {
                    PdaData data = pdaItem.getOrCreatePdaData(stack, player);
                    UUID targetUuid = targetPlayer.getUUID();
                    String targetName = targetPlayer.getName().getString();

                    if (data.hasContact(targetUuid)) {
                        player.displayClientMessage(
                                Component.literal("§8[§b📱 FurWatch PDA§8] §e" + targetName + " §7já está salvo na sua lista de contatos.")
                                        .withStyle(ChatFormatting.YELLOW),
                                true
                        );
                    } else {
                        PdaData updated = data.withContact(new PdaContact(targetUuid, targetName));
                        stack.set(FwDataComponents.PDA_DATA.get(), updated);
                        player.displayClientMessage(
                                Component.literal("§8[§b📱 FurWatch PDA§8] §aContato salvo: §f" + targetName + " §acom sucesso!")
                                        .withStyle(ChatFormatting.GREEN),
                                true
                        );
                        if (player instanceof ServerPlayer serverPlayer) {
                            serverPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1.0f, 1.6f);
                        }
                    }
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(player.level().isClientSide()));
            }
        }
    }
}
