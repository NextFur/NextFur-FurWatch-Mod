package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.economy.data.CheckData;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import net.nextfur.fwc.economy.data.WalletData;
import net.nextfur.fwc.economy.db.EconomyDatabaseManager;
import net.nextfur.fwc.economy.db.records.CheckRecord;
import net.nextfur.fwc.economy.db.records.GlobalEconomyStats;
import net.nextfur.fwc.economy.db.records.TransactionRecord;
import net.nextfur.fwc.economy.db.records.WalletSnapshotRecord;
import net.nextfur.fwc.economy.items.CurrencyItem;
import net.nextfur.fwc.economy.items.CurrencyLayerBlockItem;
import net.nextfur.fwc.economy.items.SignedCheckItem;
import net.nextfur.fwc.economy.items.WalletItem;
import net.nextfur.fwc.init.FwAttachments;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.init.FwPermissions;
import net.nextfur.fwc.network.economy.SyncWalletSlotS2CPacket;
import net.nextfur.fwc.pda.data.PdaData;
import net.nextfur.fwc.pda.items.PdaItem;

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
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer self) {
                                        return inspectOnlinePlayer(ctx.getSource(), self);
                                    }
                                    ctx.getSource().sendFailure(Component.literal("Especifique um jogador: /economy admin check <jogador>"));
                                    return 0;
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> inspectOnlinePlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                                )
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> inspectPlayerByName(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
                                )
                        )
                        .then(Commands.literal("history")
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer self) {
                                        return showHistory(ctx.getSource(), self, 10);
                                    }
                                    ctx.getSource().sendFailure(Component.literal("Especifique um jogador: /economy admin history <jogador>"));
                                    return 0;
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> showHistory(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), 10))
                                        .then(Commands.argument("limit", IntegerArgumentType.integer(1, 50))
                                                .executes(ctx -> showHistory(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), IntegerArgumentType.getInteger(ctx, "limit")))
                                        )
                                )
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> showHistoryByName(ctx.getSource(), StringArgumentType.getString(ctx, "name"), 10))
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

    private static List<ItemStack> getAllPlayerStacks(ServerPlayer player) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.addAll(player.getInventory().items);
        stacks.addAll(player.getInventory().armor);
        stacks.addAll(player.getInventory().offhand);
        var enderChest = player.getEnderChestInventory();
        for (int i = 0; i < enderChest.getContainerSize(); i++) {
            stacks.add(enderChest.getItem(i));
        }
        return stacks;
    }

    private static int inspectOnlinePlayer(CommandSourceStack source, ServerPlayer target) {
        ItemStack equippedWallet = target.getData(FwAttachments.WALLET_SLOT.get());
        long equippedBalance = 0L;
        if (!equippedWallet.isEmpty() && equippedWallet.getItem() instanceof WalletItem) {
            WalletData data = WalletItem.getOrCreateWalletData(equippedWallet, target);
            equippedBalance = data.balanceCents();
            EconomyDatabaseManager.getInstance().updateWalletSnapshot(target.getUUID(), target.getName().getString(), data.walletId(), equippedBalance);
        }

        long otherWalletsBalance = 0L;
        int otherWalletsCount = 0;
        long bankBalance = 0L;
        int bankCount = 0;
        long physicalCash = 0L;

        for (ItemStack stack : getAllPlayerStacks(target)) {
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof WalletItem) {
                WalletData d = WalletItem.getOrCreateWalletData(stack, target);
                otherWalletsBalance += d.balanceCents();
                otherWalletsCount++;
            } else if (stack.getItem() instanceof PdaItem pdaItem) {
                PdaData d = pdaItem.getOrCreatePdaData(stack, target);
                bankBalance += d.bankBalanceCents();
                bankCount++;
            } else if (stack.getItem() instanceof CurrencyItem currencyItem) {
                physicalCash += currencyItem.getValueInCents() * stack.getCount();
            } else if (stack.getItem() instanceof CurrencyLayerBlockItem layerItem) {
                physicalCash += layerItem.getValueInCents() * stack.getCount();
            } else if (stack.getItem() instanceof SignedCheckItem) {
                CheckData cd = stack.get(FwDataComponents.CHECK_DATA.get());
                if (cd != null && !cd.deposited()) {
                    EconomyDatabaseManager.getInstance().registerCheck(cd.checkId(), cd.issuerUuid(), cd.issuerName(), cd.payee(), cd.amountCents());
                }
            }
        }

        renderInspection(source, target.getUUID(), target.getName().getString(), true,
                equippedBalance, otherWalletsCount, otherWalletsBalance, bankCount, bankBalance, physicalCash);
        return 1;
    }

    private static int inspectPlayerByName(CommandSourceStack source, String name) {
        ServerPlayer online = source.getServer().getPlayerList().getPlayerByName(name);
        if (online != null) {
            return inspectOnlinePlayer(source, online);
        }

        EconomyDatabaseManager.getInstance().getWalletSnapshotByName(name).thenAccept(snapshot -> {
            if (snapshot == null) {
                source.getServer().execute(() ->
                        source.sendFailure(Component.literal("Jogador '" + name + "' não encontrado nos registros do banco de dados."))
                );
                return;
            }
            CompletableFuture<Long> walletsFuture = EconomyDatabaseManager.getInstance().getTotalWalletsBalanceByOwner(snapshot.playerUuid());
            CompletableFuture<Long> bankFuture = EconomyDatabaseManager.getInstance().getTotalBankBalanceByOwner(snapshot.playerUuid());

            walletsFuture.thenCombine(bankFuture, (totalWalletsBalance, totalBankBalance) -> {
                long equippedBalance = snapshot.lastBalanceCents();
                long otherBalance = Math.max(0L, totalWalletsBalance - equippedBalance);
                int otherCount = otherBalance > 0 ? 1 : 0;
                int bankCount = totalBankBalance > 0 ? 1 : 0;
                renderInspection(source, snapshot.playerUuid(), snapshot.playerName(), false,
                        equippedBalance, otherCount, otherBalance, bankCount, totalBankBalance, 0L);
                return null;
            });
        });

        return 1;
    }

    private static void renderInspection(CommandSourceStack source, UUID targetUuid, String targetName, boolean isOnline,
                                         long equippedBalance, int otherWalletsCount, long otherWalletsBalance,
                                         int bankCount, long bankBalance, long physicalCash) {
        EconomyDatabaseManager.getInstance().getActiveChecksByIssuer(targetUuid).thenAccept(checks -> {
            long totalWalletsBalance = equippedBalance + otherWalletsBalance;
            long checksTotal = checks.stream().mapToLong(CheckRecord::amountCents).sum();
            long totalCirculation = totalWalletsBalance + bankBalance + physicalCash + checksTotal;
            String shortUuid = targetUuid.toString().substring(0, 8);

            source.getServer().execute(() -> {
                source.sendSuccess(() -> Component.literal("§8====== §6§lFurWatch Economia §8======"), false);
                source.sendSuccess(() -> Component.literal("§7Jogador: §e" + targetName + " ")
                        .append(Component.literal("§8(#" + shortUuid + ")")
                                .withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("UUID: " + targetUuid)))
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, targetUuid.toString()))))
                        .append(Component.literal(isOnline ? " §a● Online" : " §7○ Offline")), false);

                source.sendSuccess(() -> Component.literal("§7Carteira Equipada: §a" + EconomyFormatHelper.formatFull(equippedBalance)), false);
                if (otherWalletsCount > 0 || otherWalletsBalance > 0) {
                    source.sendSuccess(() -> Component.literal("§7Outras Carteiras (§e" + otherWalletsCount + "§7): §a" + EconomyFormatHelper.formatStandard(otherWalletsBalance)), false);
                }
                source.sendSuccess(() -> Component.literal("§7Total Carteiras: §a" + EconomyFormatHelper.formatFull(totalWalletsBalance)), false);

                if (bankCount > 0 || bankBalance > 0) {
                    source.sendSuccess(() -> Component.literal("§7Shield Bank (§b" + bankCount + " PDA§7): §a" + EconomyFormatHelper.formatFull(bankBalance)), false);
                }
                if (physicalCash > 0) {
                    source.sendSuccess(() -> Component.literal("§7Dinheiro Físico: §a" + EconomyFormatHelper.formatFull(physicalCash)), false);
                }

                source.sendSuccess(() -> Component.literal("§7Cheques Pendentes (§6" + checks.size() + "§7): §c" + EconomyFormatHelper.formatStandard(checksTotal)), false);

                int shown = 0;
                for (CheckRecord c : checks) {
                    if (shown >= 5) {
                        source.sendSuccess(() -> Component.literal("  §8... +" + (checks.size() - 5) + " cheque(s)"), false);
                        break;
                    }
                    String shortId = c.checkId().toString().substring(0, 8);
                    source.sendSuccess(() -> Component.literal("  §8- §7#" + shortId + ": §f" + EconomyFormatHelper.formatStandard(c.amountCents()) +
                            " §7-> §e" + c.payee()), false);
                    shown++;
                }

                source.sendSuccess(() -> Component.literal("§ePatrimônio Total: §6§l" + EconomyFormatHelper.formatFull(totalCirculation)), false);
                source.sendSuccess(() -> Component.literal("§8=================================="), false);
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
                source.getServer().execute(() ->
                        source.sendFailure(Component.literal("Jogador '" + name + "' não encontrado nos registros do banco de dados."))
                );
                return;
            }
            showHistoryInternal(source, snapshot.playerUuid(), snapshot.playerName(), limit);
        });
        return 1;
    }

    private static void showHistoryInternal(CommandSourceStack source, UUID targetUuid, String targetName, int limit) {
        EconomyDatabaseManager.getInstance().getRecentTransactions(targetUuid, limit).thenAccept(list -> {
            source.getServer().execute(() -> {
                source.sendSuccess(() -> Component.literal("§8==== §6Histórico: §e" + targetName + " §7(" + list.size() + ") §8===="), false);
                if (list.isEmpty()) {
                    source.sendSuccess(() -> Component.literal("§7Nenhuma transação registrada."), false);
                    source.sendSuccess(() -> Component.literal("§8=================================="), false);
                    return;
                }
                for (TransactionRecord tx : list) {
                    String typeLabel = switch (tx.txType()) {
                        case "ATM_DEPOSIT" -> "§a+ATM Dep.";
                        case "ATM_WITHDRAW" -> "§c-ATM Saque";
                        case "DEPOSIT_CASH" -> "§a+Carteira Dep.";
                        case "WITHDRAW_CASH" -> "§c-Carteira Saque";
                        case "CHECK_DEPOSITED" -> "§a+Cheque Comp.";
                        case "CHECK_ISSUED" -> "§c-Cheque Emit.";
                        case "ADMIN_GIVE" -> "§a+Admin Give";
                        case "ADMIN_TAKE" -> "§c-Admin Take";
                        case "ADMIN_SET" -> "§e=Admin Set";
                        case "WALLET_DROPPED_ON_DEATH" -> "§c!Morte (Drop)";
                        default -> "§e" + tx.txType();
                    };
                    String shortTime = tx.timestamp() != null && tx.timestamp().length() >= 16
                            ? tx.timestamp().substring(5, 16)
                            : String.valueOf(tx.timestamp());
                    String detailText = (tx.details() != null && !tx.details().isBlank()) ? " §8(" + tx.details() + ")" : "";
                    source.sendSuccess(() -> Component.literal("§8[" + shortTime + "] " + typeLabel +
                            ": §f" + EconomyFormatHelper.formatStandard(tx.amountCents()) +
                            " §7(Saldo: §6" + EconomyFormatHelper.formatStandard(tx.balanceAfter()) + "§7)" + detailText), false);
                }
                source.sendSuccess(() -> Component.literal("§8=================================="), false);
            });
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
                "Staff: " + staffName
        );
        EconomyDatabaseManager.getInstance().updateWalletSnapshot(
                target.getUUID(), target.getName().getString(), updated.walletId(), newBalance
        );

        source.sendSuccess(() -> Component.literal("§6[FurWatch] §aSaldo de §e" + target.getName().getString() +
                " §aatualizado para: §f" + EconomyFormatHelper.formatFull(newBalance)), true);

        target.sendSystemMessage(Component.literal("§6[FurWatch] §eSeu saldo foi atualizado pela Staff (" + staffName + "): §a" +
                EconomyFormatHelper.formatFull(newBalance)));

        return 1;
    }

    private record OnlineScanSummary(
            int walletsCount,
            long walletsCents,
            int bankCount,
            long bankCents,
            long physicalCashCents,
            String topOnlineOwner,
            long topOnlineBalance
    ) {}

    private static int showGlobalEconomyTotal(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("§6[FurWatch] §7Calculando censo econômico global..."), false);
        scanOnlinePlayersEconomy(source.getServer())
                .thenCompose(online -> EconomyDatabaseManager.getInstance().getGlobalEconomyStats(online.physicalCashCents())
                        .thenApply(dbStats -> {
                            int totalWallets = Math.max(dbStats.totalWallets(), online.walletsCount());
                            long totalWalletCents = Math.max(dbStats.totalWalletCents(), online.walletsCents());
                            int totalBankAccounts = Math.max(dbStats.totalBankAccounts(), online.bankCount());
                            long totalBankCents = Math.max(dbStats.totalBankCents(), online.bankCents());
                            long physicalCents = online.physicalCashCents();
                            long checksCents = dbStats.totalActiveChecksCents();
                            long totalCirculating = totalWalletCents + totalBankCents + physicalCents + checksCents;
                            int totalAccounts = totalWallets + totalBankAccounts;
                            long avgCents = totalAccounts > 0 ? (totalWalletCents + totalBankCents) / totalAccounts : 0L;
                            String topOwner = dbStats.topWalletBalanceCents() >= online.topOnlineBalance()
                                    ? dbStats.topWalletOwner()
                                    : online.topOnlineOwner();
                            long topBalance = Math.max(dbStats.topWalletBalanceCents(), online.topOnlineBalance());

                            return new GlobalEconomyStats(
                                    totalWallets,
                                    totalWalletCents,
                                    totalBankAccounts,
                                    totalBankCents,
                                    physicalCents,
                                    dbStats.activeChecksCount(),
                                    checksCents,
                                    totalCirculating,
                                    avgCents,
                                    topOwner,
                                    topBalance
                            );
                        }))
                .thenAccept(stats -> {
                    source.getServer().execute(() -> {
                        source.sendSuccess(() -> Component.literal("§8==== §6§lCenso Econômico Global §8===="), false);
                        source.sendSuccess(() -> Component.literal("§7Carteiras (§e" + stats.totalWallets() + "§7): §a" + EconomyFormatHelper.formatFull(stats.totalWalletCents())), false);
                        source.sendSuccess(() -> Component.literal("§7Shield Bank (§b" + stats.totalBankAccounts() + "§7): §a" + EconomyFormatHelper.formatFull(stats.totalBankCents())), false);
                        if (stats.onlinePhysicalCashCents() > 0) {
                            source.sendSuccess(() -> Component.literal("§7Dinheiro Físico (Online): §a" + EconomyFormatHelper.formatFull(stats.onlinePhysicalCashCents())), false);
                        }
                        source.sendSuccess(() -> Component.literal("§7Cheques Pendentes (§6" + stats.activeChecksCount() + "§7): §c" + EconomyFormatHelper.formatStandard(stats.totalActiveChecksCents())), false);
                        source.sendSuccess(() -> Component.literal("§eTotal em Circulação: §6§l" + EconomyFormatHelper.formatFull(stats.totalCirculatingCents())), false);
                        source.sendSuccess(() -> Component.literal("§8----------------------------------"), false);
                        source.sendSuccess(() -> Component.literal("§7Saldo Médio p/ Conta: §f" + EconomyFormatHelper.formatStandard(stats.averageWalletCents())), false);
                        source.sendSuccess(() -> Component.literal("§7Maior Saldo: §e" + stats.topWalletOwner() + " §7(§a" + EconomyFormatHelper.formatStandard(stats.topWalletBalanceCents()) + "§7)"), false);
                        source.sendSuccess(() -> Component.literal("§8=================================="), false);
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

    private static CompletableFuture<OnlineScanSummary> scanOnlinePlayersEconomy(MinecraftServer server) {
        if (server == null) {
            return CompletableFuture.completedFuture(new OnlineScanSummary(0, 0L, 0, 0L, 0L, "Nenhum", 0L));
        }
        List<CompletableFuture<?>> futures = new ArrayList<>();
        int walletsCount = 0;
        long walletsCents = 0L;
        int bankCount = 0;
        long bankCents = 0L;
        long physicalCashCents = 0L;
        String topOnlineOwner = "Nenhum";
        long topOnlineBalance = 0L;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            long playerTrackedBalance = 0L;

            // 1. Dedicated wallet slot
            ItemStack equipped = player.getData(FwAttachments.WALLET_SLOT.get());
            if (!equipped.isEmpty() && equipped.getItem() instanceof WalletItem) {
                WalletData data = WalletItem.getOrCreateWalletData(equipped, player);
                walletsCount++;
                walletsCents += data.balanceCents();
                playerTrackedBalance += data.balanceCents();
                futures.add(recordWallet(equipped, player));
            }

            // 2. Inventory, Armor, Offhand, Ender Chest
            for (ItemStack stack : getAllPlayerStacks(player)) {
                if (stack.isEmpty()) continue;
                if (stack.getItem() instanceof WalletItem) {
                    WalletData data = WalletItem.getOrCreateWalletData(stack, player);
                    walletsCount++;
                    walletsCents += data.balanceCents();
                    playerTrackedBalance += data.balanceCents();
                    futures.add(recordWallet(stack, player));
                } else if (stack.getItem() instanceof PdaItem pdaItem) {
                    PdaData data = pdaItem.getOrCreatePdaData(stack, player);
                    bankCount++;
                    bankCents += data.bankBalanceCents();
                    playerTrackedBalance += data.bankBalanceCents();
                    futures.add(recordPda(stack, pdaItem, player));
                } else if (stack.getItem() instanceof CurrencyItem currencyItem) {
                    physicalCashCents += currencyItem.getValueInCents() * stack.getCount();
                } else if (stack.getItem() instanceof CurrencyLayerBlockItem layerItem) {
                    physicalCashCents += layerItem.getValueInCents() * stack.getCount();
                } else if (stack.getItem() instanceof SignedCheckItem) {
                    CheckData cd = stack.get(FwDataComponents.CHECK_DATA.get());
                    if (cd != null && !cd.deposited()) {
                        futures.add(EconomyDatabaseManager.getInstance().registerCheck(
                                cd.checkId(), cd.issuerUuid(), cd.issuerName(), cd.payee(), cd.amountCents()
                        ));
                    }
                }
            }

            if (playerTrackedBalance > topOnlineBalance) {
                topOnlineBalance = playerTrackedBalance;
                topOnlineOwner = player.getName().getString();
            }
        }

        OnlineScanSummary summary = new OnlineScanSummary(
                walletsCount, walletsCents, bankCount, bankCents, physicalCashCents, topOnlineOwner, topOnlineBalance
        );

        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(summary);
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> summary);
    }

    private static CompletableFuture<Void> recordWallet(ItemStack stack, ServerPlayer player) {
        WalletData data = WalletItem.getOrCreateWalletData(stack, player);
        UUID ownerUuid = data.ownerUuid() != null ? data.ownerUuid() : player.getUUID();
        String ownerName = data.ownerName() != null && !data.ownerName().isEmpty() ? data.ownerName() : player.getName().getString();
        return EconomyDatabaseManager.getInstance().upsertWallet(data.walletId(), ownerUuid, ownerName, data.balanceCents());
    }

    private static CompletableFuture<Void> recordPda(ItemStack stack, PdaItem pdaItem, ServerPlayer player) {
        PdaData data = pdaItem.getOrCreatePdaData(stack, player);
        UUID ownerUuid = data.ownerUuid().orElse(player.getUUID());
        String ownerName = data.hasOwner() ? data.ownerName() : player.getName().getString();
        return EconomyDatabaseManager.getInstance().upsertBankAccount(data.pdaId(), ownerUuid, ownerName, data.bankBalanceCents());
    }
}
