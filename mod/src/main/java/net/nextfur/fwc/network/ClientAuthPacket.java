package net.nextfur.fwc.network;

import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.server.ServerLoader;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public class ClientAuthPacket {
    private final String token;

    public ClientAuthPacket(String token) {
        this.token = token;
    }

    public ClientAuthPacket(FriendltByteBuf buf) {
        this.token = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.token);
    }

    public void handle(NetworkEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            String username = ctx.getSender().getName().getString();
            FwMain.LOGGER.info("[FURSMP] Client Auth Packet received for user: " + username);

            ServerLoader.authenticatePlayer(username, this.token);
        })

        ctx.setPacketHandled(true);
    }
}