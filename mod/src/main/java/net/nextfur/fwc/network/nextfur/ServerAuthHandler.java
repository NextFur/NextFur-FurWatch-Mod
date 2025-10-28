package net.nextfur.fwc.network.nextfur;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.api.PlayerAuthenticator;
import net.nextfur.fwc.network.nextfur.packets.AuthResponsePacket;
import net.nextfur.fwc.network.nextfur.packets.AuthTaskPayload;


public class ServerAuthHandler {
    public static void onAuthResponse(AuthResponsePacket packet, IPayloadContext ctx) {
        String packetUser = packet.getUsername();
        String token = packet.getToken();


        if (PlayerAuthenticator.authenticatePlayer(packetUser, token)) {
            ctx.finishCurrentTask(AuthTaskPayload.TYPE);
        } else {
            ctx.disconnect(Component.literal(
                    ChatFormatting.RED + "[FURSMP] Falha no login - Tente novamente." +
                            "\nCaso este erro persista, abra um ticket em nosso Discord."
            ));
        }
    }
}
