package net.nextfur.fwc.network.computer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.client.gui.ComputerClientHelper;

public record OpenComputerScreenS2CPacket(BlockPos pos, String url) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "open_computer_screen");
    public static final Type<OpenComputerScreenS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, OpenComputerScreenS2CPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenComputerScreenS2CPacket::pos,
            ByteBufCodecs.STRING_UTF8, OpenComputerScreenS2CPacket::url,
            OpenComputerScreenS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenComputerScreenS2CPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ComputerClientHelper.openScreen(packet.pos(), packet.url()));
    }
}
