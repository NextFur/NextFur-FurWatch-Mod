package net.nextfur.fwc.network.economy;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.economy.currency.CurrencyUnit;
import net.nextfur.fwc.economy.data.CheckData;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import net.nextfur.fwc.economy.data.WalletData;
import net.nextfur.fwc.economy.db.DepositResult;
import net.nextfur.fwc.economy.db.EconomyDatabaseManager;
import net.nextfur.fwc.economy.items.CurrencyItem;
import net.nextfur.fwc.economy.items.CurrencyLayerBlockItem;
import net.nextfur.fwc.economy.items.SignedCheckItem;
import net.nextfur.fwc.economy.menu.WalletMenu;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.init.FwModItems;

import java.util.*;

public record WalletActionC2SPacket(int actionType, long amountCents, String payee) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "wallet_action");
    public static final Type<WalletActionC2SPacket> TYPE = new Type<>(ID);

    public static final int ACTION_DEPOSIT_ALL = 0;
    public static final int ACTION_DEPOSIT_SLOT = 1;
    public static final int ACTION_WITHDRAW_CENTS = 2;
    public static final int ACTION_WRITE_CHECK = 3;

    public static final StreamCodec<ByteBuf, WalletActionC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> {
                buf.writeInt(val.actionType);
                buf.writeLong(val.amountCents);
                ByteBufCodecs.STRING_UTF8.encode(buf, val.payee != null ? val.payee : "");
            },
            buf -> new WalletActionC2SPacket(buf.readInt(), buf.readLong(), ByteBufCodecs.STRING_UTF8.decode(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WalletActionC2SPacket packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        ctx.enqueueWork(() -> {
            if (!(player.containerMenu instanceof WalletMenu menu)) return;

            ItemStack wallet = menu.getWalletStack();
            if (wallet.isEmpty()) return;

            WalletData data = wallet.get(FwDataComponents.WALLET_DATA.get());
            if (data == null) {
                data = WalletData.createNew(player.getUUID(), player.getName().getString());
            }

            switch (packet.actionType()) {
                case ACTION_DEPOSIT_ALL -> handleDepositAll(player, menu, wallet, data);
                case ACTION_DEPOSIT_SLOT -> handleDepositSlot(player, menu, wallet, data);
                case ACTION_WITHDRAW_CENTS -> handleWithdraw(player, menu, wallet, data, packet.amountCents());
                case ACTION_WRITE_CHECK -> handleWriteCheck(player, menu, wallet, data, packet.amountCents(), packet.payee());
            }
        });
    }

    private static void handleDepositAll(ServerPlayer player, WalletMenu menu, ItemStack wallet, WalletData data) {
        long totalCents = 0;
        int itemsDeposited = 0;

        // Iterate over player inventory slots in menu (slots 1 to 36)
        for (int i = 1; i <= 36; i++) {
            Slot slot = menu.getSlot(i);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof CurrencyItem currencyItem) {
                    long itemVal = currencyItem.getValueInCents() * stack.getCount();
                    totalCents += itemVal;
                    itemsDeposited += stack.getCount();
                    slot.set(ItemStack.EMPTY);
                } else if (stack.getItem() instanceof CurrencyLayerBlockItem layerItem) {
                    long itemVal = layerItem.getValueInCents() * stack.getCount();
                    totalCents += itemVal;
                    itemsDeposited += stack.getCount();
                    slot.set(ItemStack.EMPTY);
                }
            }
        }

        if (totalCents > 0) {
            long newBalance = data.balanceCents() + totalCents;
            WalletData updated = data.withBalance(newBalance);
            wallet.set(FwDataComponents.WALLET_DATA.get(), updated);
            menu.setWalletStack(wallet);
            menu.broadcastChanges();

            EconomyDatabaseManager.getInstance().logTransaction(
                    "DEPOSIT_CASH", player.getUUID(), player.getName().getString(),
                    null, null, totalCents, newBalance,
                    "Deposit All: " + itemsDeposited + " currency items/layers"
            );
            EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                    player.getUUID(), player.getName().getString(), updated.walletId(), newBalance
            );

            PacketDistributor.sendToPlayer(player, new WalletSyncS2CPacket(updated));
            player.sendSystemMessage(Component.literal("§aDepositado com sucesso: §f" + EconomyFormatHelper.formatFull(totalCents)));
        } else {
            player.sendSystemMessage(Component.literal("§cNenhuma moeda ou cédula encontrada no inventário para depositar."));
        }
    }

    private static void handleDepositSlot(ServerPlayer player, WalletMenu menu, ItemStack wallet, WalletData data) {
        Slot depositSlot = menu.getSlot(0);
        ItemStack stack = depositSlot.getItem();
        if (stack.isEmpty()) return;

        if (stack.getItem() instanceof CurrencyItem currencyItem) {
            long totalCents = currencyItem.getValueInCents() * stack.getCount();
            long newBalance = data.balanceCents() + totalCents;
            WalletData updated = data.withBalance(newBalance);
            wallet.set(FwDataComponents.WALLET_DATA.get(), updated);
            menu.setWalletStack(wallet);
            depositSlot.set(ItemStack.EMPTY);
            menu.broadcastChanges();

            EconomyDatabaseManager.getInstance().logTransaction(
                    "DEPOSIT_CASH", player.getUUID(), player.getName().getString(),
                    null, null, totalCents, newBalance,
                    "Deposit Slot: " + currencyItem.getUnit().getLabel() + " x" + stack.getCount()
            );
            EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                    player.getUUID(), player.getName().getString(), updated.walletId(), newBalance
            );

            PacketDistributor.sendToPlayer(player, new WalletSyncS2CPacket(updated));
            player.sendSystemMessage(Component.literal("§aDepositado com sucesso: §f" + EconomyFormatHelper.formatFull(totalCents)));
        } else if (stack.getItem() instanceof CurrencyLayerBlockItem layerItem) {
            long totalCents = layerItem.getValueInCents() * stack.getCount();
            long newBalance = data.balanceCents() + totalCents;
            WalletData updated = data.withBalance(newBalance);
            wallet.set(FwDataComponents.WALLET_DATA.get(), updated);
            menu.setWalletStack(wallet);
            depositSlot.set(ItemStack.EMPTY);
            menu.broadcastChanges();

            EconomyDatabaseManager.getInstance().logTransaction(
                    "DEPOSIT_CASH", player.getUUID(), player.getName().getString(),
                    null, null, totalCents, newBalance,
                    "Deposit Slot Layer: " + layerItem.getUnit().getLabel() + " layer x" + stack.getCount()
            );
            EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                    player.getUUID(), player.getName().getString(), updated.walletId(), newBalance
            );

            PacketDistributor.sendToPlayer(player, new WalletSyncS2CPacket(updated));
            player.sendSystemMessage(Component.literal("§aDepositado com sucesso: §f" + EconomyFormatHelper.formatFull(totalCents)));
        } else if (stack.getItem() instanceof SignedCheckItem) {
            CheckData checkData = stack.get(FwDataComponents.CHECK_DATA.get());
            if (checkData == null || checkData.deposited()) {
                player.sendSystemMessage(Component.literal("§cEste cheque já foi compensado e não possui mais valor!"));
                return;
            }

            EconomyDatabaseManager.getInstance().depositCheck(checkData.checkId(), player.getUUID(), player.getName().getString())
                    .thenAccept(result -> {
                        if (result == DepositResult.SUCCESS) {
                            player.server.execute(() -> {
                                long newBalance = data.balanceCents() + checkData.amountCents();
                                WalletData updated = data.withBalance(newBalance);
                                wallet.set(FwDataComponents.WALLET_DATA.get(), updated);
                                menu.setWalletStack(wallet);
                                depositSlot.set(ItemStack.EMPTY);
                                menu.broadcastChanges();

                                EconomyDatabaseManager.getInstance().logTransaction(
                                        "CHECK_DEPOSITED", player.getUUID(), player.getName().getString(),
                                        checkData.issuerUuid(), checkData.issuerName(),
                                        checkData.amountCents(), newBalance,
                                        "Check #" + checkData.checkId().toString().substring(0, 8) + " cashed"
                                );
                                EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                                        player.getUUID(), player.getName().getString(), updated.walletId(), newBalance
                                );

                                PacketDistributor.sendToPlayer(player, new WalletSyncS2CPacket(updated));
                                player.sendSystemMessage(Component.literal("§aCheque de §f" + EconomyFormatHelper.formatFull(checkData.amountCents()) + "§a compensado com sucesso!"));
                            });
                        } else if (result == DepositResult.ALREADY_DEPOSITED) {
                            player.sendSystemMessage(Component.literal("§cEste cheque já consta como depositado no sistema bancário!"));
                        } else {
                            player.sendSystemMessage(Component.literal("§cErro ao processar cheque: registro não encontrado ou cancelado."));
                        }
                    });
        }
    }

    private static void handleWithdraw(ServerPlayer player, WalletMenu menu, ItemStack wallet, WalletData data, long centsToWithdraw) {
        if (centsToWithdraw <= 0) return;
        if (data.balanceCents() < centsToWithdraw) {
            player.sendSystemMessage(Component.literal("§cSaldo insuficiente na carteira para este saque!"));
            return;
        }

        List<ItemStack> itemsToGive = calculateDenominations(centsToWithdraw);
        if (itemsToGive.isEmpty()) return;

        // Check inventory space
        int requiredSlots = itemsToGive.size();
        int emptySlots = 0;
        for (int i = 1; i <= 36; i++) {
            if (menu.getSlot(i).getItem().isEmpty()) emptySlots++;
        }

        if (emptySlots < requiredSlots) {
            player.sendSystemMessage(Component.literal("§cInventário cheio! Libere pelo menos " + requiredSlots + " espaços para o saque."));
            return;
        }

        // Deduct balance and grant items
        long newBalance = data.balanceCents() - centsToWithdraw;
        WalletData updated = data.withBalance(newBalance);
        wallet.set(FwDataComponents.WALLET_DATA.get(), updated);
        menu.setWalletStack(wallet);

        for (ItemStack item : itemsToGive) {
            player.getInventory().add(item);
        }
        menu.broadcastChanges();

        EconomyDatabaseManager.getInstance().logTransaction(
                "WITHDRAW_CASH", player.getUUID(), player.getName().getString(),
                null, null, centsToWithdraw, newBalance,
                "Withdrew " + itemsToGive.size() + " stacks of cash"
        );
        EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                player.getUUID(), player.getName().getString(), updated.walletId(), newBalance
        );

        PacketDistributor.sendToPlayer(player, new WalletSyncS2CPacket(updated));
        player.sendSystemMessage(Component.literal("§eSaque efetuado com sucesso: §f" + EconomyFormatHelper.formatFull(centsToWithdraw)));
    }

    private static void handleWriteCheck(ServerPlayer player, WalletMenu menu, ItemStack wallet, WalletData data, long amountCents, String payee) {
        if (amountCents <= 0) {
            player.sendSystemMessage(Component.literal("§cO valor do cheque deve ser maior que zero!"));
            return;
        }
        if (data.balanceCents() < amountCents) {
            player.sendSystemMessage(Component.literal("§cSaldo insuficiente para emitir este cheque!"));
            return;
        }

        // Check if player has a blank check
        boolean foundBlank = false;
        for (int i = 1; i <= 36; i++) {
            Slot slot = menu.getSlot(i);
            ItemStack st = slot.getItem();
            if (!st.isEmpty() && st.is(FwModItems.BLANK_CHECK.get())) {
                st.shrink(1);
                foundBlank = true;
                break;
            }
        }

        if (!foundBlank) {
            Slot depositSlot = menu.getSlot(0);
            if (!depositSlot.getItem().isEmpty() && depositSlot.getItem().is(FwModItems.BLANK_CHECK.get())) {
                depositSlot.getItem().shrink(1);
                foundBlank = true;
            }
        }

        if (!foundBlank) {
            player.sendSystemMessage(Component.literal("§cVocê precisa de uma Folha de Cheque em Branco (blank_check) no inventário para emitir um cheque!"));
            return;
        }

        // Deduct amount
        long newBalance = data.balanceCents() - amountCents;
        WalletData updated = data.withBalance(newBalance);
        wallet.set(FwDataComponents.WALLET_DATA.get(), updated);
        menu.setWalletStack(wallet);

        // Generate signed check
        UUID checkId = UUID.randomUUID();
        String validPayee = (payee == null || payee.isBlank()) ? "Portador" : payee.trim();
        CheckData checkData = new CheckData(checkId, player.getUUID(), player.getName().getString(), validPayee, amountCents, System.currentTimeMillis(), false);

        ItemStack signedCheck = new ItemStack(FwModItems.SIGNED_CHECK.get());
        signedCheck.set(FwDataComponents.CHECK_DATA.get(), checkData);

        if (!player.getInventory().add(signedCheck)) {
            player.drop(signedCheck, false);
        }
        menu.broadcastChanges();

        EconomyDatabaseManager.getInstance().registerCheck(checkId, player.getUUID(), player.getName().getString(), validPayee, amountCents);
        EconomyDatabaseManager.getInstance().logTransaction(
                "CHECK_ISSUED", player.getUUID(), player.getName().getString(),
                null, validPayee, amountCents, newBalance,
                "Issued check #" + checkId.toString().substring(0, 8) + " to " + validPayee
        );
        EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                player.getUUID(), player.getName().getString(), updated.walletId(), newBalance
        );

        PacketDistributor.sendToPlayer(player, new WalletSyncS2CPacket(updated));
        player.sendSystemMessage(Component.literal("§aCheque emitido com sucesso! Valor: §f" + EconomyFormatHelper.formatFull(amountCents) + " §7(Para: " + validPayee + ")"));
    }

    private static List<ItemStack> calculateDenominations(long cents) {
        List<ItemStack> list = new ArrayList<>();
        long remaining = cents;

        CurrencyUnit[] units = new CurrencyUnit[] {
                CurrencyUnit.BILL_200M, CurrencyUnit.BILL_100M, CurrencyUnit.BILL_50M,
                CurrencyUnit.BILL_20M, CurrencyUnit.BILL_10M, CurrencyUnit.BILL_5M,
                CurrencyUnit.COIN_2M, CurrencyUnit.COIN_1M,
                CurrencyUnit.COIN_50C, CurrencyUnit.COIN_25C, CurrencyUnit.COIN_20C,
                CurrencyUnit.COIN_5C, CurrencyUnit.COIN_1C
        };

        for (CurrencyUnit u : units) {
            long val = u.getValueInCents();
            long count = remaining / val;
            if (count > 0) {
                remaining %= val;
                ItemStack baseItem = getItemForUnit(u);
                while (count > 0) {
                    int stackCount = (int) Math.min(count, 64);
                    ItemStack stack = baseItem.copy();
                    stack.setCount(stackCount);
                    list.add(stack);
                    count -= stackCount;
                }
            }
        }
        return list;
    }

    private static ItemStack getItemForUnit(CurrencyUnit u) {
        return switch (u) {
            case COIN_1C -> new ItemStack(FwModItems.COIN_1C.get());
            case COIN_5C -> new ItemStack(FwModItems.COIN_5C.get());
            case COIN_20C -> new ItemStack(FwModItems.COIN_20C.get());
            case COIN_25C -> new ItemStack(FwModItems.COIN_25C.get());
            case COIN_50C -> new ItemStack(FwModItems.COIN_50C.get());
            case COIN_1M -> new ItemStack(FwModItems.COIN_1M.get());
            case COIN_2M -> new ItemStack(FwModItems.COIN_2M.get());
            case BILL_5M -> new ItemStack(FwModItems.BILL_5M.get());
            case BILL_10M -> new ItemStack(FwModItems.BILL_10M.get());
            case BILL_20M -> new ItemStack(FwModItems.BILL_20M.get());
            case BILL_50M -> new ItemStack(FwModItems.BILL_50M.get());
            case BILL_100M -> new ItemStack(FwModItems.BILL_100M.get());
            case BILL_200M -> new ItemStack(FwModItems.BILL_200M.get());
        };
    }
}
