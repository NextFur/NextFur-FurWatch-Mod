package net.nextfur.fwc.network.world;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
//import net.nextfur.fwc.client.audio.ClientSoundHandler;

public class SoundControlPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "sound_control");
    public static final Type<SoundControlPacket> TYPE = new Type<>(ID);

    public final String action;
    public final String url;
    public final float volume;
    public final int radius;
    public final int fadein;
    public final int fadeout;
    public final boolean loop;

    public SoundControlPacket(String action, String url, float volume, int radius, int fadein, int fadeout, boolean loop) {
        this.action = action;
        this.url = url;
        this.volume = volume;
        this.radius = radius;
        this.fadein = fadein;
        this.fadeout = fadeout;
        this.loop = loop;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, SoundControlPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                // Write (Sending, clientside)
                buf.writeUtf(packet.action);    // Action
                buf.writeUtf(packet.url);       // Sound URL
                buf.writeFloat(packet.volume);  // Volume
                buf.writeInt(packet.radius);    // Radius
                buf.writeInt(packet.fadein);    // FadeIn
                buf.writeInt(packet.fadeout);   // FadeOut
                buf.writeBoolean(packet.loop);  // Loop
            },
            buf -> new SoundControlPacket(
                    buf.readUtf(),      // Action
                    buf.readUtf(),      // Sound Url
                    buf.readFloat(),    // Volume
                    buf.readInt(),      // Radius
                    buf.readInt(),      // FadeIn
                    buf.readInt(),      // FadeOut
                    buf.readBoolean()   // Loop
            ) // Read (Receiving, serverside)
    );

    public static void handle(SoundControlPacket packet, final IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> {
                // TODO:
                //  ClientSoundHandler.handlePacket(packet);
            });
        } else if (context.flow().isServerbound()) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer sender) {
                    if (!sender.hasPermissions(2)) return;

                    ServerLevel level = sender.serverLevel();
                    PacketDistributor.sendToPlayersNear(
                            level,
                            null,
                            sender.getX(),
                            sender.getY(),
                            sender.getZ(),
                            packet.radius,
                            packet
                    );
                }
            });
        }
    }
}
