package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.nextfur.fwc.init.FwPermissions;
import net.nextfur.fwc.sanity.SanityData;
import net.nextfur.fwc.sanity.SanityService;

import java.util.Locale;

public class SanityCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(buildCommand("sanity"));
        dispatcher.register(buildCommand("sanidade"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCommand(String literal) {
        return Commands.literal(literal)
                .requires(FwPermissions::hasAdminPermission)
                .executes(ctx -> {
                    if (ctx.getSource().getEntity() instanceof ServerPlayer self) {
                        return inspectSanity(ctx.getSource(), self);
                    }
                    ctx.getSource().sendFailure(Component.literal("§cEspecifique um jogador: /" + literal + " get <player>"));
                    return 0;
                })
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> inspectSanity(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                        )
                )
                .then(Commands.literal("check")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> inspectSanity(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                        )
                )
                .then(Commands.literal("info")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> inspectSanity(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                        )
                )
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0.0f, 100.0f))
                                        .executes(ctx -> modifySanity(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                FloatArgumentType.getFloat(ctx, "amount"),
                                                0
                                        ))
                                )
                        )
                )
                .then(Commands.literal("add")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0.01f, 100.0f))
                                        .executes(ctx -> modifySanity(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                FloatArgumentType.getFloat(ctx, "amount"),
                                                1
                                        ))
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0.01f, 100.0f))
                                        .executes(ctx -> modifySanity(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                FloatArgumentType.getFloat(ctx, "amount"),
                                                2
                                        ))
                                )
                        )
                )
                .then(Commands.literal("reset")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> resetSanity(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                        )
                );
    }

    private static int modifySanity(CommandSourceStack source, ServerPlayer target, float amount, int mode) {
        SanityData current = SanityService.getSanityData(target);
        float newSanity = switch (mode) {
            case 0 -> Mth.clamp(amount, SanityData.MIN_SANITY, SanityData.MAX_SANITY);
            case 1 -> Mth.clamp(current.sanity() + amount, SanityData.MIN_SANITY, SanityData.MAX_SANITY);
            case 2 -> Mth.clamp(current.sanity() - amount, SanityData.MIN_SANITY, SanityData.MAX_SANITY);
            default -> current.sanity();
        };

        boolean triggerSpike = newSanity < current.sanity() && newSanity < SanityService.LOW_SANITY_START;
        SanityData updated = current.withSanity(newSanity);
        SanityService.setSanityData(target, updated, triggerSpike);

        String formattedSanity = formatSanityColor(newSanity) + String.format(Locale.ROOT, "%.2f%%", newSanity);
        source.sendSuccess(() -> Component.literal("§6[FurWatch] §aSanidade de §e" + target.getName().getString()
                + " §aatualizada para " + formattedSanity + "§a."), true);
        return 1;
    }

    private static int resetSanity(CommandSourceStack source, ServerPlayer target) {
        SanityService.clearSanityDebuffs(target);
        SanityService.setSanityData(target, SanityData.DEFAULT, false);

        source.sendSuccess(() -> Component.literal("§6[FurWatch] §aSanidade e histórico de §e" + target.getName().getString()
                + " §aforam resetados para §a§l100.00%§a."), true);
        return 1;
    }

    private static int inspectSanity(CommandSourceStack source, ServerPlayer target) {
        long now = System.currentTimeMillis();
        SanityData data = SanityService.getSanityData(target);
        float sanity = data.sanity();
        float intensity = SanityService.getInsanityIntensity(sanity);

        int pKills10m = SanityService.countWithinWindow(data.playerKillTimestamps(), now, SanityService.WINDOW_10_MIN_MS);
        int pKills30m = SanityService.countWithinWindow(data.playerKillTimestamps(), now, SanityService.WINDOW_30_MIN_MS);
        int pKills60m = SanityService.countWithinWindow(data.playerKillTimestamps(), now, SanityService.WINDOW_60_MIN_MS);

        int aKills10m = SanityService.countWithinWindow(data.animalKillTimestamps(), now, SanityService.WINDOW_10_MIN_MS);
        int aKills30m = SanityService.countWithinWindow(data.animalKillTimestamps(), now, SanityService.WINDOW_30_MIN_MS);
        int aKills60m = SanityService.countWithinWindow(data.animalKillTimestamps(), now, SanityService.WINDOW_60_MIN_MS);

        float nextPlayerPenalty = SanityService.previewNextPlayerKillPenalty(data, now);
        float nextAnimalPenalty = SanityService.previewNextAnimalKillPenalty(data, now);

        String stateLabel = getPsychologicalStateLabel(sanity);
        String sanityStr = formatSanityColor(sanity) + String.format(Locale.ROOT, "%.2f%%", sanity);
        String activityStr = String.format(Locale.ROOT, "%.1f%%", intensity * 100.0f);

        source.sendSuccess(() -> Component.literal("§8====== §6§lFurWatch Sanidade: §e" + target.getName().getString() + " §8======"), false);
        source.sendSuccess(() -> Component.literal("§7Nível de Sanidade: " + sanityStr + " §8(" + stateLabel + "§8)"), false);
        source.sendSuccess(() -> Component.literal("§7Atividade de Insanidade (35-10%): §d" + activityStr), false);
        source.sendSuccess(() -> Component.literal("§7Abates de Jogadores (10m/30m/1h): §f"
                + pKills10m + " §8/ §f" + pKills30m + " §8/ §f" + pKills60m
                + " §8(Próx. penalidade: §c-" + String.format(Locale.ROOT, "%.2f%%", nextPlayerPenalty) + "§8)"), false);
        source.sendSuccess(() -> Component.literal("§7Abates de Animais (10m/30m/1h): §f"
                + aKills10m + " §8/ §f" + aKills30m + " §8/ §f" + aKills60m
                + " §8(Próx. penalidade: §c-" + String.format(Locale.ROOT, "%.2f%%", nextAnimalPenalty) + "§8)"), false);
        source.sendSuccess(() -> Component.literal("§8=========================================="), false);
        return 1;
    }

    private static String formatSanityColor(float sanity) {
        if (sanity > 65.0f) return "§a";
        if (sanity > 35.0f) return "§e";
        if (sanity > 20.0f) return "§6";
        if (sanity > 10.0f) return "§c";
        return "§4§l";
    }

    private static String getPsychologicalStateLabel(float sanity) {
        if (sanity > 65.0f) return "§aEstável";
        if (sanity > 35.0f) return "§eInquieto";
        if (sanity > 20.0f) return "§6Perturbado";
        if (sanity > 10.0f) return "§cInsano";
        return "§4§lColapso Mental";
    }
}
