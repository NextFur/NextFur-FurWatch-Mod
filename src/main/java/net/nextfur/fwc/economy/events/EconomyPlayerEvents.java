package net.nextfur.fwc.economy.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.economy.data.WalletData;
import net.nextfur.fwc.economy.db.EconomyDatabaseManager;
import net.nextfur.fwc.init.FwAttachments;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.network.economy.SyncWalletSlotS2CPacket;

@EventBusSubscriber(modid = FwMain.MODID)
public class EconomyPlayerEvents {

    @SubscribeEvent
    public static void onPlayerDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack wallet = player.getData(FwAttachments.WALLET_SLOT.get());
            if (!wallet.isEmpty()) {
                // Drop the wallet item entity at player location
                ItemEntity drop = new ItemEntity(
                        player.level(),
                        player.getX(), player.getY(), player.getZ(),
                        wallet.copy()
                );
                drop.setDefaultPickUpDelay();
                event.getDrops().add(drop);

                // Clear the dedicated slot
                player.setData(FwAttachments.WALLET_SLOT.get(), ItemStack.EMPTY);
                PacketDistributor.sendToPlayer(player, new SyncWalletSlotS2CPacket(ItemStack.EMPTY));

                // Log to SQLite and clear snapshot
                WalletData data = wallet.get(FwDataComponents.WALLET_DATA.get());
                long balance = data != null ? data.balanceCents() : 0L;
                EconomyDatabaseManager.getInstance().logTransaction(
                        "WALLET_DROPPED_ON_DEATH",
                        player.getUUID(), player.getName().getString(),
                        null, null,
                        balance, 0L,
                        "Wallet dropped on player death at " + (int) player.getX() + ", " + (int) player.getY() + ", " + (int) player.getZ()
                );
                EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                        player.getUUID(), player.getName().getString(), null, 0L
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() && event.getEntity() instanceof ServerPlayer newPlayer) {
            // Keep wallet when returning from the End dimension
            ItemStack originalWallet = event.getOriginal().getData(FwAttachments.WALLET_SLOT.get());
            if (!originalWallet.isEmpty()) {
                newPlayer.setData(FwAttachments.WALLET_SLOT.get(), originalWallet.copy());
                PacketDistributor.sendToPlayer(newPlayer, new SyncWalletSlotS2CPacket(originalWallet));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack wallet = player.getData(FwAttachments.WALLET_SLOT.get());
            PacketDistributor.sendToPlayer(player, new SyncWalletSlotS2CPacket(wallet));

            if (!wallet.isEmpty()) {
                WalletData data = wallet.get(FwDataComponents.WALLET_DATA.get());
                if (data != null) {
                    EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                            player.getUUID(), player.getName().getString(), data.walletId(), data.balanceCents()
                    );
                }
            }
        }
    }
}
