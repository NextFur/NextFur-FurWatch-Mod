package net.nextfur.fwc.pda.app.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.pda.app.PdaApp;
import net.nextfur.fwc.pda.client.PdaScreen;
import net.nextfur.fwc.pda.client.PdaTheme;

public class VideoPlayerApp extends PdaApp {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "videoplayer");

    public VideoPlayerApp() {
        super(
                ID,
                Component.literal("Videoplayer"),
                Component.literal("Reprodutor de mídia holográfica."),
                0xFFFF006E, // Vivid Magenta/Pink cyber accent
                null
        );
    }

    @Override
    protected void renderProceduralIcon(GuiGraphics gui, int x, int y, int size, boolean hovered) {
        int color = hovered ? 0xFFFFFFFF : (0xFF000000 | getThemeColor());
        int fill = hovered ? 0x44FF006E : 0x22FF006E;

        int cx = x + size / 2;
        int cy = y + size / 2;

        gui.fill(x + 4, y + 4, x + size - 4, y + size - 4, fill);
        gui.renderOutline(x + 4, y + 4, size - 8, size - 8, color);

        gui.drawCenteredString(gui.guiWidth() > 0 ? net.minecraft.client.Minecraft.getInstance().font : null,
                "▶", cx + 1, cy - 4, color);
    }

    @Override
    public void init(PdaScreen screen, int contentX, int contentY, int contentWidth, int contentHeight) {
        // Navigation is handled via the top bar tabs
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int contentX, int contentY, int contentWidth, int contentHeight) {
        Font font = net.minecraft.client.Minecraft.getInstance().font;

        PdaTheme.drawPanel(gui, contentX, contentY, contentWidth, contentHeight, 0xFFFF006E);

        gui.drawCenteredString(font, ChatFormatting.BOLD + "FURWATCH MEDIA CENTER // VIDEOPLAYER", contentX + contentWidth / 2, contentY + 8, 0xFFFF006E);
        gui.drawCenteredString(font, "Reprodutor Holográfico de Vídeos e Streams", contentX + contentWidth / 2, contentY + 20, PdaTheme.TEXT_SUB);

        int cardW = contentWidth - 20;
        int cardH = 88;
        int cardX = contentX + 10;
        int cardY = contentY + 34;

        gui.fill(cardX, cardY, cardX + cardW, cardY + cardH, PdaTheme.BG_CARD);
        gui.renderOutline(cardX, cardY, cardW, cardH, 0x55FF006E);

        gui.drawString(font, "Driver de Vídeo: " + ChatFormatting.GOLD + "[EM DESENVOLVIMENTO]", cardX + 8, cardY + 8, 0xFFFFFFFF, false);
        gui.drawString(font, "Formatos Suportados: " + ChatFormatting.GRAY + "MP4, WebM, FurCinema", cardX + 8, cardY + 22, PdaTheme.TEXT_SUB, false);
        gui.drawString(font, "Aceleração Holográfica: " + ChatFormatting.LIGHT_PURPLE + "Habilitada", cardX + 8, cardY + 36, PdaTheme.TEXT_SUB, false);
        gui.drawString(font, "Buffer de Streaming: " + ChatFormatting.AQUA + "0 MB / Pronto", cardX + 8, cardY + 50, PdaTheme.TEXT_SUB, false);
        gui.drawString(font, "Aviso: " + ChatFormatting.YELLOW + "Codec de vídeo para telas remotas em desenvolvimento.", cardX + 8, cardY + 66, PdaTheme.TEXT_SUB, false);
    }
}
