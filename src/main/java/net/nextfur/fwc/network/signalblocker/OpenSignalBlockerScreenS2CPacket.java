package net.nextfur.fwc.network.signalblocker;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.client.gui.SignalBlockerClientHelper;

public record OpenSignalBlockerScreenS2CPacket(
        BlockPos pos,
        int radius,
        int redstoneMode,
        boolean manualEnabled,
        boolean active
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "open_signal_blocker_screen");
    public static final Type<OpenSignalBlockerScreenS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, OpenSignalBlockerScreenS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                BlockPos.STREAM_CODEC.encode(buf, pkt.pos);
                ByteBufCodecs.VAR_INT.encode(buf, pkt.radius);
                ByteBufCodecs.VAR_INT.encode(buf, pkt.redstoneMode);
                ByteBufCodecs.BOOL.encode(buf, pkt.manualEnabled);
                ByteBufCodecs.BOOL.encode(buf, pkt.active);
            },
            buf -> new OpenSignalBlockerScreenS2CPacket(
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenSignalBlockerScreenS2CPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> SignalBlockerClientHelper.openScreen(
                packet.pos(),
                packet.radius(),
                packet.redstoneMode(),
                packet.manualEnabled(),
                packet.active()
        ));
    }
}
