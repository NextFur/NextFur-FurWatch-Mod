package net.nextfur.fwc.network.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.util.client.PlayerComponent;

import java.util.UUID;

public class FlashlightStateS2CPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "flashlight_state");
    public static final Type<FlashlightStateS2CPacket> TYPE = new Type<>(ID);

    private final UUID playerId;
    private final boolean enabled;

    public FlashlightStateS2CPacket(UUID playerId, boolean enabled) {
        this.playerId = playerId;
        this.enabled = enabled;
    }

    public static final StreamCodec<FriendlyByteBuf, FlashlightStateS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeUtf(packet.playerId.toString());
                buffer.writeBoolean(packet.enabled);
            },
            buffer -> new FlashlightStateS2CPacket(UUID.fromString(buffer.readUtf()), buffer.readBoolean())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FlashlightStateS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> PlayerComponent.setPlayerFlashlight(packet.playerId, packet.enabled));
    }
}
