package net.nextfur.fwc.pda.app.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.pda.app.PdaApp;
import net.nextfur.fwc.pda.client.PdaScreen;
import net.nextfur.fwc.pda.client.PdaTheme;
import net.nextfur.fwc.pda.data.PdaData;
import net.nextfur.fwc.pda.network.PdaActionC2SPacket;

import java.util.Optional;

public class SettingsApp extends PdaApp {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "settings");

    private PdaScreen currentScreen;
    private boolean confirmingReset = false;
    private Button resetBtn;
    private Button confirmBtn;
    private Button cancelBtn;
    private Button toggleVignetteBtn;
    private Button toggleCracksBtn;

    public SettingsApp() {
        super(
                ID,
                Component.literal("Configurações"),
                Component.literal("Gerenciamento do dispositivo e redefinição."),
                0xFFFFAA00, // Tech Amber accent
                null
        );
    }

    @Override
    protected void renderProceduralIcon(GuiGraphics gui, int x, int y, int size, boolean hovered) {
        int color = hovered ? 0xFFFFFFFF : (0xFF000000 | getThemeColor());
        int fill = hovered ? 0x44FFAA00 : 0x22FFAA00;

        int cx = x + size / 2;
        int cy = y + size / 2;

        gui.fill(x + 4, y + 4, x + size - 4, y + size - 4, fill);
        gui.renderOutline(x + 4, y + 4, size - 8, size - 8, color);

        gui.drawCenteredString(gui.guiWidth() > 0 ? net.minecraft.client.Minecraft.getInstance().font : null,
                "⚙", cx, cy - 4, color);
    }

    @Override
    public void init(PdaScreen screen, int contentX, int contentY, int contentWidth, int contentHeight) {
        this.currentScreen = screen;
        this.confirmingReset = false;

        // Overlay toggles
        this.toggleVignetteBtn = Button.builder(
                getVignetteText(),
                b -> {
                    PdaScreen.showVignetteOverlay = !PdaScreen.showVignetteOverlay;
                    b.setMessage(getVignetteText());
                }
        ).pos(contentX + 6, contentY + 72).size(125, 16).build();
        screen.addAppWidget(toggleVignetteBtn);

        this.toggleCracksBtn = Button.builder(
                getCracksText(),
                b -> {
                    PdaScreen.showCrackedOverlay = !PdaScreen.showCrackedOverlay;
                    b.setMessage(getCracksText());
                }
        ).pos(contentX + 135, contentY + 72).size(125, 16).build();
        screen.addAppWidget(toggleCracksBtn);

        // Reset buttons
        this.resetBtn = Button.builder(Component.literal("Redefinir PDA"), b -> {
            this.confirmingReset = true;
            updateButtons();
        }).pos(contentX + 6, contentY + 92).size(95, 16).build();
        screen.addAppWidget(resetBtn);

        this.confirmBtn = Button.builder(Component.literal("Confirmar Reset"), b -> {
            PacketDistributor.sendToServer(new PdaActionC2SPacket(
                    PdaActionC2SPacket.ACTION_RESET_PDA,
                    screen.isMainHand(),
                    Optional.empty(),
                    "",
                    "",
                    Optional.empty()
            ));
            this.confirmingReset = false;
            updateButtons();
        }).pos(contentX + 6, contentY + 92).size(95, 16).build();
        screen.addAppWidget(confirmBtn);

        this.cancelBtn = Button.builder(Component.literal("Cancelar"), b -> {
            this.confirmingReset = false;
            updateButtons();
        }).pos(contentX + 105, contentY + 92).size(60, 16).build();
        screen.addAppWidget(cancelBtn);

        updateButtons();
    }

    private Component getVignetteText() {
        return Component.literal("Vinheta CRT: " + (PdaScreen.showVignetteOverlay ? "§aLIGADO" : "§cDESLIGADO"));
    }

    private Component getCracksText() {
        return Component.literal("Tela Trincada: " + (PdaScreen.showCrackedOverlay ? "§aLIGADO" : "§cDESLIGADO"));
    }

    private void updateButtons() {
        if (resetBtn != null) resetBtn.visible = !confirmingReset;
        if (confirmBtn != null) confirmBtn.visible = confirmingReset;
        if (cancelBtn != null) cancelBtn.visible = confirmingReset;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int contentX, int contentY, int contentWidth, int contentHeight) {
        Font font = net.minecraft.client.Minecraft.getInstance().font;
        PdaData data = currentScreen.getPdaData();

        PdaTheme.drawPanel(gui, contentX, contentY, contentWidth, contentHeight, 0xFFFFAA00);

        gui.drawString(font, ChatFormatting.BOLD + "CONFIGS.", contentX + 8, contentY + 5, 0xFFFFAA00, false);

        int infoY = contentY + 16;
        int cardW = contentWidth - 12;
        int cardH = 52;
        gui.fill(contentX + 6, infoY, contentX + 6 + cardW, infoY + cardH, PdaTheme.BG_CARD);
        gui.renderOutline(contentX + 6, infoY, cardW, cardH, 0x44FFAA00);

        String ownerStr = (data != null && data.hasOwner()) ? data.ownerName() : "Não Vinculado";
        ChatFormatting ownerFormat = (data != null && data.hasOwner()) ? ChatFormatting.WHITE : ChatFormatting.RED;
        String pdaIdStr = (data != null) ? "#" + data.pdaId().toString().substring(0, 8) : "Desconhecido";
        String colorVariantStr = (data != null) ? data.colorVariant().getDisplayName() : "Azul";
        int contactsCount = (data != null) ? data.contacts().size() : 0;
        int notesCount = (data != null) ? data.notes().size() : 0;

        gui.drawString(font, "Modelo: " + ChatFormatting.AQUA + "FurWatch Tab [" + colorVariantStr + "]" + ChatFormatting.DARK_GRAY + " " + pdaIdStr, contentX + 12, infoY + 6, 0xFFFFFFFF, false);
        gui.drawString(font, "Proprietário: " + ownerFormat + ownerStr, contentX + 12, infoY + 18, 0xFFFFFFFF, false);
        gui.drawString(font, "Armazenamento: " + ChatFormatting.GREEN + contactsCount + " Contatos" + ChatFormatting.GRAY + " | " + ChatFormatting.GREEN + notesCount + " Notas", contentX + 12, infoY + 30, 0xFFFFFFFF, false);

        int footerY = contentY + 114;
        if (confirmingReset) {
            gui.drawString(font, ChatFormatting.RED + "⚠ Redefinir desvinculará o proprietário deste PDA!", contentX + 8, footerY, 0xFFFF5555, false);
        } else {
            gui.drawString(font, ChatFormatting.GRAY + "Troque contatos para preencher seu PDA!", contentX + 8, footerY, 0xFF7A8DAB, false);
        }
    }

    @Override
    public void onDataUpdated(PdaData data) {
        updateButtons();
    }
}
