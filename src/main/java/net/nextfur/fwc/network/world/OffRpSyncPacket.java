package net.nextfur.fwc.network.world;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.FwMain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class OffRpSyncPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "offrp_sync");
    public static final Type<OffRpSyncPacket> TYPE = new Type<>(ID);

    private final List<UUID> offRpPlayers;

    public OffRpSyncPacket(Collection<UUID> offRpPlayers) {
        this.offRpPlayers = new ArrayList<>(offRpPlayers);
    }

    public List<UUID> getOffRpPlayers() {
        return this.offRpPlayers;
    }

    public static final StreamCodec<FriendlyByteBuf, OffRpSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.offRpPlayers.size());
                for(UUID player : packet.offRpPlayers) {
                    buf.writeUtf(player.toString());
                }
            },
            buf -> {
                List<UUID> offPlayers = new ArrayList<>();
                int size = buf.readInt();
                for(var x = 0 ; x < size ; x++) {
                    offPlayers.add(UUID.fromString(buf.readUtf()));
                }
                return new OffRpSyncPacket(offPlayers);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
