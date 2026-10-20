package net.nextfur.fwc.network.sanity;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.client.sanity.SanityClientHelper;

public record SanitySyncS2CPacket(
        float sanity,
        boolean triggerSpike
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "sanity_sync");
    public static final Type<SanitySyncS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, SanitySyncS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                ByteBufCodecs.FLOAT.encode(buf, pkt.sanity);
                ByteBufCodecs.BOOL.encode(buf, pkt.triggerSpike);
            },
            buf -> new SanitySyncS2CPacket(
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SanitySyncS2CPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> SanityClientHelper.updateSanity(packet.sanity(), packet.triggerSpike()));
    }
}
