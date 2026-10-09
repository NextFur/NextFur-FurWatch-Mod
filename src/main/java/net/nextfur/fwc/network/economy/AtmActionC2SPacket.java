package net.nextfur.fwc.network.economy;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.economy.data.CheckData;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import net.nextfur.fwc.economy.db.DepositResult;
import net.nextfur.fwc.economy.db.EconomyDatabaseManager;
import net.nextfur.fwc.economy.items.CurrencyItem;
import net.nextfur.fwc.economy.items.CurrencyLayerBlockItem;
import net.nextfur.fwc.economy.items.SignedCheckItem;
import net.nextfur.fwc.economy.menu.AtmMenu;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.pda.data.PdaData;
import net.nextfur.fwc.pda.items.PdaItem;

import java.util.List;

public record AtmActionC2SPacket(int actionType, long amountCents) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "atm_action");
    public static final Type<AtmActionC2SPacket> TYPE = new Type<>(ID);

    public static final int ACTION_DEPOSIT_SLOT = 1;
    public static final int ACTION_DEPOSIT_ALL = 2;
    public static final int ACTION_WITHDRAW = 3;

    public static final StreamCodec<ByteBuf, AtmActionC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> {
                buf.writeInt(val.actionType);
                ByteBufCodecs.VAR_LONG.encode(buf, val.amountCents);
            },
            buf -> new AtmActionC2SPacket(buf.readInt(), ByteBufCodecs.VAR_LONG.decode(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AtmActionC2SPacket packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        ctx.enqueueWork(() -> {
            if (!(player.containerMenu instanceof AtmMenu menu)) return;

            ItemStack pdaStack = menu.getPdaStack();
            if (pdaStack.isEmpty() || !(pdaStack.getItem() instanceof PdaItem pdaItem)) {
                player.sendSystemMessage(Component.literal("§c[ATM] Por favor, insira um PDA no terminal para realizar operações!"));
                player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5f, 0.7f);
                return;
            }

            PdaData pdaData = pdaItem.getOrCreatePdaData(pdaStack, player);

            switch (packet.actionType()) {
                case ACTION_DEPOSIT_SLOT -> handleDepositSlot(player, menu, pdaStack, pdaData);
                case ACTION_DEPOSIT_ALL -> handleDepositAll(player, menu, pdaStack, pdaData);
                case ACTION_WITHDRAW -> handleWithdraw(player, menu, pdaStack, pdaData, packet.amountCents());
            }
        });
    }

    private static void handleDepositSlot(ServerPlayer player, AtmMenu menu, ItemStack pdaStack, PdaData pdaData) {
        Slot depositSlot = menu.getSlot(1);
        ItemStack slotStack = depositSlot.getItem();

        if (slotStack.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c[ATM] O compartimento de depósito está vazio!"));
            player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5f, 0.7f);
            return;
        }

        if (slotStack.getItem() instanceof CurrencyItem currencyItem) {
            long totalCents = currencyItem.getValueInCents() * slotStack.getCount();
            depositSlot.set(ItemStack.EMPTY);
            completeDeposit(player, menu, pdaStack, pdaData, totalCents, "cash deposit");
        } else if (slotStack.getItem() instanceof CurrencyLayerBlockItem layerItem) {
            long totalCents = layerItem.getValueInCents() * slotStack.getCount();
            depositSlot.set(ItemStack.EMPTY);
            completeDeposit(player, menu, pdaStack, pdaData, totalCents, "cash stack deposit");
        } else if (slotStack.getItem() instanceof SignedCheckItem) {
            CheckData checkData = slotStack.get(FwDataComponents.CHECK_DATA.get());
            if (checkData == null) {
                player.sendSystemMessage(Component.literal("§c[ATM] Cheque inválido ou danificado!"));
                return;
            }

            EconomyDatabaseManager.getInstance().depositCheck(checkData.checkId(), player.getUUID(), player.getName().getString())
                    .thenAccept(result -> {
                        if (result == DepositResult.SUCCESS) {
                            depositSlot.set(ItemStack.EMPTY);
                            completeDeposit(player, menu, pdaStack, pdaData, checkData.amountCents(), "check #" + checkData.checkId().toString().substring(0, 8));
                        } else if (result == DepositResult.ALREADY_DEPOSITED) {
                            player.sendSystemMessage(Component.literal("§c[ATM] Este cheque já consta como depositado no sistema bancário!"));
                        } else {
                            player.sendSystemMessage(Component.literal("§c[ATM] Erro ao processar cheque: registro não encontrado ou cancelado."));
                        }
                    });
        }
    }

    private static void handleDepositAll(ServerPlayer player, AtmMenu menu, ItemStack pdaStack, PdaData pdaData) {
        long totalCents = 0;

        // Slot 1 (ATM deposit slot)
        Slot depositSlot = menu.getSlot(1);
        ItemStack slotStack = depositSlot.getItem();
        if (!slotStack.isEmpty()) {
            if (slotStack.getItem() instanceof CurrencyItem currencyItem) {
                totalCents += currencyItem.getValueInCents() * slotStack.getCount();
                depositSlot.set(ItemStack.EMPTY);
            } else if (slotStack.getItem() instanceof CurrencyLayerBlockItem layerItem) {
                totalCents += layerItem.getValueInCents() * slotStack.getCount();
                depositSlot.set(ItemStack.EMPTY);
            }
        }

        // Slots 2 to 37 (Player inventory)
        for (int i = 2; i <= 37; i++) {
            Slot slot = menu.getSlot(i);
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof CurrencyItem currencyItem) {
                    totalCents += currencyItem.getValueInCents() * stack.getCount();
                    slot.set(ItemStack.EMPTY);
                } else if (stack.getItem() instanceof CurrencyLayerBlockItem layerItem) {
                    totalCents += layerItem.getValueInCents() * stack.getCount();
                    slot.set(ItemStack.EMPTY);
                }
            }
        }

        if (totalCents > 0) {
            completeDeposit(player, menu, pdaStack, pdaData, totalCents, "all inventory cash");
        } else {
            player.sendSystemMessage(Component.literal("§e[ATM] Nenhuma moeda ou cédula encontrada no inventário para depósito."));
        }
    }

    private static void completeDeposit(ServerPlayer player, AtmMenu menu, ItemStack pdaStack, PdaData pdaData, long amountCents, String detail) {
        long newBalance = pdaData.bankBalanceCents() + amountCents;
        PdaData updated = pdaData.withBankBalance(newBalance);
        pdaStack.set(FwDataComponents.PDA_DATA.get(), updated);
        menu.getSlot(0).setChanged();
        menu.broadcastChanges();

        EconomyDatabaseManager.getInstance().logTransaction(
                "ATM_DEPOSIT", player.getUUID(), player.getName().getString(),
                null, "Shield Bank", amountCents, newBalance,
                "Deposited " + detail + " into PDA virtual account"
        );

        player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7f, 1.2f);
        PacketDistributor.sendToPlayer(player, new AtmSyncS2CPacket(newBalance));
        player.sendSystemMessage(Component.literal("§a[ATM] Depósito realizado com sucesso: §f"
                + EconomyFormatHelper.formatFull(amountCents) + " §ano Shield Bank!"));
    }

    private static void handleWithdraw(ServerPlayer player, AtmMenu menu, ItemStack pdaStack, PdaData pdaData, long centsToWithdraw) {
        if (centsToWithdraw <= 0) return;

        if (pdaData.bankBalanceCents() < centsToWithdraw) {
            player.sendSystemMessage(Component.literal("§c[ATM] Saldo insuficiente no Shield Bank para realizar este saque!"));
            player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5f, 0.7f);
            return;
        }

        List<ItemStack> itemsToGive = WalletActionC2SPacket.calculateDenominations(centsToWithdraw);
        if (itemsToGive.isEmpty()) return;

        // Check inventory space (slots 2 to 37)
        int requiredSlots = itemsToGive.size();
        int emptySlots = 0;
        for (int i = 2; i <= 37; i++) {
            if (menu.getSlot(i).getItem().isEmpty()) emptySlots++;
        }

        if (emptySlots < requiredSlots) {
            player.sendSystemMessage(Component.literal("§c[ATM] Inventário cheio! Libere pelo menos " + requiredSlots + " espaços para as cédulas/moedas."));
            return;
        }

        long newBalance = pdaData.bankBalanceCents() - centsToWithdraw;
        PdaData updated = pdaData.withBankBalance(newBalance);
        pdaStack.set(FwDataComponents.PDA_DATA.get(), updated);
        menu.getSlot(0).setChanged();

        for (ItemStack item : itemsToGive) {
            player.getInventory().add(item);
        }
        menu.broadcastChanges();

        EconomyDatabaseManager.getInstance().logTransaction(
                "ATM_WITHDRAW", player.getUUID(), player.getName().getString(),
                null, "Shield Bank", centsToWithdraw, newBalance,
                "Withdrew " + itemsToGive.size() + " cash stacks from PDA virtual account"
        );

        player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, 1.5f);
        PacketDistributor.sendToPlayer(player, new AtmSyncS2CPacket(newBalance));
        player.sendSystemMessage(Component.literal("§e[ATM] Saque efetuado com sucesso: §f"
                + EconomyFormatHelper.formatFull(centsToWithdraw) + " §e(Shield Bank)"));
    }
}
