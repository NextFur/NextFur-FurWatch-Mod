package net.nextfur.fwc.network.economy;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.economy.data.WalletData;
import net.nextfur.fwc.economy.db.EconomyDatabaseManager;
import net.nextfur.fwc.economy.items.WalletItem;
import net.nextfur.fwc.init.FwAttachments;
import net.nextfur.fwc.init.FwDataComponents;

public record EquipWalletSlotC2SPacket(int action) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "equip_wallet_slot");
    public static final Type<EquipWalletSlotC2SPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, EquipWalletSlotC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EquipWalletSlotC2SPacket::action,
            EquipWalletSlotC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EquipWalletSlotC2SPacket packet, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            ctx.enqueueWork(() -> {
                ItemStack slotWallet = player.getData(FwAttachments.WALLET_SLOT.get());
                ItemStack cursor = player.containerMenu.getCarried();

                if (packet.action() == 0) { // Click on slot
                    if (cursor.isEmpty() && !slotWallet.isEmpty()) {
                        // Take from slot to cursor
                        player.containerMenu.setCarried(slotWallet.copy());
                        player.setData(FwAttachments.WALLET_SLOT.get(), ItemStack.EMPTY);
                    } else if (cursor.getItem() instanceof WalletItem && slotWallet.isEmpty()) {
                        // Place into slot
                        ItemStack toEquip = cursor.split(1);
                        WalletItem.getOrCreateWalletData(toEquip, player);
                        player.setData(FwAttachments.WALLET_SLOT.get(), toEquip);
                    } else if (cursor.getItem() instanceof WalletItem && !slotWallet.isEmpty()) {
                        // Swap wallets
                        ItemStack toEquip = cursor.split(1);
                        WalletItem.getOrCreateWalletData(toEquip, player);
                        player.setData(FwAttachments.WALLET_SLOT.get(), toEquip);
                        player.containerMenu.setCarried(slotWallet);
                    }
                } else if (packet.action() == 1) { // Shift-click to unequip
                    if (!slotWallet.isEmpty()) {
                        if (player.getInventory().add(slotWallet)) {
                            player.setData(FwAttachments.WALLET_SLOT.get(), ItemStack.EMPTY);
                        }
                    }
                }

                ItemStack currentSlot = player.getData(FwAttachments.WALLET_SLOT.get());
                player.containerMenu.broadcastChanges();
                PacketDistributor.sendToPlayer(player, new SyncWalletSlotS2CPacket(currentSlot));

                // Update SQLite snapshot
                if (!currentSlot.isEmpty()) {
                    WalletData data = currentSlot.get(FwDataComponents.WALLET_DATA.get());
                    if (data != null) {
                        EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                                player.getUUID(), player.getName().getString(), data.walletId(), data.balanceCents()
                        );
                    }
                } else {
                    EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                            player.getUUID(), player.getName().getString(), null, 0L
                    );
                }
            });
        }
    }
}
