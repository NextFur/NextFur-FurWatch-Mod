package net.nextfur.fwc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.nextfur.fwc.pda.service.PdaMessagingService;
import net.nextfur.fwc.pda.service.PdaMessagingService.PdaLookupResult;

public class PdaTellCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> tellNode = dispatcher.register(Commands.literal("tell")
                .executes(PdaTellCommand::sendUsage)
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(PdaTellCommand::sendUsage)
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(PdaTellCommand::execute)
                        )
                )
        );

        // Aliases /msg and /w redirect to /tell
        dispatcher.register(Commands.literal("msg").redirect(tellNode));
        dispatcher.register(Commands.literal("w").redirect(tellNode));
    }

    private static int sendUsage(CommandContext<CommandSourceStack> context) {
        context.getSource().sendFailure(Component.literal("§8[§b📱 FurWatch PDA§8] §cUso correto: /tell <jogador> <mensagem>"));
        return 0;
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("§8[§b📱 FurWatch PDA§8] §cSomente jogadores podem usar este comando."));
            return 0;
        }

        ServerPlayer recipient = EntityArgument.getPlayer(context, "target");
        String message = StringArgumentType.getString(context, "message").trim();

        if (message.isEmpty()) {
            player.sendSystemMessage(Component.literal("§8[§b📱 FurWatch PDA§8] §cA mensagem não pode estar vazia."));
            return 0;
        }

        if (recipient.getUUID().equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("§8[§b📱 FurWatch PDA§8] §cVocê não pode enviar uma mensagem para si mesmo."));
            return 0;
        }

        PdaLookupResult senderPda = PdaMessagingService.findSenderPda(player);
        if (!senderPda.foundPda()) {
            player.sendSystemMessage(Component.literal("§8[§b📱 FurWatch PDA§8] §cVocê precisa ter um PDA em seu inventário para usar o /tell."));
            return 0;
        }

        if (!senderPda.hasOwner()) {
            player.sendSystemMessage(Component.literal("§8[§b📱 FurWatch PDA§8] §cO PDA em seu inventário não possui um usuário registrado. Vincule-o antes de usar."));
            return 0;
        }

        if (senderPda.data().ownerUuid().isPresent() && senderPda.data().ownerUuid().get().equals(recipient.getUUID())) {
            player.sendSystemMessage(Component.literal("§8[§b📱 FurWatch PDA§8] §cVocê não pode enviar uma mensagem para o próprio proprietário deste PDA."));
            return 0;
        }

        PdaLookupResult recipientPda = PdaMessagingService.findRecipientPda(recipient);
        if (!recipientPda.foundPda() || !recipientPda.hasOwner()) {
            player.sendSystemMessage(Component.literal("§8[§b📱 FurWatch PDA§8] §cO jogador §e" + recipient.getName().getString() + " §cnão possui um PDA registrado em seu inventário."));
            return 0;
        }

        PdaMessagingService.sendPdaMessage(
                player,
                senderPda.stack(),
                senderPda.data(),
                recipient,
                recipientPda.stack(),
                recipientPda.data(),
                message
        );

        return 1;
    }
}
