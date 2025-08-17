package net.nextfur.fwc.network;

import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.server.ServerLoader;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext; 

public class ClientAuthPacket {
    private final String token;

    public ClientAuthPacket(String token) {
        this.token = token;
    }

    public ClientAuthPacket(FriendlyByteBuf buf) {
        this.token = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.token);
    }

    public void handle(final PlayPayloadContext context) {
        context.workHandler().execute(() -> {
            context.player().ifPresent(player -> {
                String username = player.getName().getString();
                FwMain.LOGGER.info("[FURSMP] Received auth token packet from user: " + username);

                ServerLoader.pendingTokens.put(username, this.token);
            });
        });
    }
}