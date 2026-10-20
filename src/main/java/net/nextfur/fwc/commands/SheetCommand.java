package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.init.FwPermissions;
import net.nextfur.fwc.network.persona.OpenPersonaSheetS2CPacket;
import net.nextfur.fwc.persona.PersonaAttribute;
import net.nextfur.fwc.persona.PersonaData;
import net.nextfur.fwc.persona.PersonaService;

import java.util.Arrays;

public class SheetCommand {
    private static final SuggestionProvider<CommandSourceStack> ATTRIBUTE_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(
                    Arrays.stream(PersonaAttribute.values()).map(PersonaAttribute::getId),
                    builder
            );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = Commands.literal("sheet")
                .executes(ctx -> openOwnSheet(ctx.getSource()))
                .then(Commands.literal("admin")
                        .requires(FwPermissions::hasAdminPermission)
                        .then(Commands.literal("open")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> adminOpenSheet(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                                )
                        )
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> adminSetLock(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), false))
                                )
                        )
                        .then(Commands.literal("lock")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> adminSetLock(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), true))
                                )
                        )
                        .then(Commands.literal("give")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("points", IntegerArgumentType.integer(1, 100))
                                                .executes(ctx -> adminModifyPoints(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "points"),
                                                        0
                                                ))
                                        )
                                )
                        )
                        .then(Commands.literal("take")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("points", IntegerArgumentType.integer(1, 100))
                                                .executes(ctx -> adminModifyPoints(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "points"),
                                                        1
                                                ))
                                        )
                                )
                        )
                        .then(Commands.literal("points")
                                .then(Commands.literal("add")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("points", IntegerArgumentType.integer(1, 100))
                                                        .executes(ctx -> adminModifyPoints(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                IntegerArgumentType.getInteger(ctx, "points"),
                                                                0
                                                        ))
                                                )
                                        )
                                )
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("points", IntegerArgumentType.integer(1, 100))
                                                        .executes(ctx -> adminModifyPoints(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                IntegerArgumentType.getInteger(ctx, "points"),
                                                                1
                                                        ))
                                                )
                                        )
                                )
                                .then(Commands.literal("set")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .then(Commands.argument("points", IntegerArgumentType.integer(0, 100))
                                                        .executes(ctx -> adminModifyPoints(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                IntegerArgumentType.getInteger(ctx, "points"),
                                                                2
                                                        ))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("attribute", StringArgumentType.word())
                                                .suggests(ATTRIBUTE_SUGGESTIONS)
                                                .then(Commands.argument("level", IntegerArgumentType.integer(-3, 3))
                                                        .executes(ctx -> adminSetAttribute(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                StringArgumentType.getString(ctx, "attribute"),
                                                                IntegerArgumentType.getInteger(ctx, "level")
                                                        ))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("reset")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> adminResetSheet(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                                )
                        )
                        .then(Commands.literal("info")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> adminInspectSheet(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                                )
                        )
                        .then(Commands.literal("check")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> adminInspectSheet(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))
                                )
                        )
                );

        dispatcher.register(root);
    }

    private static int openOwnSheet(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Somente jogadores podem abrir a ficha de Persona."));
            return 0;
        }

        PersonaData data = PersonaService.getPersonaData(player);
        PacketDistributor.sendToPlayer(player, new OpenPersonaSheetS2CPacket(
                player.getUUID(),
                player.getName().getString(),
                data,
                false
        ));
        return 1;
    }

    private static int adminOpenSheet(CommandSourceStack source, ServerPlayer target) {
        if (!(source.getEntity() instanceof ServerPlayer admin)) {
            source.sendFailure(Component.literal("Somente jogadores podem abrir a GUI de administração."));
            return 0;
        }

        PersonaData data = PersonaService.getPersonaData(target);
        PacketDistributor.sendToPlayer(admin, new OpenPersonaSheetS2CPacket(
                target.getUUID(),
                target.getName().getString(),
                data,
                true
        ));
        return 1;
    }

    private static int adminSetLock(CommandSourceStack source, ServerPlayer target, boolean locked) {
        PersonaData current = PersonaService.getPersonaData(target);
        PersonaData updated = current.withLocked(locked);
        PersonaService.setPersonaData(target, updated);

        if (!locked) {
            source.sendSuccess(() -> Component.literal("§6[FurWatch] §aFicha de §e" + target.getName().getString() +
                    " §afoi §lDESBLOQUEADA§a."), true);
            target.sendSystemMessage(Component.literal("§6[FurWatch] §aSua ficha foi desbloqueada pela Staff! Use §e/sheet §apara configurar."));
        } else {
            source.sendSuccess(() -> Component.literal("§6[FurWatch] §eFicha de §f" + target.getName().getString() +
                    " §efoi §c§lBLOQUEADA§e."), true);
            target.sendSystemMessage(Component.literal("§6[FurWatch] §eSua ficha foi bloqueada pela Staff."));
        }
        return 1;
    }

    private static int adminModifyPoints(CommandSourceStack source, ServerPlayer target, int amount, int mode) {
        PersonaData current = PersonaService.getPersonaData(target);
        int newPoints = switch (mode) {
            case 0 -> current.availablePoints() + amount;
            case 1 -> Math.max(0, current.availablePoints() - amount);
            case 2 -> Math.max(0, amount);
            default -> current.availablePoints();
        };

        PersonaData updated = current.withAvailablePoints(newPoints);
        PersonaService.setPersonaData(target, updated);

        source.sendSuccess(() -> Component.literal("§6[FurWatch] §aPontos de §e" + target.getName().getString() +
                " §aatualizados para: §6§l" + newPoints), true);
        target.sendSystemMessage(Component.literal("§6[FurWatch] §eSeus pontos disponíveis foram alterados para: §6§l" + newPoints));
        return 1;
    }

    private static int adminSetAttribute(CommandSourceStack source, ServerPlayer target, String attrName, int level) {
        PersonaAttribute attr = PersonaAttribute.byName(attrName);
        if (attr == null) {
            source.sendFailure(Component.literal("§cAtributo inválido: '" + attrName + "'."));
            return 0;
        }

        if (level < attr.getMinLevel() || level > attr.getMaxLevel()) {
            source.sendFailure(Component.literal("§cO nível de " + attr.getDisplayName() + " deve estar entre " +
                    attr.getMinLevel() + " e +" + attr.getMaxLevel() + "."));
            return 0;
        }

        PersonaData current = PersonaService.getPersonaData(target);
        PersonaData updated = current.withAttribute(attr, level);
        PersonaService.setPersonaData(target, updated);

        String lvlStr = level > 0 ? "+" + level : String.valueOf(level);
        source.sendSuccess(() -> Component.literal("§6[FurWatch] §aAtributo §e" + attr.getDisplayName() +
                " §ade §f" + target.getName().getString() + " §adefinido para §b" + lvlStr), true);
        target.sendSystemMessage(Component.literal("§6[FurWatch] §eSeu atributo §6" + attr.getDisplayName() +
                " §efoi alterado para §b" + lvlStr));
        return 1;
    }

    private static int adminResetSheet(CommandSourceStack source, ServerPlayer target) {
        PersonaService.setPersonaData(target, PersonaData.DEFAULT);
        source.sendSuccess(() -> Component.literal("§6[FurWatch] §aFicha de §e" + target.getName().getString() +
                " §afoi resetada."), true);
        target.sendSystemMessage(Component.literal("§6[FurWatch] §eSua ficha foi resetada pela Staff. Use §a/sheet §epara reconfigurar."));
        return 1;
    }

    private static int adminInspectSheet(CommandSourceStack source, ServerPlayer target) {
        PersonaData data = PersonaService.getPersonaData(target);
        source.sendSuccess(() -> Component.literal("§8====== §6§lFurWatch Persona: §e" + target.getName().getString() + " §8======"), false);
        source.sendSuccess(() -> Component.literal("§7Status: " + (data.locked() ? "§cBloqueada" : "§aDesbloqueada")), false);
        source.sendSuccess(() -> Component.literal("§7Pontos Disponíveis: §6§l" + data.availablePoints() +
                " §8| §7Pontos Gastos: §e" + data.getSpentPoints()), false);

        for (PersonaAttribute attr : PersonaAttribute.values()) {
            int lvl = data.getLevel(attr);
            String lvlStr = lvl > 0 ? "§a+" + lvl : (lvl < 0 ? "§c" + lvl : "§70");
            source.sendSuccess(() -> Component.literal(" §8- §f" + attr.getDisplayName() + ": " + lvlStr), false);
        }
        source.sendSuccess(() -> Component.literal("§8=========================================="), false);
        return 1;
    }
}
