package net.nextfur.fwc.network.world;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.util.world.SkyColorData;

public class SkyColorChangePacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "skycolor_change");
    public static final Type<SkyColorChangePacket> TYPE = new Type<>(ID);

    private final int fogcolor;
    private final int boxcolor;

    public SkyColorChangePacket(int fogcolor, int boxcolor) {
        this.fogcolor = fogcolor;
        this.boxcolor = boxcolor;
    }

    public static final StreamCodec<FriendlyByteBuf, SkyColorChangePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.fogcolor);
                buf.writeInt(packet.boxcolor);
            },
            buf -> new SkyColorChangePacket(buf.readInt(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SkyColorChangePacket packet, ServerPlayer sender) {
        if(!sender.hasPermissions(2)) return; // n sei, deu vontade de por isso aki

        SkyColorData.setCurrentFogColor(packet.fogcolor);
        SkyColorData.setCurrentBoxColor(packet.boxcolor);

        PacketDistributor.sendToAllPlayers(new SkyColorSyncPacket(packet.fogcolor, packet.boxcolor));
    }
}