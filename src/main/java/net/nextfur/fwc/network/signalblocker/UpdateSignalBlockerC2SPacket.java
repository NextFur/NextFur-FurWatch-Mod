package net.nextfur.fwc.network.signalblocker;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.blocks.entity.SignalBlockerBlockEntity;

public record UpdateSignalBlockerC2SPacket(
        BlockPos pos,
        int radius,
        int redstoneMode,
        boolean manualEnabled
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "update_signal_blocker");
    public static final Type<UpdateSignalBlockerC2SPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, UpdateSignalBlockerC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                BlockPos.STREAM_CODEC.encode(buf, pkt.pos);
                ByteBufCodecs.VAR_INT.encode(buf, pkt.radius);
                ByteBufCodecs.VAR_INT.encode(buf, pkt.redstoneMode);
                ByteBufCodecs.BOOL.encode(buf, pkt.manualEnabled);
            },
            buf -> new UpdateSignalBlockerC2SPacket(
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpdateSignalBlockerC2SPacket packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }

        ctx.enqueueWork(() -> {
            if (!player.isCreative()) {
                return;
            }

            if (player.distanceToSqr(packet.pos().getCenter()) > 64.0) {
                return;
            }

            if (player.serverLevel().getBlockEntity(packet.pos()) instanceof SignalBlockerBlockEntity blocker) {
                blocker.setRadius(packet.radius());
                blocker.setRedstoneMode(packet.redstoneMode());
                blocker.setManualEnabled(packet.manualEnabled());
                blocker.updateBlockState();
                blocker.setChanged();
                player.serverLevel().sendBlockUpdated(packet.pos(), blocker.getBlockState(), blocker.getBlockState(), 3);

                player.displayClientMessage(
                        Component.literal("§a[FurWatch] Bloqueador configurado: Raio de " + blocker.getRadius() + " blocos."),
                        true
                );
                player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1.0f, 1.5f);
            }
        });
    }
}
