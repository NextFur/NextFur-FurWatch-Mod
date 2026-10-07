package net.nextfur.fwc.pda.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.pda.client.PdaClientHelper;
import net.nextfur.fwc.pda.data.PdaData;

public record SyncPdaDataS2CPacket(PdaData data) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "sync_pda_data");
    public static final Type<SyncPdaDataS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, SyncPdaDataS2CPacket> STREAM_CODEC = StreamCodec.composite(
            PdaData.STREAM_CODEC, SyncPdaDataS2CPacket::data,
            SyncPdaDataS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPdaDataS2CPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> PdaClientHelper.syncPdaData(packet.data()));
    }
}
