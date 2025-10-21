package net.nextfur.fwc.network.nextfur;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.FwMain;

public class ClientAuthPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "client_auth");
    public static final Type<ClientAuthPacket> TYPE = new Type<>(ID);

    private final String token;
    private final String username;

    public ClientAuthPacket(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public static final StreamCodec<FriendlyByteBuf, ClientAuthPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUtf(packet.token);
                buf.writeUtf(packet.username);
            },
            buf -> new ClientAuthPacket(
                    buf.readUtf(),
                    buf.readUtf()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public String getToken() {
        return this.token;
    }

    public String getUsername() {
        return this.username;
    }
}
