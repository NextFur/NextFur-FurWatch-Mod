package net.nextfur.fwc.pda.service;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.pda.data.PdaContact;
import net.nextfur.fwc.pda.data.PdaData;
import net.nextfur.fwc.pda.data.PdaMessage;
import net.nextfur.fwc.pda.items.PdaItem;
import net.nextfur.fwc.pda.network.SyncPdaDataS2CPacket;

import java.util.UUID;

public class PdaMessagingService {

    public record PdaLookupResult(
            ItemStack stack,
            PdaData data,
            boolean foundPda,
            boolean hasOwner
    ) {
        public static final PdaLookupResult NOT_FOUND = new PdaLookupResult(ItemStack.EMPTY, null, false, false);
    }

    /**
     * Finds the sender's active PDA stack and data.
     * Priority:
     * 1. Handheld PDA (main hand, then offhand) if it has an owner.
     * 2. Inventory PDA owned by this player.
     * 3. Any inventory PDA that has an owner (e.g. grabbed from another player).
     * 4. Handheld or inventory PDA without an owner (to detect unlinked PDA).
     */
    public static PdaLookupResult findSenderPda(ServerPlayer player) {
        // 1. Check main hand
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHand.getItem() instanceof PdaItem) {
            PdaData data = mainHand.get(FwDataComponents.PDA_DATA.get());
            if (data != null && data.hasOwner()) {
                return new PdaLookupResult(mainHand, data, true, true);
            }
        }

        // 2. Check off hand
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offHand.getItem() instanceof PdaItem) {
            PdaData data = offHand.get(FwDataComponents.PDA_DATA.get());
            if (data != null && data.hasOwner()) {
                return new PdaLookupResult(offHand, data, true, true);
            }
        }

        ItemStack ownedPdaStack = null;
        PdaData ownedPdaData = null;

        ItemStack otherPdaStack = null;
        PdaData otherPdaData = null;

        ItemStack unlinkedPdaStack = null;
        PdaData unlinkedPdaData = null;

        // 3. Scan full inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof PdaItem) {
                PdaData data = stack.get(FwDataComponents.PDA_DATA.get());
                if (data != null && data.hasOwner()) {
                    if (data.ownerUuid().isPresent() && data.ownerUuid().get().equals(player.getUUID())) {
                        ownedPdaStack = stack;
                        ownedPdaData = data;
                        break; // Best match: player's own registered PDA
                    } else if (otherPdaStack == null) {
                        otherPdaStack = stack;
                        otherPdaData = data;
                    }
                } else if (unlinkedPdaStack == null) {
                    unlinkedPdaStack = stack;
                    unlinkedPdaData = data;
                }
            }
        }

        if (ownedPdaStack != null) {
            return new PdaLookupResult(ownedPdaStack, ownedPdaData, true, true);
        }
        if (otherPdaStack != null) {
            return new PdaLookupResult(otherPdaStack, otherPdaData, true, true);
        }
        if (unlinkedPdaStack != null) {
            return new PdaLookupResult(unlinkedPdaStack, unlinkedPdaData, true, false);
        }

        // Check if hand had unlinked PDA
        if (mainHand.getItem() instanceof PdaItem) {
            return new PdaLookupResult(mainHand, mainHand.get(FwDataComponents.PDA_DATA.get()), true, false);
        }
        if (offHand.getItem() instanceof PdaItem) {
            return new PdaLookupResult(offHand, offHand.get(FwDataComponents.PDA_DATA.get()), true, false);
        }

        return PdaLookupResult.NOT_FOUND;
    }

    /**
     * Finds the recipient's registered PDA stack and data in their inventory.
     * Priority:
     * 1. PDA owned by recipient.
     * 2. Any registered PDA in recipient's inventory.
     */
    public static PdaLookupResult findRecipientPda(ServerPlayer recipient) {
        ItemStack ownedPdaStack = null;
        PdaData ownedPdaData = null;

        ItemStack anyPdaStack = null;
        PdaData anyPdaData = null;

        for (int i = 0; i < recipient.getInventory().getContainerSize(); i++) {
            ItemStack stack = recipient.getInventory().getItem(i);
            if (stack.getItem() instanceof PdaItem) {
                PdaData data = stack.get(FwDataComponents.PDA_DATA.get());
                if (data != null && data.hasOwner()) {
                    if (data.ownerUuid().isPresent() && data.ownerUuid().get().equals(recipient.getUUID())) {
                        ownedPdaStack = stack;
                        ownedPdaData = data;
                        break;
                    } else if (anyPdaStack == null) {
                        anyPdaStack = stack;
                        anyPdaData = data;
                    }
                }
            }
        }

        if (ownedPdaStack != null) {
            return new PdaLookupResult(ownedPdaStack, ownedPdaData, true, true);
        }
        if (anyPdaStack != null) {
            return new PdaLookupResult(anyPdaStack, anyPdaData, true, true);
        }

        return PdaLookupResult.NOT_FOUND;
    }

    /**
     * Sends a message between two players using their respective PDAs.
     * The message is sent using the sender PDA's registered owner identity.
     */
    public static boolean sendPdaMessage(
            ServerPlayer senderPlayer,
            ItemStack senderStack,
            PdaData senderData,
            ServerPlayer recipientPlayer,
            ItemStack recipientStack,
            PdaData recipientData,
            String messageText
    ) {
        String senderName = senderData.hasOwner() ? senderData.ownerName() : senderPlayer.getName().getString();
        UUID senderUuid = senderData.hasOwner() ? senderData.ownerUuid().orElse(senderPlayer.getUUID()) : senderPlayer.getUUID();

        String recipientName = recipientPlayer.getName().getString();
        UUID recipientUuid = recipientPlayer.getUUID();

        // Verify signal blockers
        if (senderPlayer.hasEffect(net.nextfur.fwc.init.FwModEffects.SIGNAL_BLOCKED)) {
            senderPlayer.sendSystemMessage(Component.literal("§8[§b📱 FurWatch PDA§8] §c[SEM SINAL] O sinal do seu dispositivo está bloqueado por interferência eletromagnética!"));
            senderPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.PLAYERS, 1.0f, 0.8f);
            return false;
        }

        if (recipientPlayer.hasEffect(net.nextfur.fwc.init.FwModEffects.SIGNAL_BLOCKED)) {
            senderPlayer.sendSystemMessage(Component.literal("§8[§b📱 FurWatch PDA§8] §c[FALHA DE ALCANCE] Não foi possível entregar a mensagem. O sinal de " + recipientName + " está bloqueado por interferência!"));
            senderPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.PLAYERS, 1.0f, 0.8f);
            return false;
        }

        // 1. Deliver notification to recipient
        Component recipientMsg = Component.empty()
                .append(Component.literal("§3[§b📱 FurWatch PDA §8// §eDe: §b" + senderName + "§3] §f" + messageText));
        recipientPlayer.sendSystemMessage(recipientMsg);
        recipientPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, 1.8f);

        // 2. Deliver confirmation to sender
        Component senderMsg = Component.empty()
                .append(Component.literal("§3[§b📱 FurWatch PDA §8// §7Para: §b" + recipientName + "§3] §f" + messageText));
        senderPlayer.sendSystemMessage(senderMsg);
        senderPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.6f, 1.5f);

        // 3. Record message
        PdaMessage msgRecord = new PdaMessage(
                UUID.randomUUID(),
                senderUuid,
                senderName,
                recipientUuid,
                recipientName,
                messageText,
                System.currentTimeMillis()
        );

        // 4. Update sender PDA
        PdaData updatedSenderData = senderData.withMessage(msgRecord);
        if (!updatedSenderData.hasContact(recipientUuid)) {
            updatedSenderData = updatedSenderData.withContact(new PdaContact(recipientUuid, recipientName));
        }
        senderStack.set(FwDataComponents.PDA_DATA.get(), updatedSenderData);
        senderPlayer.inventoryMenu.broadcastChanges();
        PacketDistributor.sendToPlayer(senderPlayer, new SyncPdaDataS2CPacket(updatedSenderData));

        // 5. Update recipient PDA
        PdaData updatedRecipientData = recipientData.withMessage(msgRecord);
        if (!updatedRecipientData.hasContact(senderUuid)) {
            updatedRecipientData = updatedRecipientData.withContact(new PdaContact(senderUuid, senderName));
        }
        recipientStack.set(FwDataComponents.PDA_DATA.get(), updatedRecipientData);
        recipientPlayer.inventoryMenu.broadcastChanges();
        PacketDistributor.sendToPlayer(recipientPlayer, new SyncPdaDataS2CPacket(updatedRecipientData));

        return true;
    }
}
