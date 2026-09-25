package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import net.nextfur.fwc.economy.data.WalletData;
import net.nextfur.fwc.economy.db.EconomyDatabaseManager;
import net.nextfur.fwc.economy.db.records.CheckRecord;
import net.nextfur.fwc.economy.db.records.GlobalEconomyStats;
import net.nextfur.fwc.economy.db.records.TransactionRecord;
import net.nextfur.fwc.economy.db.records.WalletSnapshotRecord;
import net.nextfur.fwc.economy.items.WalletItem;
import net.nextfur.fwc.init.FwAttachments;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.init.FwPermissions;
import net.nextfur.fwc.network.economy.SyncWalletSlotS2CPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EconomyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("economy")
                .then(Commands.literal("balance")
                        .executes(ctx -> showOwnBalance(ctx.getSource()))
                )
                .then(Commands.literal("admin")
                        .requires(FwPermissions::hasAdminPermission)
                        .then(Commands.literal("total")
                                .executes(ctx -> showGlobalEconomyTotal(ctx.getSource()))
                        )
                        .then(Commands.literal("supply")
                                .executes(ctx -> showGlobalEconomyTotal(ctx.getSource()))
                        )
                        .then(Commands.literal("overview")
                                .executes(ctx -> showGlobalEconomyTotal(ctx.getSource()))
                        )
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
        ItemStack equippedWallet = target.getData(FwAttachments.WALLET_SLOT.get());
        long equippedBalance = 0L;
        if (!equippedWallet.isEmpty() && equippedWallet.getItem() instanceof WalletItem) {
            WalletData data = WalletItem.getOrCreateWalletData(equippedWallet, target);
            equippedBalance = data.balanceCents();
            EconomyDatabaseManager.getInstance().upsertWallet(data.walletId(), target.getUUID(), target.getName().getString(), equippedBalance);
        }

        long otherWalletsBalance = 0L;
        int otherWalletsCount = 0;

        // Check main inventory items (slots 0-35)
        for (ItemStack stack : target.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem() instanceof WalletItem) {
                WalletData d = WalletItem.getOrCreateWalletData(stack, target);
                otherWalletsBalance += d.balanceCents();
                otherWalletsCount++;
                EconomyDatabaseManager.getInstance().upsertWallet(d.walletId(), target.getUUID(), target.getName().getString(), d.balanceCents());
            }
        }

        // Check armor
        for (ItemStack stack : target.getInventory().armor) {
            if (!stack.isEmpty() && stack.getItem() instanceof WalletItem) {
                WalletData d = WalletItem.getOrCreateWalletData(stack, target);
                otherWalletsBalance += d.balanceCents();
                otherWalletsCount++;
                EconomyDatabaseManager.getInstance().upsertWallet(d.walletId(), target.getUUID(), target.getName().getString(), d.balanceCents());
            }
        }

        // Check offhand
        for (ItemStack stack : target.getInventory().offhand) {
            if (!stack.isEmpty() && stack.getItem() instanceof WalletItem) {
                WalletData d = WalletItem.getOrCreateWalletData(stack, target);
                otherWalletsBalance += d.balanceCents();
                otherWalletsCount++;
                EconomyDatabaseManager.getInstance().upsertWallet(d.walletId(), target.getUUID(), target.getName().getString(), d.balanceCents());
            }
        }

        // Check ender chest
        var enderChest = target.getEnderChestInventory();
        for (int i = 0; i < enderChest.getContainerSize(); i++) {
            ItemStack stack = enderChest.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof WalletItem) {
                WalletData d = WalletItem.getOrCreateWalletData(stack, target);
                otherWalletsBalance += d.balanceCents();
                otherWalletsCount++;
                EconomyDatabaseManager.getInstance().upsertWallet(d.walletId(), target.getUUID(), target.getName().getString(), d.balanceCents());
            }
        }

        renderInspection(source, target.getUUID(), target.getName().getString(), true, equippedBalance, otherWalletsCount, otherWalletsBalance);
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
            EconomyDatabaseManager.getInstance().getTotalWalletsBalanceByOwner(snapshot.playerUuid()).thenAccept(totalWalletsBalance -> {
                long equippedBalance = snapshot.lastBalanceCents();
                long otherBalance = Math.max(0L, totalWalletsBalance - equippedBalance);
                int otherCount = otherBalance > 0 ? 1 : 0;
                renderInspection(source, snapshot.playerUuid(), snapshot.playerName(), false, equippedBalance, otherCount, otherBalance);
            });
        });

        return 1;
    }

    private static void renderInspection(CommandSourceStack source, UUID targetUuid, String targetName, boolean isOnline,
                                         long equippedBalance, int otherWalletsCount, long otherWalletsBalance) {
        EconomyDatabaseManager.getInstance().getActiveChecksByIssuer(targetUuid).thenAccept(checks -> {
            long totalWalletsBalance = equippedBalance + otherWalletsBalance;
            long checksTotal = checks.stream().mapToLong(CheckRecord::amountCents).sum();
            long totalCirculation = totalWalletsBalance + checksTotal;

            source.getServer().execute(() -> {
                source.sendSuccess(() -> Component.literal("§8================= §6§lFurWatch Economia §8================="), false);
                source.sendSuccess(() -> Component.literal("§7Jogador: §e" + targetName + " §8(UUID: " + targetUuid + ")"), false);
                source.sendSuccess(() -> Component.literal("§7Status: " + (isOnline ? "§a● Online" : "§7○ Offline (Snapshot)")), false);
                source.sendSuccess(() -> Component.literal("§7Saldo Carteira Equipada: §a" + EconomyFormatHelper.formatFull(equippedBalance)), false);
                if (otherWalletsCount > 0 || otherWalletsBalance > 0) {
                    source.sendSuccess(() -> Component.literal("§7Outras Carteiras (Inventário/Baú): §e" + otherWalletsCount + " carteira(s) §7(Total: §a" + EconomyFormatHelper.formatStandard(otherWalletsBalance) + "§7)"), false);
                }
                source.sendSuccess(() -> Component.literal("§7Total em Carteiras: §a" + EconomyFormatHelper.formatFull(totalWalletsBalance)), false);
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

    private static int showGlobalEconomyTotal(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§6[FurWatch] §7Calculando censo econômico global..."), false);
        scanOnlinePlayersWallets(source.getServer())
                .thenCompose(v -> EconomyDatabaseManager.getInstance().getGlobalEconomyStats())
                .thenAccept(stats -> {
                    source.getServer().execute(() -> {
                        source.sendSuccess(() -> Component.literal("§8================ §6§lFurWatch - Censo Econômico Global §8================"), false);
                        source.sendSuccess(() -> Component.literal("§7Carteiras Registradas: §e" + stats.totalWallets() + " §7(equipadas e guardadas)"), false);
                        source.sendSuccess(() -> Component.literal("§7Total em Carteiras: §a" + EconomyFormatHelper.formatFull(stats.totalWalletCents())), false);
                        source.sendSuccess(() -> Component.literal("§7Cheques Ativos Pendentes: §6" + stats.activeChecksCount() + " cheque(s) §7(Total: §c" + EconomyFormatHelper.formatStandard(stats.totalActiveChecksCents()) + "§7)"), false);
                        source.sendSuccess(() -> Component.literal("§eMassa Monetária Total em Circulação: §6§l" + EconomyFormatHelper.formatFull(stats.totalCirculatingCents())), false);
                        source.sendSuccess(() -> Component.literal("§8-----------------------------------------------------------------"), false);
                        source.sendSuccess(() -> Component.literal("§7Saldo Médio por Carteira: §f" + EconomyFormatHelper.formatStandard(stats.averageWalletCents())), false);
                        source.sendSuccess(() -> Component.literal("§7Maior Carteira Cadastrada: §e" + stats.topWalletOwner() + " §7(§a" + EconomyFormatHelper.formatStandard(stats.topWalletBalanceCents()) + "§7)"), false);
                        source.sendSuccess(() -> Component.literal("§8================================================================="), false);
                    });
                })
                .exceptionally(ex -> {
                    source.getServer().execute(() -> {
                        source.sendFailure(Component.literal("§cErro ao calcular total da economia: " + ex.getMessage()));
                    });
                    return null;
                });
        return 1;
    }

    private static CompletableFuture<Void> scanOnlinePlayersWallets(MinecraftServer server) {
        if (server == null) return CompletableFuture.completedFuture(null);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            // 1. Dedicated wallet slot
            ItemStack equipped = player.getData(FwAttachments.WALLET_SLOT.get());
            if (!equipped.isEmpty() && equipped.getItem() instanceof WalletItem) {
                futures.add(recordWallet(equipped, player));
            }

            // 2. Main inventory & hotbar
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && stack.getItem() instanceof WalletItem) {
                    futures.add(recordWallet(stack, player));
                }
            }

            // 3. Armor slots
            for (ItemStack stack : player.getInventory().armor) {
                if (!stack.isEmpty() && stack.getItem() instanceof WalletItem) {
                    futures.add(recordWallet(stack, player));
                }
            }

            // 4. Offhand slot
            for (ItemStack stack : player.getInventory().offhand) {
                if (!stack.isEmpty() && stack.getItem() instanceof WalletItem) {
                    futures.add(recordWallet(stack, player));
                }
            }

            // 5. Ender Chest
            var enderChest = player.getEnderChestInventory();
            for (int i = 0; i < enderChest.getContainerSize(); i++) {
                ItemStack stack = enderChest.getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof WalletItem) {
                    futures.add(recordWallet(stack, player));
                }
            }
        }

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    private static CompletableFuture<Void> recordWallet(ItemStack stack, ServerPlayer player) {
        WalletData data = WalletItem.getOrCreateWalletData(stack, player);
        UUID ownerUuid = data.ownerUuid() != null ? data.ownerUuid() : player.getUUID();
        String ownerName = data.ownerName() != null && !data.ownerName().isEmpty() ? data.ownerName() : player.getName().getString();
        return EconomyDatabaseManager.getInstance().upsertWallet(data.walletId(), ownerUuid, ownerName, data.balanceCents());
    }
}
