package net.nextfur.fwc.network;

import net.nextfur.fwc.FwMain;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ClientAuthPacket implements CustomPacketPayload {

    public static final Type<ClientAuthPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "client_auth"));

    public static final StreamCodec<FriendlyByteBuf, ClientAuthPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> packet.write(buffer), 
            ClientAuthPacket::new                       
    );

    private final String token;

    public ClientAuthPacket(String token) {
        this.token = token;
    }

    private ClientAuthPacket(FriendlyByteBuf buf) {
        this.token = buf.readUtf();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.token);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public String getToken() {
        return this.token;
    }
}