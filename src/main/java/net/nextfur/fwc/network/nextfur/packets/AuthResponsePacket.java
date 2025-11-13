package net.nextfur.fwc.network.nextfur.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.network.nextfur.ServerAuthHandler;

public class AuthResponsePacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "auth_response");
    public static final Type<AuthResponsePacket> TYPE = new Type<>(ID);

    private final String username;
    private final String token;
    private final String nextId;

    public AuthResponsePacket(String username, String token, String nextId) {
        this.username = username;
        this.token = token;
        this.nextId = nextId;
    }

    public static final StreamCodec<FriendlyByteBuf, AuthResponsePacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeUtf(packet.username);
                buffer.writeUtf(packet.token);
                buffer.writeUtf(packet.nextId);
            },
            buffer -> new AuthResponsePacket(buffer.readUtf(), buffer.readUtf(), buffer.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public String getUsername() {
        return this.username;
    }

    public String getToken() {
        return this.token;
    }

    public String getNextId() { return this.nextId; }

    public static void handle(AuthResponsePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ServerAuthHandler.onAuthResponse(packet, context));
    }
}
