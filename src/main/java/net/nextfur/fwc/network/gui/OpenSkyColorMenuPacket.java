package net.nextfur.fwc.network.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.client.gui.SkyColorMenuScreen;

public class OpenSkyColorMenuPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "open_skymenu");
    public static final Type<OpenSkyColorMenuPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, OpenSkyColorMenuPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {},
            buffer -> new OpenSkyColorMenuPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(OpenSkyColorMenuPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.execute(() -> mc.setScreen(new SkyColorMenuScreen(Component.literal("FurWatch - SkyColor ;3"))));
        }
    }
}