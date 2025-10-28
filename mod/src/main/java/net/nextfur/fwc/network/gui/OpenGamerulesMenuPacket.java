package net.nextfur.fwc.network.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.FwMain;
//import net.nextfur.fwc.client.gui.GameruleMenuScreen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class OpenGamerulesMenuPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "open_debugmenu");
    public static final CustomPacketPayload.Type<OpenGamerulesMenuPacket> TYPE = new CustomPacketPayload.Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, OpenGamerulesMenuPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {},
            buffer -> new OpenGamerulesMenuPacket()
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    @OnlyIn(Dist.CLIENT)
    public static void handle(OpenGamerulesMenuPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal(ChatFormatting.RED + "" + ChatFormatting.BOLD + "Voce nao viu nada... ;3"));
            //mc.execute(() -> mc.setScreen(new GameruleMenuScreen(Component.literal(ChatFormatting.GOLD + "" + ChatFormatting.BOLD + "FurSMP - Menu Admin"))));
        }
    }
}
