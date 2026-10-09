package net.nextfur.fwc.network.economy;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.FwMain;

public record AtmSyncS2CPacket(long newBalanceCents) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "atm_sync");
    public static final Type<AtmSyncS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, AtmSyncS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, AtmSyncS2CPacket::newBalanceCents,
            AtmSyncS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AtmSyncS2CPacket packet, net.neoforged.neoforge.network.handling.IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (net.minecraft.client.Minecraft.getInstance().screen instanceof net.nextfur.fwc.client.gui.AtmScreen screen) {
                screen.onSyncReceived(packet.newBalanceCents());
            }
        });
    }
}
