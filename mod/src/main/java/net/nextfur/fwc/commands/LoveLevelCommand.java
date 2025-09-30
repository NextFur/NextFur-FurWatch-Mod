package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.nextfur.fwc.Config;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.util.FurWatchApiClient;

public class LoveLevelCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lovelevel")
            .then(Commands.literal("set")
                .then(Commands.argument("level", IntegerArgumentType.integer(0, 100))
                    .executes(context -> setLoveLevel(context, IntegerArgumentType.getInteger(context, "level")))))
            .then(Commands.literal("add")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                    .executes(context -> addLoveLevel(context, IntegerArgumentType.getInteger(context, "amount")))))
            .then(Commands.literal("subtract")
                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                    .executes(context -> subtractLoveLevel(context, IntegerArgumentType.getInteger(context, "amount")))))
            .then(Commands.literal("get")
                .executes(context -> getLoveLevel(context))));
    }

    private static int setLoveLevel(CommandContext<CommandSourceStack> context, int level) {
        if (!Config.enableLoginSystem) {
            context.getSource().sendFailure(Component.literal("§cLogin system is disabled. Cannot use love level commands."));
            return 0;
        }

        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("§cThis command can only be used by players."));
            return 0;
        }

        String username = player.getName().getString();
        context.getSource().sendSuccess(() -> Component.literal("§7Setting love level to " + level + "..."), false);

        FurWatchApiClient.setLoveLevel(username, level).thenAccept(response -> {
            if (response.isSuccess()) {
                context.getSource().sendSuccess(() -> Component.literal("§a✓ Love level set to " + response.getLoveLevel() + " successfully!"), false);
                if (Config.debugMode) {
                    FwMain.LOGGER.info("[FURSMP] Love level set for {}: {}", username, response.getLoveLevel());
                }
            } else {
                context.getSource().sendFailure(Component.literal("§c✗ Failed to set love level: " + response.getMessage()));
                FwMain.LOGGER.error("[FURSMP] Failed to set love level for {}: {}", username, response.getMessage());
            }
        }).exceptionally(throwable -> {
            context.getSource().sendFailure(Component.literal("§c✗ Error occurred while setting love level."));
            FwMain.LOGGER.error("[FURSMP] Exception while setting love level for {}: ", username, throwable);
            return null;
        });

        return 1;
    }

    private static int addLoveLevel(CommandContext<CommandSourceStack> context, int amount) {
        if (!Config.enableLoginSystem) {
            context.getSource().sendFailure(Component.literal("§cLogin system is disabled. Cannot use love level commands."));
            return 0;
        }

        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("§cThis command can only be used by players."));
            return 0;
        }

        String username = player.getName().getString();
        context.getSource().sendSuccess(() -> Component.literal("§7Adding " + amount + " to love level..."), false);

        FurWatchApiClient.addLoveLevel(username, amount).thenAccept(response -> {
            if (response.isSuccess()) {
                context.getSource().sendSuccess(() -> Component.literal("§a✓ Added " + amount + " to love level! Current level: " + response.getLoveLevel()), false);
                if (Config.debugMode) {
                    FwMain.LOGGER.info("[FURSMP] Love level increased for {}: {}", username, response.getLoveLevel());
                }
            } else {
                context.getSource().sendFailure(Component.literal("§c✗ Failed to add to love level: " + response.getMessage()));
                FwMain.LOGGER.error("[FURSMP] Failed to add to love level for {}: {}", username, response.getMessage());
            }
        }).exceptionally(throwable -> {
            context.getSource().sendFailure(Component.literal("§c✗ Error occurred while adding to love level."));
            FwMain.LOGGER.error("[FURSMP] Exception while adding to love level for {}: ", username, throwable);
            return null;
        });

        return 1;
    }

    private static int subtractLoveLevel(CommandContext<CommandSourceStack> context, int amount) {
        if (!Config.enableLoginSystem) {
            context.getSource().sendFailure(Component.literal("§c[FURSMP] O Sistema de login está desligado. Não é possível usar comandos de nível de amor."));
            return 0;
        }

        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("§c[FURSMP] Esse comando só pode ser usado por jogadores, no client-side."));
            return 0;
        }

        String username = player.getName().getString();
        context.getSource().sendSuccess(() -> Component.literal("§7[FURSMP] Removendo " + amount + " do nível de amor..."), false);

        FurWatchApiClient.subtractLoveLevel(username, amount).thenAccept(response -> {
            if (response.isSuccess()) {
                context.getSource().sendSuccess(() -> Component.literal("§a[FURSMP] Removido " + amount + " do nível de amor! Nível atual: " + response.getLoveLevel()), false);
                if (Config.debugMode) {
                    FwMain.LOGGER.info("[FURSMP] Love level decreased for {}: {}", username, response.getLoveLevel());
                }
            } else {
                context.getSource().sendFailure(Component.literal("§c[FURSMP] Falha ao remover do nível de amor: " + response.getMessage()));
                FwMain.LOGGER.error("[FURSMP] Failed to subtract from love level for {}: {}", username, response.getMessage());
            }
        }).exceptionally(throwable -> {
            context.getSource().sendFailure(Component.literal("§c[FURSMP] Ocorreu um erro ao remover do nível de amor."));
            FwMain.LOGGER.error("[FURSMP] Exception while subtracting from love level for {}: ", username, throwable);
            return null;
        });

        return 1;
    }

    private static int getLoveLevel(CommandContext<CommandSourceStack> context) {
        if (!Config.enableLoginSystem) {
            context.getSource().sendFailure(Component.literal("§c[FURSMP] O Sistema de login está desligado. Não é possível usar comandos de nível de amor."));
            return 0;
        }

        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("§c[FURSMP] Esse comando só pode ser usado por jogadores."));
            return 0;
        }

        String username = player.getName().getString();
        context.getSource().sendSuccess(() -> Component.literal("§7[FURSMP] Recuperando nível de amor..."), false);

        FurWatchApiClient.getLoveLevel(username).thenAccept(response -> {
            if (response.isSuccess()) {
                context.getSource().sendSuccess(() -> Component.literal("§a Seu nível de amor atual é: " + response.getLoveLevel()), false);
                if (Config.debugMode) {
                    FwMain.LOGGER.info("[FURSMP] Love level retrieved for {}: {}", username, response.getLoveLevel());
                }
            } else {
                context.getSource().sendFailure(Component.literal("§c[FURSMP] Falha ao recuperar nível de amor: " + response.getMessage()));
                FwMain.LOGGER.error("[FURSMP] Failed to get love level for {}: {}", username, response.getMessage());
            }
        }).exceptionally(throwable -> {
            context.getSource().sendFailure(Component.literal("§c[FURSMP] Ocorreu um erro ao recuperar nível de amor."));
            FwMain.LOGGER.error("[FURSMP] Exception while getting love level for {}: ", username, throwable);
            return null;
        });

        return 1;
    }
}
