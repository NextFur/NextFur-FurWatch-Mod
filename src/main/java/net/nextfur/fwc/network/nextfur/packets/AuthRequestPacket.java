package net.nextfur.fwc.network.nextfur.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.network.nextfur.ClientAuthHandler;

public class AuthRequestPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "auth_request");
    public static final Type<AuthRequestPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, AuthRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {},
            buffer -> new AuthRequestPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AuthRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientAuthHandler.onAuthRequest(context));
    }
}
