package net.nextfur.fwc.network.computer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.blocks.entity.ComputerBlockEntity;

public record UpdateComputerUrlC2SPacket(BlockPos pos, String url) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "update_computer_url");
    public static final Type<UpdateComputerUrlC2SPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, UpdateComputerUrlC2SPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateComputerUrlC2SPacket::pos,
            ByteBufCodecs.STRING_UTF8, UpdateComputerUrlC2SPacket::url,
            UpdateComputerUrlC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateComputerUrlC2SPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                // Creative or Admin permission check
                if (!player.isCreative() && !player.hasPermissions(2)) {
                    return;
                }
                // Distance check
                if (player.distanceToSqr(packet.pos().getCenter()) > 64.0) {
                    return;
                }
                if (packet.url() == null || packet.url().length() > 4096) {
                    return;
                }
                if (player.serverLevel().getBlockEntity(packet.pos()) instanceof ComputerBlockEntity be) {
                    be.setUrl(packet.url());
                    be.setChanged();
                    player.serverLevel().sendBlockUpdated(packet.pos(), be.getBlockState(), be.getBlockState(), 3);
                }
            }
        });
    }
}
