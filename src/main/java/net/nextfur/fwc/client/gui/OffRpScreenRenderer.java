package net.nextfur.fwc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.nextfur.fwc.client.world.OffRpRenderer;

public class OffRpScreenRenderer {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean offrp = OffRpRenderer.OFFRP_PLAYERS.contains(mc.player.getUUID());
        if(!offrp) return;

        String text = "[OFF-RP ON]";

        var gui = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int x = 10;
        int y = screenHeight - 20;

        RenderSystem.enableBlend();
        gui.drawString(mc.font, text, x, y, 0xFFAA66FF);
    }

}
