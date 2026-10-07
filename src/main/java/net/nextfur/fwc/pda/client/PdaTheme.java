package net.nextfur.fwc.pda.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.FwMain;

public class PdaTheme {
    public static final ResourceLocation FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/gui/pda/pda_frame.png");
    public static final ResourceLocation VIGNETTE_TEXTURE = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/gui/pda/pda_vignette.png");
    public static final ResourceLocation CRACKS_TEXTURE = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/gui/pda/pda_cracks.png");
    public static final ResourceLocation SMUDGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/gui/pda/pda_smudge.png");
    public static final ResourceLocation BLUR_TEXTURE = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "textures/gui/pda/pda_blur.png");

    public static final int BG_SCREEN = 0xFF0D1217;
    public static final int BG_PANEL = 0xEE0B131C;
    public static final int BG_CARD = 0xAA0E1824;
    public static final int BG_CARD_HOVER = 0xDD152538;

    public static final int TEXT_TITLE = 0xFFFFFFFF;
    public static final int TEXT_GREEN = 0xFF00FF88;
    public static final int TEXT_CYAN = 0xFF00E5FF;
    public static final int TEXT_SUB = 0xFF8FA3BF;
    public static final int TEXT_MUTED = 0xFF546680;

    /**
     * Renders the rugged PDA hardware chassis texture (image 0.png).
     */
    public static void drawHardwareFrame(GuiGraphics gui, int x, int y, int width, int height) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gui.blit(FRAME_TEXTURE, x, y, width, height, 0.0f, 0.0f, 1158, 702, 1158, 702);
        RenderSystem.disableBlend();
    }

    /**
     * Renders the CRT screen vignette / edge shadow overlay (image 2.png).
     */
    public static void drawScreenVignette(GuiGraphics gui, int x, int y, int width, int height) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gui.blit(VIGNETTE_TEXTURE, x, y, width, height, 0.0f, 0.0f, 1024, 576, 1024, 576);
        RenderSystem.disableBlend();
    }

    /**
     * Renders the cracked screen overlay (image 4.png).
     */
    public static void drawScreenCracks(GuiGraphics gui, int x, int y, int width, int height) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gui.blit(CRACKS_TEXTURE, x, y, width, height, 0.0f, 0.0f, 2046, 1195, 2046, 1195);
        RenderSystem.disableBlend();
    }

    /**
     * Renders the screen smudge / reflection overlay.
     */
    public static void drawScreenSmudge(GuiGraphics gui, int x, int y, int width, int height) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        gui.blit(SMUDGE_TEXTURE, x, y, width, height, 0.0f, 0.0f, 256, 256, 256, 256);
        RenderSystem.disableBlend();
    }

    /**
     * Draws a sci-fi slanted parallelogram tab button matching the example UI.
     */
    public static void drawSciFiTab(GuiGraphics gui, Font font, Component label, int x, int y, int width, int height, boolean active, boolean hovered, int accentColor) {
        int bg;
        int border;
        int textColor;

        if (active) {
            bg = 0xDD003542;
            border = (accentColor != 0) ? accentColor : 0xFF00FFB2;
            textColor = 0xFFFFFFFF;
        } else if (hovered) {
            bg = 0x88002630;
            border = 0xFF00E5FF;
            textColor = 0xFFE0FFFF;
        } else {
            bg = 0x550A141D;
            border = 0x5500A896;
            textColor = 0xFF7CA0B5;
        }

        int slant = 3;
        for (int row = 0; row < height; row++) {
            int xOff = (height - 1 - row) * slant / Math.max(1, height - 1);
            int rowX = x + xOff;
            gui.fill(rowX, y + row, rowX + width - slant, y + row + 1, bg);
            gui.fill(rowX, y + row, rowX + 1, y + row + 1, border);
            gui.fill(rowX + width - slant - 1, y + row, rowX + width - slant, y + row + 1, border);
        }
        gui.fill(x + slant, y, x + width, y + 1, border);
        gui.fill(x, y + height - 1, x + width - slant, y + height, border);

        if (active) {
            // Underline highlight bar
            gui.fill(x + 2, y + height - 2, x + width - slant - 2, y + height - 1, border);
        }

        int textWidth = font.width(label);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - 8) / 2;
        gui.drawString(font, label, textX, textY, textColor, false);
    }

    /**
     * Draws the sci-fi workspace panel frame.
     */
    public static void drawWorkspacePanel(GuiGraphics gui, int x, int y, int width, int height, int borderColor) {
        gui.fill(x, y, x + width, y + height, BG_PANEL);

        int border = (borderColor != 0) ? borderColor : 0x5500E5FF;
        gui.renderOutline(x, y, width, height, border);

        // Sci-fi corner brackets
        int tickLen = 6;
        gui.fill(x, y, x + tickLen, y + 1, border | 0xFF000000);
        gui.fill(x, y, x + 1, y + tickLen, border | 0xFF000000);
        gui.fill(x + width - tickLen, y, x + width, y + 1, border | 0xFF000000);
        gui.fill(x + width - 1, y, x + width, y + tickLen, border | 0xFF000000);
        gui.fill(x, y + height - 1, x + tickLen, y + height, border | 0xFF000000);
        gui.fill(x, y + height - tickLen, x + 1, y + height, border | 0xFF000000);
        gui.fill(x + width - tickLen, y + height - 1, x + width, y + height, border | 0xFF000000);
        gui.fill(x + width - 1, y + height - tickLen, x + width, y + height, border | 0xFF000000);
    }

    /**
     * Alias for drawWorkspacePanel.
     */
    public static void drawPanel(GuiGraphics gui, int x, int y, int width, int height, int borderColor) {
        drawWorkspacePanel(gui, x, y, width, height, borderColor);
    }
}
