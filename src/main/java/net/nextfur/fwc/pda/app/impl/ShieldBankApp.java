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

public class ShieldBankApp extends PdaApp {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "shield_bank");

    public ShieldBankApp() {
        super(
                ID,
                Component.literal("Shield Bank"),
                Component.literal("Sistema financeiro digital integrado."),
                0xFF00FFB2, // Emerald/Teal cyber accent
                null
        );
    }

    @Override
    protected void renderProceduralIcon(GuiGraphics gui, int x, int y, int size, boolean hovered) {
        int color = hovered ? 0xFFFFFFFF : (0xFF000000 | getThemeColor());
        int fill = hovered ? 0x4400FFB2 : 0x2200FFB2;

        int cx = x + size / 2;
        int cy = y + size / 2;

        gui.fill(x + 4, y + 4, x + size - 4, y + size - 4, fill);
        gui.renderOutline(x + 4, y + 4, size - 8, size - 8, color);

        gui.drawCenteredString(gui.guiWidth() > 0 ? net.minecraft.client.Minecraft.getInstance().font : null,
                "$", cx, cy - 4, color);
    }

    @Override
    public void init(PdaScreen screen, int contentX, int contentY, int contentWidth, int contentHeight) {
        // Navigation is handled via the top bar tabs
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int contentX, int contentY, int contentWidth, int contentHeight) {
        Font font = net.minecraft.client.Minecraft.getInstance().font;

        PdaTheme.drawPanel(gui, contentX, contentY, contentWidth, contentHeight, 0xFF00FFB2);

        gui.drawCenteredString(font, ChatFormatting.BOLD + "SHIELD BANK // TERMINAL FINANCEIRO", contentX + contentWidth / 2, contentY + 8, 0xFF00FFB2);
        gui.drawCenteredString(font, "Rede Bancária Descentralizada FurWatch", contentX + contentWidth / 2, contentY + 20, PdaTheme.TEXT_SUB);

        int cardW = contentWidth - 20;
        int cardH = 88;
        int cardX = contentX + 10;
        int cardY = contentY + 34;

        gui.fill(cardX, cardY, cardX + cardW, cardY + cardH, PdaTheme.BG_CARD);
        gui.renderOutline(cardX, cardY, cardW, cardH, 0x5500FFB2);

        gui.drawString(font, "Status do Módulo: " + ChatFormatting.GOLD + "[EM DESENVOLVIMENTO]", cardX + 8, cardY + 8, 0xFFFFFFFF, false);
        gui.drawString(font, "Servidor de Transações: " + ChatFormatting.GRAY + "FurWatch Net (Offline)", cardX + 8, cardY + 22, PdaTheme.TEXT_SUB, false);
        gui.drawString(font, "Protocolo de Criptografia: " + ChatFormatting.AQUA + "AES-256 GCM (Ativo)", cardX + 8, cardY + 36, PdaTheme.TEXT_SUB, false);
        gui.drawString(font, "Saldo Vinculado à Carteira: " + ChatFormatting.GREEN + "F$ 0,00", cardX + 8, cardY + 50, 0xFFFFFFFF, false);
        gui.drawString(font, "Aviso: " + ChatFormatting.YELLOW + "Operações de saque e transferências em breve.", cardX + 8, cardY + 66, PdaTheme.TEXT_SUB, false);
    }
}
