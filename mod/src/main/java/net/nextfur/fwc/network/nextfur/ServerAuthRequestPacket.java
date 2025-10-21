package net.nextfur.fwc.network.nextfur;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.Config;
import net.nextfur.fwc.FwMain;

public class ServerAuthRequestPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "server_auth_request");
    public static final CustomPacketPayload.Type<ServerAuthRequestPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ServerAuthRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new ServerAuthRequestPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(ServerAuthRequestPacket packet) {
        Minecraft mc = Minecraft.getInstance();

        PacketDistributor.sendToServer(new ClientAuthPacket(
                Config.getAuthToken(),
                mc.getUser().getName()
        ));
    }
}
