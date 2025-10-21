package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.nextfur.fwc.CommonConfig;
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
                .then(Commands.argument("username", StringArgumentType.string())
                    .executes(context -> getLoveLevel(context, StringArgumentType.getString(context, "username"))))));
    }

    private static int setLoveLevel(CommandContext<CommandSourceStack> context, int level) {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("§c[FURSMP] Esse comando só pode ser usado por jogadores, no client-side."));
            return 0;
        }

        String username = player.getName().getString();
        context.getSource().sendSuccess(() -> Component.literal("§7[FURSMP] Definindo " + level + "..."), false);

        FurWatchApiClient.setLoveLevel(username, level).thenAccept(response -> {
            if (response.isSuccess()) {
                context.getSource().sendSuccess(() -> Component.literal("§a[FURSMP] Love level set to " + response.getLoveLevel() + " successfully!"), false);
                if (CommonConfig.debugMode) {
                    FwMain.LOGGER.info("[FURSMP] Love level set for {}: {}", username, response.getLoveLevel());
                }
            } else {
                context.getSource().sendFailure(Component.literal("§c[FURSMP] Failed to set love level: " + response.getMessage()));
                FwMain.LOGGER.error("[FURSMP] Failed to set love level for {}: {}", username, response.getMessage());
            }
        }).exceptionally(throwable -> {
            context.getSource().sendFailure(Component.literal("§c[FURSMP] Ocorreu um erro ao definir o nível de amor."));
            FwMain.LOGGER.error("[FURSMP] Exception while setting love level for {}: ", username, throwable);
            return null;
        });

        return 1;
    }

    private static int addLoveLevel(CommandContext<CommandSourceStack> context, int amount) {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("§c[FURSMP] Esse comando só pode ser executado por jogadores."));
            return 0;
        }

        String username = player.getName().getString();
        context.getSource().sendSuccess(() -> Component.literal("§7[FURSMP] Adicionando " + amount + " ao nível de amor..."), false);

        FurWatchApiClient.addLoveLevel(username, amount).thenAccept(response -> {
            if (response.isSuccess()) {
                context.getSource().sendSuccess(() -> Component.literal("§a[FURSMP] Adicionado " + amount + " ao nível de amor! Nível atual: " + response.getLoveLevel()), false);
                if (CommonConfig.debugMode) {
                    FwMain.LOGGER.info("[FURSMP] Love level increased for {}: {}", username, response.getLoveLevel());
                }
            } else {
                context.getSource().sendFailure(Component.literal("§c[FURSMP] Falha ao adicionar ao nível de amor: " + response.getMessage()));
                FwMain.LOGGER.error("[FURSMP] Failed to add to love level for {}: {}", username, response.getMessage());
            }
        }).exceptionally(throwable -> {
            context.getSource().sendFailure(Component.literal("§c[FURSMP] Ocorreu um erro ao adicionar ao nível de amor."));
            FwMain.LOGGER.error("[FURSMP] Exception while adding to love level for {}: ", username, throwable);
            return null;
        });

        return 1;
    }

    private static int subtractLoveLevel(CommandContext<CommandSourceStack> context, int amount) {
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
                if (CommonConfig.debugMode) {
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

    private static int getLoveLevel(CommandContext<CommandSourceStack> context, String username) {
        Player player = context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("§c[FURSMP] Esse comando só pode ser usado por jogadores."));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.literal("§7[FURSMP] Recuperando nível de amor..."), false);

        FurWatchApiClient.getLoveLevel(username).thenAccept(response -> {
            if (response.isSuccess()) {
                context.getSource().sendSuccess(() -> Component.literal("§a [FURSMP] O Nível de Amor para " + username + " é: " + response.getLoveLevel()), false);
                if (CommonConfig.debugMode) {
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
