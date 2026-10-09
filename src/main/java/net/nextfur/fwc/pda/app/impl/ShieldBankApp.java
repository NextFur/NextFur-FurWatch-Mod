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

    private PdaScreen screen;

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
        this.screen = screen;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int contentX, int contentY, int contentWidth, int contentHeight) {
        Font font = net.minecraft.client.Minecraft.getInstance().font;

        PdaTheme.drawPanel(gui, contentX, contentY, contentWidth, contentHeight, 0xFF00FFB2);

        gui.drawCenteredString(font, ChatFormatting.BOLD + "SHIELD BANK - CONTA DIGITAL", contentX + contentWidth / 2, contentY + 6, 0xFF00FFB2);

        net.nextfur.fwc.pda.data.PdaData data = (this.screen != null) ? this.screen.getPdaData() : null;
        long balance = (data != null) ? data.bankBalanceCents() : 0L;
        String owner = (data != null && data.hasOwner()) ? data.ownerName() : "Não vinculado";
        String accountId = (data != null) ? "#" + data.pdaId().toString().substring(0, 8) : "#00000000";

        // Virtual Bank Card Container
        int cardW = contentWidth - 16;
        int cardH = 82;
        int cardX = contentX + 8;
        int cardY = contentY + 20;

        // Card background & borders
        gui.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0xFF0B1A24);
        gui.fill(cardX + 1, cardY + 1, cardX + cardW - 1, cardY + 18, 0xFF122838);
        gui.renderOutline(cardX, cardY, cardW, cardH, 0xFF00FFB2);

        // Card header
        gui.drawString(font, "SHIELD ACCOUNT", cardX + 6, cardY + 5, 0xFF00FFB2, false);
        gui.drawString(font, accountId, cardX + cardW - font.width(accountId) - 6, cardY + 5, 0xFF70A0B0, false);

        // Owner & Account
        gui.drawString(font, "Titular: " + ChatFormatting.WHITE + owner, cardX + 6, cardY + 24, 0xFF88A0B0, false);

        // Balance Section
        gui.drawString(font, "Saldo em Conta Protegida:", cardX + 6, cardY + 40, 0xFF88A0B0, false);
        String formattedBalance = net.nextfur.fwc.economy.data.EconomyFormatHelper.formatStandard(balance);
        gui.drawString(font, ChatFormatting.BOLD + formattedBalance, cardX + 6, cardY + 52, 0xFF00FFB2, false);

        String denom = "(" + net.nextfur.fwc.economy.data.EconomyFormatHelper.formatDenomination(balance) + ")";
        gui.drawString(font, denom, cardX + 10 + font.width(formattedBalance), cardY + 53, 0xFF00A876, false);

        // Status badge
        String statusText = "[ATIVO]";
        gui.drawString(font, statusText, cardX + cardW - font.width(statusText) - 6, cardY + 68, 0xFF00FF88, false);

        // Bottom Info Note Box
        int infoY = cardY + cardH + 6;
        int infoH = contentHeight - (infoY - contentY) - 4;
        if (infoH > 16) {
            gui.fill(cardX, infoY, cardX + cardW, infoY + infoH, 0x44000000);
            gui.renderOutline(cardX, infoY, cardW, infoH, 0x3300FFB2);

            gui.drawString(font, ChatFormatting.AQUA + "ℹ Informações:", cardX + 4, infoY + 4, 0xFFFFFFFF, false);
            gui.drawString(font, ChatFormatting.GRAY + "Depósitos e saques apenas em ATMs", cardX + 4, infoY + 16, 0xFFFFFFFF, false);
        }
    }
}
