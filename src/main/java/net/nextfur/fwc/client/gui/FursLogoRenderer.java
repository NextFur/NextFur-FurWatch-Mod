package net.nextfur.fwc.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.FwMain;

public class FursLogoRenderer extends LogoRenderer {
    private static final ResourceLocation FURS_LOGO = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/gui/title/furs4logo.png");

    public FursLogoRenderer(boolean keepLogoThroughFade) {
        super(keepLogoThroughFade);
    }

    @Override
    public void renderLogo(GuiGraphics gui, int screenWidth, float transparency, int height) {
        gui.setColor(1.0F, 1.0F, 1.0F, transparency);
        RenderSystem.enableBlend();

        int x = screenWidth / 2 - 128;
        gui.blit(FURS_LOGO, x, height, 0.0F, 0.0F, 256, 64, 256, 64);

        RenderSystem.disableBlend();
    }
}
