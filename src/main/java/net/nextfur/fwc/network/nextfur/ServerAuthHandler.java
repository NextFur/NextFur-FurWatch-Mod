package net.nextfur.fwc.network.nextfur;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.api.PlayerAuthenticator;
import net.nextfur.fwc.network.nextfur.packets.AuthResponsePacket;
import net.nextfur.fwc.network.nextfur.packets.AuthTaskPayload;

import static net.nextfur.fwc.FwMain.LOGGER;

public class ServerAuthHandler {
    public static void onAuthResponse(AuthResponsePacket packet, IPayloadContext ctx) {
        String packetUser = packet.getUsername();
        String token = packet.getToken();
        String userId = packet.getNextId();

        boolean DEBUG = true;

        if (!FMLEnvironment.dist.isDedicatedServer()) {
            LOGGER.info("Mundo singleplayer detectado, ignorando login...");
            ctx.finishCurrentTask(AuthTaskPayload.TYPE);
            return;
        }

        LOGGER.info("Requisicao de login recebida de " + packetUser+ "...");

        PlayerAuthenticator.authenticatePlayerAsync(packetUser, token)
                .thenAccept(statusCode -> {
                    ctx.enqueueWork(() -> {
                        int code = DEBUG ? 402 : statusCode;
                        if(code != 200) { // owo
                            ctx.disconnect(Component.literal(
                                    ChatFormatting.RED + "[FURSMP] Falha no login - tente novamente.\n" +
                                            ChatFormatting.YELLOW + "! Caso este erro persista, abra um ticket em nosso Discord. !\n\n"+
                                            ChatFormatting.GRAY + "0x" + code + "\n\n"+
                                            ChatFormatting.RED + "[ " + packetUser + " ] "+ChatFormatting.RESET + "-" + ChatFormatting.GOLD + " NextFurID: " + userId
                            ));
                            LOGGER.info("Erro ao efetuar login para " + packetUser);
                            return;
                        }

                        LOGGER.info("Login efetuado com sucesso para o jogador " + packetUser);
                        ctx.finishCurrentTask(AuthTaskPayload.TYPE);
                    });
                });
    }
}
