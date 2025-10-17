package net.nextfur.fwc.network.world;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.util.world.SkyColorState;

public class SkyColorSyncPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "skycolor_sync");
    public static final Type<SkyColorSyncPacket> TYPE = new Type<>(ID);

    private final int fogcolor;
    private final int boxcolor;

    public SkyColorSyncPacket(int fogcolor, int boxcolor) {
        this.fogcolor = fogcolor;
        this.boxcolor = boxcolor;
    }

    public static final StreamCodec<FriendlyByteBuf, SkyColorSyncPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeInt(packet.fogcolor);
                buf.writeInt(packet.boxcolor);
            },
            buf -> new SkyColorSyncPacket(buf.readInt(), buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SkyColorSyncPacket packet) {
        Minecraft.getInstance().execute(() -> {
            SkyColorState.setFogColor(packet.fogcolor);
            SkyColorState.setBoxcolor(packet.boxcolor);
        });
    }
}
