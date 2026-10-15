package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import net.nextfur.fwc.economy.data.WalletData;
import net.nextfur.fwc.economy.db.EconomyDatabaseManager;
import net.nextfur.fwc.economy.items.WalletItem;
import net.nextfur.fwc.economy.menu.WalletMenu;
import net.nextfur.fwc.init.FwAttachments;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.init.FwPermissions;
import net.nextfur.fwc.network.economy.SyncWalletSlotS2CPacket;
import net.nextfur.fwc.network.economy.WalletActionC2SPacket;
import net.nextfur.fwc.network.economy.WalletSyncS2CPacket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PickpocketCommand {

    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MILLIS = 10_000L; // 10 seconds cooldown

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> pickpocketNode = dispatcher.register(
                Commands.literal("pickpocket")
                        .executes(ctx -> executePickpocket(ctx, null))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(ctx -> executePickpocket(ctx, EntityArgument.getPlayer(ctx, "target")))
                        )
        );

        // Aliases
        dispatcher.register(Commands.literal("pickpoket").redirect(pickpocketNode));
        dispatcher.register(Commands.literal("batercarteira").redirect(pickpocketNode));
    }

    private static int executePickpocket(CommandContext<CommandSourceStack> context, ServerPlayer targetArg) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer thief)) {
            source.sendFailure(Component.literal("§6[FurWatch] §cSomente jogadores podem executar este comando."));
            return 0;
        }

        if (thief.isSpectator() || !thief.isAlive()) {
            source.sendFailure(Component.literal("§6[FurWatch] §cVocê não pode roubar carteiras neste gamemode!"));
            return 0;
        }

        // Determine target
        ServerPlayer target = targetArg;
        if (target == null) {
            AABB searchBox = thief.getBoundingBox().inflate(3.0);
            List<ServerPlayer> nearby = thief.serverLevel().getEntitiesOfClass(
                    ServerPlayer.class,
                    searchBox,
                    p -> p != thief && !p.isSpectator() && p.isAlive() && thief.distanceTo(p) <= 3.0
            );

            if (nearby.isEmpty()) {
                source.sendFailure(Component.literal("§6[FurWatch] §cNenhum jogador por perto (1-3 blocos de distância) para roubar a carteira!"));
                return 0;
            }

            nearby.sort(Comparator.comparingDouble(thief::distanceTo));
            target = nearby.get(0);
        }

        // Validate target
        if (target.getUUID().equals(thief.getUUID())) {
            source.sendFailure(Component.literal("§6[FurWatch] §cVocê não pode roubar a sua própria carteira!"));
            return 0;
        }

        if (thief.level() != target.level() || thief.distanceTo(target) > 3.0) {
            source.sendFailure(Component.literal("§6[FurWatch] §c" + target.getName().getString() + " está muito longe! Você precisa estar a no máximo 3 blocos de distância."));
            return 0;
        }

        if (target.isSpectator() || !target.isAlive()) {
            source.sendFailure(Component.literal("§6[FurWatch] §cO jogador alvo não está disponível."));
            return 0;
        }

        // Cooldown check
        long now = System.currentTimeMillis();
        if (!FwPermissions.hasAdminPermission(source)) {
            long last = COOLDOWNS.getOrDefault(thief.getUUID(), 0L);
            if (now - last < COOLDOWN_MILLIS) {
                long remainingSeconds = (COOLDOWN_MILLIS - (now - last) + 999) / 1000;
                source.sendFailure(Component.literal("§6[FurWatch] §cAguarde " + remainingSeconds + "s para tentar roubar uma carteira novamente!"));
                return 0;
            }
        }

        List<WalletRef> targetWallets = findWallets(target);
        if (targetWallets.isEmpty()) {
            source.sendFailure(Component.literal("§6[FurWatch] §c" + target.getName().getString() + " não possui nenhuma carteira no inventário!"));
            return 0;
        }

        WalletRef richestWallet = null;
        for (WalletRef ref : targetWallets) {
            if (richestWallet == null || ref.data().balanceCents() > richestWallet.data().balanceCents()) {
                richestWallet = ref;
            }
        }

        long targetBalance = richestWallet.data().balanceCents();
        if (targetBalance <= 0) {
            source.sendFailure(Component.literal("§6[FurWatch] §cA carteira de " + target.getName().getString() + " está vazia! Não há dinheiro para furtar."));
            return 0;
        }

        COOLDOWNS.put(thief.getUUID(), now);

        RandomSource random = thief.getRandom();
        boolean success = random.nextInt(8) == 0;

        if (success) {
            handleSuccess(thief, target, richestWallet, targetBalance, random);
        } else {
            handleMiss(thief, target, random);
        }

        return 1;
    }

    private static void handleSuccess(ServerPlayer thief, ServerPlayer target, WalletRef richestTargetWallet, long targetBalance, RandomSource random) {
        int percent = 1 + random.nextInt(20);
        long stolenCents = Math.round(targetBalance * (percent / 100.0));
        stolenCents = Math.max(1L, Math.min(targetBalance, stolenCents));

        long newTargetBalance = targetBalance - stolenCents;
        WalletData updatedTargetData = richestTargetWallet.data().withBalance(newTargetBalance);
        richestTargetWallet.stack().set(FwDataComponents.WALLET_DATA.get(), updatedTargetData);

        if (richestTargetWallet.isEquippedSlot()) {
            target.setData(FwAttachments.WALLET_SLOT.get(), richestTargetWallet.stack());
            PacketDistributor.sendToPlayer(target, new SyncWalletSlotS2CPacket(richestTargetWallet.stack()));
        }

        if (target.containerMenu instanceof WalletMenu menu) {
            if (menu.getWalletStack() == richestTargetWallet.stack() || richestTargetWallet.isEquippedSlot()) {
                menu.setWalletStack(richestTargetWallet.stack());
                menu.broadcastChanges();
                PacketDistributor.sendToPlayer(target, new WalletSyncS2CPacket(updatedTargetData));
            }
        }
        target.containerMenu.broadcastChanges();
        target.inventoryMenu.broadcastChanges();

        EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                target.getUUID(), target.getName().getString(), updatedTargetData.walletId(), newTargetBalance
        );
        EconomyDatabaseManager.getInstance().logTransaction(
                "PICKPOCKET_VICTIM", target.getUUID(), target.getName().getString(),
                thief.getUUID(), thief.getName().getString(), stolenCents, newTargetBalance,
                "Pickpocketed by " + thief.getName().getString() + " (" + percent + "%)"
        );

        List<WalletRef> thiefWallets = findWallets(thief);
        if (!thiefWallets.isEmpty()) {
            WalletRef thiefWallet = thiefWallets.stream()
                    .filter(WalletRef::isEquippedSlot)
                    .findFirst()
                    .orElse(thiefWallets.get(0));

            long newThiefBalance = thiefWallet.data().balanceCents() + stolenCents;
            WalletData updatedThiefData = thiefWallet.data().withBalance(newThiefBalance);
            thiefWallet.stack().set(FwDataComponents.WALLET_DATA.get(), updatedThiefData);

            if (thiefWallet.isEquippedSlot()) {
                thief.setData(FwAttachments.WALLET_SLOT.get(), thiefWallet.stack());
                PacketDistributor.sendToPlayer(thief, new SyncWalletSlotS2CPacket(thiefWallet.stack()));
            }

            if (thief.containerMenu instanceof WalletMenu menu) {
                if (menu.getWalletStack() == thiefWallet.stack() || thiefWallet.isEquippedSlot()) {
                    menu.setWalletStack(thiefWallet.stack());
                    menu.broadcastChanges();
                    PacketDistributor.sendToPlayer(thief, new WalletSyncS2CPacket(updatedThiefData));
                }
            }
            thief.containerMenu.broadcastChanges();
            thief.inventoryMenu.broadcastChanges();

            EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                    thief.getUUID(), thief.getName().getString(), updatedThiefData.walletId(), newThiefBalance
            );
            EconomyDatabaseManager.getInstance().logTransaction(
                    "PICKPOCKET_STOLEN", thief.getUUID(), thief.getName().getString(),
                    target.getUUID(), target.getName().getString(), stolenCents, newThiefBalance,
                    "Stolen from " + target.getName().getString() + " (" + percent + "%) into wallet"
            );
        } else {
            List<ItemStack> denominations = WalletActionC2SPacket.calculateDenominations(stolenCents);
            for (ItemStack cash : denominations) {
                if (!thief.getInventory().add(cash)) {
                    thief.drop(cash, false);
                }
            }
            thief.containerMenu.broadcastChanges();
            thief.inventoryMenu.broadcastChanges();

            EconomyDatabaseManager.getInstance().logTransaction(
                    "PICKPOCKET_STOLEN", thief.getUUID(), thief.getName().getString(),
                    target.getUUID(), target.getName().getString(), stolenCents, 0L,
                    "Stolen from " + target.getName().getString() + " (" + percent + "%) as physical cash"
            );
        }

        String formatted = EconomyFormatHelper.formatFull(stolenCents);
        thief.sendSystemMessage(Component.literal("§6[FurWatch] §aVocê bateu a carteira de §e" + target.getName().getString()
                + "§a com sucesso! Furtou: §e" + formatted + " §7(" + percent + "% do saldo)."));
        thief.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7f, 1.2f);
    }

    private static void handleMiss(ServerPlayer thief, ServerPlayer target, RandomSource random) {
        int alertRoll = random.nextInt(100); // 0..99

        if (alertRoll < 5) {
            target.sendSystemMessage(Component.literal("§6[FurWatch] §c§lAtenção! §e" + thief.getName().getString()
                    + " §ctentou te roubar!"));
            target.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1.0f, 0.8f);

            thief.sendSystemMessage(Component.literal("§6[FurWatch] §cVocê falhou e foi flagrado por §e"
                    + target.getName().getString() + "§c!"));
            thief.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 0.8f);
        } else if (alertRoll < 50) {
            target.sendSystemMessage(Component.literal("§6[FurWatch] §cVocê sentiu uma movimentação estranha... §eAlguém tentou te roubar!"));
            target.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);

            thief.sendSystemMessage(Component.literal("§6[FurWatch] §cVocê falhou ao tentar roubar a carteira de §e"
                    + target.getName().getString() + "§c!"));
            thief.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 0.8f);
        } else {
            thief.sendSystemMessage(Component.literal("§6[FurWatch] §cVocê falhou ao roubar a carteira de §e"
                    + target.getName().getString() + "§c, mas não foi notado."));
            thief.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 0.8f);
        }

        EconomyDatabaseManager.getInstance().logTransaction(
                "PICKPOCKET_FAIL", thief.getUUID(), thief.getName().getString(),
                target.getUUID(), target.getName().getString(), 0L, 0L,
                "Pickpocket attempt failed (alert roll: " + alertRoll + ")"
        );
    }

    private record WalletRef(ItemStack stack, WalletData data, boolean isEquippedSlot) {}

    private static List<WalletRef> findWallets(ServerPlayer player) {
        List<WalletRef> list = new ArrayList<>();

        ItemStack equipped = player.getData(FwAttachments.WALLET_SLOT.get());
        if (!equipped.isEmpty() && equipped.getItem() instanceof WalletItem) {
            WalletData data = WalletItem.getOrCreateWalletData(equipped, player);
            list.add(new WalletRef(equipped, data, true));
        }

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof WalletItem) {
                WalletData data = WalletItem.getOrCreateWalletData(stack, player);
                list.add(new WalletRef(stack, data, false));
            }
        }

        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty() && stack.getItem() instanceof WalletItem) {
                WalletData data = WalletItem.getOrCreateWalletData(stack, player);
                list.add(new WalletRef(stack, data, false));
            }
        }

        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && stack.getItem() instanceof WalletItem) {
                WalletData data = WalletItem.getOrCreateWalletData(stack, player);
                list.add(new WalletRef(stack, data, false));
            }
        }

        return list;
    }
}
