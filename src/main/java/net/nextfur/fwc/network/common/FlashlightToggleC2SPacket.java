package net.nextfur.fwc.network.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.server.FlashlightServerState;

public class FlashlightToggleC2SPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "flashlight_toggle");
    public static final Type<FlashlightToggleC2SPacket> TYPE = new Type<>(ID);

    private final boolean enabled;

    public FlashlightToggleC2SPacket(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public static final StreamCodec<FriendlyByteBuf, FlashlightToggleC2SPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> buffer.writeBoolean(packet.enabled),
            buffer -> new FlashlightToggleC2SPacket(buffer.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FlashlightToggleC2SPacket packet, ServerPlayer sender) {
        FlashlightServerState.setPlayerState(sender.getUUID(), packet.enabled());
    }
}
