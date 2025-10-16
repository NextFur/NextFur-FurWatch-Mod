package net.nextfur.fwc.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.client.gui.TitleMenuScreen;

public class OpenTitleMenuPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "open_tmenu");
    public static final Type<OpenTitleMenuPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, OpenTitleMenuPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {},
            buffer -> new OpenTitleMenuPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(OpenTitleMenuPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.execute(() -> mc.setScreen(new TitleMenuScreen(Component.literal("FurWatch - Title ;3"))));
        }
    }
}
