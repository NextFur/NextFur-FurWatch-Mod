package net.nextfur.fwc.client.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.nextfur.fwc.util.world.SkyColorState;

public class CustomSkyRenderer {
    @SubscribeEvent
    public void onFogColor(ViewportEvent.ComputeFogColor event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        int color = SkyColorState.getFogColor();
        if(color == -1) return; // default color

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        event.setRed(r);
        event.setGreen(g);
        event.setBlue(b);
    }
}
