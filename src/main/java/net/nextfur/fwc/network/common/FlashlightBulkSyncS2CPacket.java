package net.nextfur.fwc.network.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.util.client.PlayerComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlashlightBulkSyncS2CPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "flashlight_bulk_sync");
    public static final Type<FlashlightBulkSyncS2CPacket> TYPE = new Type<>(ID);

    private final Map<UUID, Boolean> states;

    public FlashlightBulkSyncS2CPacket(Map<UUID, Boolean> states) {
        this.states = new HashMap<>(states);
    }

    public static final StreamCodec<FriendlyByteBuf, FlashlightBulkSyncS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeInt(packet.states.size());
                packet.states.forEach((uuid, enabled) -> {
                    buffer.writeUtf(uuid.toString());
                    buffer.writeBoolean(enabled);
                });
            },
            buffer -> {
                int size = buffer.readInt();
                Map<UUID, Boolean> states = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    states.put(UUID.fromString(buffer.readUtf()), buffer.readBoolean());
                }
                return new FlashlightBulkSyncS2CPacket(states);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FlashlightBulkSyncS2CPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> PlayerComponent.applySnapshot(packet.states));
    }
}
