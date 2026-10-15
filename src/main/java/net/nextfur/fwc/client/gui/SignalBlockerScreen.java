package net.nextfur.fwc.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.blocks.entity.SignalBlockerBlockEntity;
import net.nextfur.fwc.network.signalblocker.UpdateSignalBlockerC2SPacket;

public class SignalBlockerScreen extends Screen {
    private final BlockPos pos;
    private int radius;
    private int redstoneMode;
    private boolean manualEnabled;
    private final boolean initialActive;

    private Button redstoneModeBtn;
    private Button powerBtn;

    public SignalBlockerScreen(BlockPos pos, int radius, int redstoneMode, boolean manualEnabled, boolean active) {
        super(Component.literal("Bloqueador de Sinal Militar"));
        this.pos = pos;
        this.radius = Math.max(SignalBlockerBlockEntity.MIN_RADIUS, Math.min(SignalBlockerBlockEntity.MAX_RADIUS, radius));
        this.redstoneMode = redstoneMode;
        this.manualEnabled = manualEnabled;
        this.initialActive = active;
    }

    @Override
    protected void init() {
        super.init();

        int panelW = 280;
        int panelH = 200;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int rowY = panelY + 68;

        // Radius Adjust Buttons: [-10] [-5] [-1] | [+1] [+5] [+10]
        int btnW = 34;
        int btnH = 20;

        this.addRenderableWidget(Button.builder(Component.literal("-10"), b -> adjustRadius(-10))
                .pos(panelX + 20, rowY).size(btnW, btnH).build());
        this.addRenderableWidget(Button.builder(Component.literal("-5"), b -> adjustRadius(-5))
                .pos(panelX + 58, rowY).size(btnW, btnH).build());
        this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> adjustRadius(-1))
                .pos(panelX + 96, rowY).size(btnW, btnH).build());

        this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> adjustRadius(1))
                .pos(panelX + 150, rowY).size(btnW, btnH).build());
        this.addRenderableWidget(Button.builder(Component.literal("+5"), b -> adjustRadius(5))
                .pos(panelX + 188, rowY).size(btnW, btnH).build());
        this.addRenderableWidget(Button.builder(Component.literal("+10"), b -> adjustRadius(10))
                .pos(panelX + 226, rowY).size(btnW, btnH).build());

        // Quick Presets: 16m (Padrão), 32m, 64m, 128m (Máx)
        int presetY = rowY + 24;
        int presetW = 56;
        this.addRenderableWidget(Button.builder(Component.literal("16m"), b -> setRadius(16))
                .pos(panelX + 20, presetY).size(presetW, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("32m"), b -> setRadius(32))
                .pos(panelX + 80, presetY).size(presetW, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("64m"), b -> setRadius(64))
                .pos(panelX + 140, presetY).size(presetW, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal("128m"), b -> setRadius(128))
                .pos(panelX + 200, presetY).size(presetW, 18).build());

        // Redstone Mode Button
        int configY = presetY + 24;
        this.redstoneModeBtn = Button.builder(getRedstoneModeLabel(), b -> cycleRedstoneMode())
                .pos(panelX + 20, configY)
                .size(panelW - 40, 20)
                .build();
        this.addRenderableWidget(redstoneModeBtn);

        // Power Override Button
        int powerY = configY + 22;
        this.powerBtn = Button.builder(getPowerLabel(), b -> togglePower())
                .pos(panelX + 20, powerY)
                .size(panelW - 40, 20)
                .build();
        this.addRenderableWidget(powerBtn);

        // Save & Cancel Buttons
        int bottomY = panelY + panelH - 26;
        int actionBtnW = 116;

        this.addRenderableWidget(Button.builder(Component.literal("Salvar Config."), b -> saveAndClose())
                .pos(panelX + 20, bottomY)
                .size(actionBtnW, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancelar"), b -> onClose())
                .pos(panelX + panelW - 20 - actionBtnW, bottomY)
                .size(actionBtnW, 20)
                .build());
    }

    private void adjustRadius(int delta) {
        setRadius(this.radius + delta);
    }

    private void setRadius(int newRadius) {
        this.radius = Math.max(SignalBlockerBlockEntity.MIN_RADIUS, Math.min(SignalBlockerBlockEntity.MAX_RADIUS, newRadius));
    }

    private void cycleRedstoneMode() {
        this.redstoneMode = (this.redstoneMode + 1) % 3;
        if (this.redstoneModeBtn != null) {
            this.redstoneModeBtn.setMessage(getRedstoneModeLabel());
        }
    }

    private Component getRedstoneModeLabel() {
        return switch (this.redstoneMode) {
            case SignalBlockerBlockEntity.MODE_INVERTED -> Component.literal("Redstone: Desativar por Interferência");
            case SignalBlockerBlockEntity.MODE_DIRECT -> Component.literal("Redstone: Ativar com Sinal");
            case SignalBlockerBlockEntity.MODE_ALWAYS_ON -> Component.literal("Redstone: Ignorar (Sempre Ativo)");
            default -> Component.literal("Redstone: Desconhecido");
        };
    }

    private void togglePower() {
        this.manualEnabled = !this.manualEnabled;
        if (this.powerBtn != null) {
            this.powerBtn.setMessage(getPowerLabel());
        }
    }

    private Component getPowerLabel() { 
        return this.manualEnabled
                ? Component.literal("LIGADO [ON]").withStyle(ChatFormatting.GREEN)
                : Component.literal("DESLIGADO [OFF]").withStyle(ChatFormatting.RED);
    }

    private void saveAndClose() {
        PacketDistributor.sendToServer(new UpdateSignalBlockerC2SPacket(
                this.pos,
                this.radius,
                this.redstoneMode,
                this.manualEnabled
        ));
        onClose();
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        // 1. Render background blur and darkening FIRST so it only affects the world behind the GUI
        super.renderBackground(gui, mouseX, mouseY, partialTick);

        int panelW = 280;
        int panelH = 200;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        // 2. Main Panel (Tactical Military Green)
        gui.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xEE162214);
        gui.renderOutline(panelX, panelY, panelW, panelH, 0xFF4C6A48);
        gui.renderOutline(panelX + 2, panelY + 2, panelW - 4, panelH - 4, 0xFF2A3B28);

        // 3. Header Bar
        gui.fill(panelX + 4, panelY + 4, panelX + panelW - 4, panelY + 26, 0xFF1E2F1C);
        gui.renderOutline(panelX + 4, panelY + 4, panelW - 8, 22, 0xFF3D5838);

        gui.drawString(this.font, "Bloqueador Dalvânico -", panelX + 10, panelY + 10, 0xFFA4D68E, true);
        gui.drawString(this.font, "EW-JAMMER MK-IV", panelX + panelW - 100, panelY + 10, 0xFF76A065, false);

        // 4. Status Ribbon
        int statusY = panelY + 30;
        boolean currentlyActive = this.manualEnabled && (this.redstoneMode == SignalBlockerBlockEntity.MODE_ALWAYS_ON || initialActive);
        int statusBg = currentlyActive ? 0x44228833 : 0x44882222;
        int statusBorder = currentlyActive ? 0xFF00FF66 : 0xFFFF4444;
        String statusText = currentlyActive
                ? "● OPERACIONAL"
                : "○ STANDBY";
        int statusColor = currentlyActive ? 0xFF55FF88 : 0xFFFF7777;

        gui.fill(panelX + 6, statusY, panelX + panelW - 6, statusY + 16, statusBg);
        gui.renderOutline(panelX + 6, statusY, panelW - 12, 16, statusBorder);
        gui.drawCenteredString(this.font, statusText, panelX + panelW / 2, statusY + 4, statusColor);

        int radiusDisplayY = panelY + 50;
        String radiusText = "RAIO: " + this.radius + " BLOCOS";
        gui.drawCenteredString(this.font, Component.literal(radiusText).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD),
                panelX + panelW / 2, radiusDisplayY, 0xFFFFFF);

        for (net.minecraft.client.gui.components.Renderable renderable : this.renderables) {
            renderable.render(gui, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
