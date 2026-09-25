package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import net.nextfur.fwc.economy.data.WalletData;
import net.nextfur.fwc.economy.db.EconomyDatabaseManager;
import net.nextfur.fwc.economy.db.records.CheckRecord;
import net.nextfur.fwc.economy.db.records.TransactionRecord;
import net.nextfur.fwc.economy.db.records.WalletSnapshotRecord;
import net.nextfur.fwc.economy.items.WalletItem;
import net.nextfur.fwc.init.FwAttachments;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.init.FwPermissions;
import net.nextfur.fwc.network.economy.SyncWalletSlotS2CPacket;

import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;

public class EconomyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("economy")
                .then(Commands.literal("balance")
                        .executes(ctx -> showOwnBalance(ctx.getSource()))
                )
                .then(Commands.literal("admin")
                        .requires(FwPermissions::hasAdminPermission)
                        .then(Commands.literal("check")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> inspectOnlinePlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                                )
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> inspectPlayerByName(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
                                )
                        )
                        .then(Commands.literal("history")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> showHistory(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), 5))
                                        .then(Commands.argument("limit", IntegerArgumentType.integer(1, 50))
                                                .executes(ctx -> showHistory(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "limit")))
                                        )
                                )
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> showHistoryByName(ctx.getSource(), StringArgumentType.getString(ctx, "name"), 5))
                                        .then(Commands.argument("limit", IntegerArgumentType.integer(1, 50))
                                                .executes(ctx -> showHistoryByName(ctx.getSource(), StringArgumentType.getString(ctx, "name"), IntegerArgumentType.getInteger(ctx, "limit")))
                                        )
                                )
                        )
                        .then(Commands.literal("give")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", StringArgumentType.word())
                                                .executes(ctx -> adminAdjust(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "amount"), 0))
                                        )
                                )
                        )
                        .then(Commands.literal("take")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", StringArgumentType.word())
                                                .executes(ctx -> adminAdjust(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "amount"), 1))
                                        )
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", StringArgumentType.word())
                                                .executes(ctx -> adminAdjust(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "amount"), 2))
                                        )
                                )
                        )
                );

        dispatcher.register(root);

        // Aliases
        dispatcher.register(Commands.literal("fweco").redirect(dispatcher.getRoot().getChild("economy")));
        dispatcher.register(Commands.literal("carteira").redirect(dispatcher.getRoot().getChild("economy")));
    }

    private static int showOwnBalance(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Somente jogadores possuem carteira física."));
            return 0;
        }

        ItemStack wallet = player.getData(FwAttachments.WALLET_SLOT.get());
        if (wallet.isEmpty()) {
            wallet = player.getMainHandItem();
        }

        if (wallet.getItem() instanceof WalletItem) {
            WalletData data = wallet.get(FwDataComponents.WALLET_DATA.get());
            long balance = data != null ? data.balanceCents() : 0L;
            source.sendSuccess(() -> Component.literal("§6[FurWatch] §7Saldo na Carteira: §a" + EconomyFormatHelper.formatFull(balance)), false);
        } else {
            source.sendFailure(Component.literal("Você não tem nenhuma carteira equipada no slot nem na mão principal!"));
        }
        return 1;
    }

    private static int inspectOnlinePlayer(CommandSourceStack source, ServerPlayer target) {
        ItemStack wallet = target.getData(FwAttachments.WALLET_SLOT.get());
        if (wallet.isEmpty() && target.getMainHandItem().getItem() instanceof WalletItem) {
            wallet = target.getMainHandItem();
        }

        long walletBalance = 0L;
        if (!wallet.isEmpty()) {
            WalletData data = wallet.get(FwDataComponents.WALLET_DATA.get());
            if (data != null) {
                walletBalance = data.balanceCents();
            }
        }

        renderInspection(source, target.getUUID(), target.getName().getString(), true, walletBalance);
        return 1;
    }

    private static int inspectPlayerByName(CommandSourceStack source, String name) {
        ServerPlayer online = source.getServer().getPlayerList().getPlayerByName(name);
        if (online != null) {
            return inspectOnlinePlayer(source, online);
        }

        EconomyDatabaseManager.getInstance().getWalletSnapshotByName(name).thenAccept(snapshot -> {
            if (snapshot == null) {
                source.sendFailure(Component.literal("Jogador '" + name + "' não encontrado nos registros do banco de dados."));
                return;
            }
            renderInspection(source, snapshot.playerUuid(), snapshot.playerName(), false, snapshot.lastBalanceCents());
        });

        return 1;
    }

    private static void renderInspection(CommandSourceStack source, UUID targetUuid, String targetName, boolean isOnline, long walletBalance) {
        EconomyDatabaseManager.getInstance().getActiveChecksByIssuer(targetUuid).thenAccept(checks -> {
            long checksTotal = checks.stream().mapToLong(CheckRecord::amountCents).sum();
            long totalCirculation = walletBalance + checksTotal;

            source.sendSuccess(() -> Component.literal("§8================= §6§lFurWatch Economia §8================="), false);
            source.sendSuccess(() -> Component.literal("§7Jogador: §e" + targetName + " §8(UUID: " + targetUuid + ")"), false);
            source.sendSuccess(() -> Component.literal("§7Status: " + (isOnline ? "§a● Online" : "§7○ Offline (Snapshot)")), false);
            source.sendSuccess(() -> Component.literal("§7Saldo na Carteira: §a" + EconomyFormatHelper.formatFull(walletBalance)), false);
            source.sendSuccess(() -> Component.literal("§7Cheques Emitidos Pendentes: §6" + checks.size() + " cheque(s) §7(Total: §c" + EconomyFormatHelper.formatStandard(checksTotal) + "§7)"), false);

            int shown = 0;
            for (CheckRecord c : checks) {
                if (shown >= 5) {
                    source.sendSuccess(() -> Component.literal("  §8... e mais " + (checks.size() - 5) + " cheque(s) pendentes."), false);
                    break;
                }
                String shortId = c.checkId().toString().substring(0, 8);
                source.sendSuccess(() -> Component.literal("  §8- §7#" + shortId + ": §f" + EconomyFormatHelper.formatStandard(c.amountCents()) +
                        " §7(Para: §e" + c.payee() + "§7 em " + c.issuedAt() + ")"), false);
                shown++;
            }

            source.sendSuccess(() -> Component.literal("§7Total em Cheques Não Compensados: §c" + EconomyFormatHelper.formatStandard(checksTotal)), false);
            source.sendSuccess(() -> Component.literal("§ePatrimônio Total em Circulação: §6§l" + EconomyFormatHelper.formatFull(totalCirculation)), false);
            source.sendSuccess(() -> Component.literal("§8========================================================="), false);
        });
    }

    private static int showHistory(CommandSourceStack source, ServerPlayer target, int limit) {
        showHistoryInternal(source, target.getUUID(), target.getName().getString(), limit);
        return 1;
    }

    private static int showHistoryByName(CommandSourceStack source, String name, int limit) {
        ServerPlayer online = source.getServer().getPlayerList().getPlayerByName(name);
        if (online != null) {
            return showHistory(source, online, limit);
        }

        EconomyDatabaseManager.getInstance().getWalletSnapshotByName(name).thenAccept(snapshot -> {
            if (snapshot == null) {
                source.sendFailure(Component.literal("Jogador '" + name + "' não encontrado nos registros do banco de dados."));
                return;
            }
            showHistoryInternal(source, snapshot.playerUuid(), snapshot.playerName(), limit);
        });
        return 1;
    }

    private static void showHistoryInternal(CommandSourceStack source, UUID targetUuid, String targetName, int limit) {
        EconomyDatabaseManager.getInstance().getRecentTransactions(targetUuid, limit).thenAccept(list -> {
            source.sendSuccess(() -> Component.literal("§8--- [ §6Últimas " + list.size() + " Transações de §e" + targetName + " §8] ---"), false);
            if (list.isEmpty()) {
                source.sendSuccess(() -> Component.literal("§7Nenhuma transação registrada."), false);
                return;
            }
            for (TransactionRecord tx : list) {
                String typeColor = switch (tx.txType()) {
                    case "DEPOSIT_CASH", "CHECK_DEPOSITED", "ADMIN_GIVE" -> "§a";
                    case "WITHDRAW_CASH", "CHECK_ISSUED", "ADMIN_TAKE" -> "§c";
                    default -> "§e";
                };
                source.sendSuccess(() -> Component.literal("§8[" + tx.timestamp() + "] " + typeColor + tx.txType() +
                        ": §f" + EconomyFormatHelper.formatStandard(tx.amountCents()) +
                        " §7(Saldo pós: §6" + EconomyFormatHelper.formatStandard(tx.balanceAfter()) + "§7) §8- " + tx.details()), false);
            }
        });
    }

    private static int adminAdjust(CommandSourceStack source, ServerPlayer target, String amountStr, int mode) {
        OptionalLong parsed = EconomyFormatHelper.parseToCents(amountStr);
        if (parsed.isEmpty() || parsed.getAsLong() < 0) {
            source.sendFailure(Component.literal("Valor inválido: " + amountStr));
            return 0;
        }

        long amountCents = parsed.getAsLong();
        ItemStack wallet = target.getData(FwAttachments.WALLET_SLOT.get());
        boolean isEquippedSlot = true;

        if (wallet.isEmpty()) {
            wallet = target.getMainHandItem();
            isEquippedSlot = false;
        }

        if (!(wallet.getItem() instanceof WalletItem)) {
            source.sendFailure(Component.literal("O jogador alvo (" + target.getName().getString() + ") não possui uma carteira equipada no slot nem na mão principal!"));
            return 0;
        }

        WalletData data = WalletItem.getOrCreateWalletData(wallet, target);
        long currentBalance = data.balanceCents();
        long newBalance;
        String actionName;

        switch (mode) {
            case 0 -> { // Give
                newBalance = currentBalance + amountCents;
                actionName = "ADMIN_GIVE";
            }
            case 1 -> { // Take
                newBalance = Math.max(0L, currentBalance - amountCents);
                actionName = "ADMIN_TAKE";
            }
            case 2 -> { // Set
                newBalance = amountCents;
                actionName = "ADMIN_SET";
            }
            default -> {
                return 0;
            }
        }

        WalletData updated = data.withBalance(newBalance);
        wallet.set(FwDataComponents.WALLET_DATA.get(), updated);

        if (isEquippedSlot) {
            target.setData(FwAttachments.WALLET_SLOT.get(), wallet);
            PacketDistributor.sendToPlayer(target, new SyncWalletSlotS2CPacket(wallet));
        }

        target.containerMenu.broadcastChanges();

        String staffName = source.getTextName();
        UUID staffUuid = source.getEntity() instanceof ServerPlayer sp ? sp.getUUID() : UUID.randomUUID();

        EconomyDatabaseManager.getInstance().logTransaction(
                actionName, target.getUUID(), target.getName().getString(),
                staffUuid, staffName, amountCents, newBalance,
                "Admin adjustment by " + staffName
        );
        EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                target.getUUID(), target.getName().getString(), updated.walletId(), newBalance
        );

        source.sendSuccess(() -> Component.literal("§6[FurWatch] §aSaldo de §e" + target.getName().getString() +
                " §aatualizado com sucesso para: §f" + EconomyFormatHelper.formatFull(newBalance)), true);

        target.sendSystemMessage(Component.literal("§6[FurWatch] §eSeu saldo foi atualizado pela Staff (" + staffName + "): §a" +
                EconomyFormatHelper.formatFull(newBalance)));

        return 1;
    }
}
