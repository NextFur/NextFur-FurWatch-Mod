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

        if (!FMLEnvironment.dist.isDedicatedServer()) {
            LOGGER.info("Mundo singleplayer detectado, ignorando login...");
            ctx.finishCurrentTask(AuthTaskPayload.TYPE);
            return;
        }

        LOGGER.info("Requisicao de login recebida de " + packetUser+ "...");

        PlayerAuthenticator.authenticatePlayerAsync(packetUser, token)
                .thenAccept(isAuthenticated -> {
                    ctx.enqueueWork(() -> {
                        if (isAuthenticated) {
                            ctx.finishCurrentTask(AuthTaskPayload.TYPE);
                            LOGGER.info("Login efetuado com sucesso para o jogador " + packetUser);
                        } else {
                            ctx.disconnect(Component.literal(
                                    ChatFormatting.RED + "[FURSMP] Falha no login - Tente novamente.\n" +
                                            ChatFormatting.YELLOW + "! Caso este erro persista, abra um ticket em nosso Discord. !"
                            ));
                            LOGGER.info("Erro ao efetuar login para " + packetUser);
                        }
                    });
                });
    }
}
