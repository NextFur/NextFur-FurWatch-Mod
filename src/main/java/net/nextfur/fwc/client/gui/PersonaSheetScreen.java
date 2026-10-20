package net.nextfur.fwc.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.network.persona.SavePersonaSheetC2SPacket;
import net.nextfur.fwc.persona.PersonaAttribute;
import net.nextfur.fwc.persona.PersonaData;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class PersonaSheetScreen extends Screen {
    private static final int PANEL_W = 292;
    private static final int PANEL_H = 238;
    private static final int ROW_START_Y_OFFSET = 54;
    private static final int ROW_HEIGHT = 24;

    private final UUID targetUuid;
    private final String targetName;
    private final PersonaData initialData;
    private final boolean adminEditMode;
    private final int initialTotalPool;

    private PersonaData workingData;
    private boolean confirmSaveArmed = false;

    private final Map<PersonaAttribute, Button> minusButtons = new EnumMap<>(PersonaAttribute.class);
    private final Map<PersonaAttribute, Button> plusButtons = new EnumMap<>(PersonaAttribute.class);
    private Button saveButton;
    private Button resetButton;
    private Button adminLockToggleBtn;

    public PersonaSheetScreen(UUID targetUuid, String targetName, PersonaData data, boolean adminEditMode) {
        super(Component.literal("Persona"));
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.initialData = data;
        this.workingData = data;
        this.adminEditMode = adminEditMode;
        this.initialTotalPool = data.getTotalPointsPool();
    }

    private boolean isEditable() {
        return this.adminEditMode || !this.workingData.locked();
    }

    @Override
    protected void init() {
        super.init();
        this.minusButtons.clear();
        this.plusButtons.clear();

        int panelX = (this.width - PANEL_W) / 2;
        int panelY = (this.height - PANEL_H) / 2;

        if (this.adminEditMode) {
            int ribbonY = panelY + 30;
            this.addRenderableWidget(Button.builder(Component.literal("-1"), b -> adminAdjustPoints(-1))
                    .pos(panelX + 150, ribbonY)
                    .size(24, 16)
                    .build());
            this.addRenderableWidget(Button.builder(Component.literal("+1"), b -> adminAdjustPoints(1))
                    .pos(panelX + 176, ribbonY)
                    .size(24, 16)
                    .build());

            this.adminLockToggleBtn = Button.builder(getLockToggleLabel(), b -> toggleAdminLock())
                    .pos(panelX + 204, ribbonY)
                    .size(80, 16)
                    .build();
            this.addRenderableWidget(this.adminLockToggleBtn);
        }

        PersonaAttribute[] attributes = PersonaAttribute.values();
        for (int i = 0; i < attributes.length; i++) {
            PersonaAttribute attr = attributes[i];
            int rowY = panelY + ROW_START_Y_OFFSET + (i * ROW_HEIGHT);

            Button minusBtn = Button.builder(Component.literal("-"), b -> adjustAttribute(attr, -1))
                    .pos(panelX + 114, rowY + 2)
                    .size(18, 18)
                    .build();
            Button plusBtn = Button.builder(Component.literal("+"), b -> adjustAttribute(attr, 1))
                    .pos(panelX + 260, rowY + 2)
                    .size(18, 18)
                    .build();

            this.minusButtons.put(attr, minusBtn);
            this.plusButtons.put(attr, plusBtn);
            this.addRenderableWidget(minusBtn);
            this.addRenderableWidget(plusBtn);
        }

        int bottomY = panelY + PANEL_H - 26;
        if (isEditable()) {
            this.saveButton = Button.builder(getSaveButtonLabel(), b -> onSaveClicked())
                    .pos(panelX + 10, bottomY)
                    .size(96, 20)
                    .build();
            this.addRenderableWidget(this.saveButton);

            this.resetButton = Button.builder(Component.literal("Resetar"), b -> resetBars())
                    .pos(panelX + 110, bottomY)
                    .size(84, 20)
                    .build();
            this.addRenderableWidget(this.resetButton);

            this.addRenderableWidget(Button.builder(Component.literal("Cancelar"), b -> onClose())
                    .pos(panelX + 198, bottomY)
                    .size(84, 20)
                    .build());
        } else {
            this.addRenderableWidget(Button.builder(Component.literal("Fechar"), b -> onClose())
                    .pos(panelX + (PANEL_W - 100) / 2, bottomY)
                    .size(100, 20)
                    .build());
        }

        updateButtonsState();
    }

    private void adminAdjustPoints(int delta) {
        if (!this.adminEditMode) return;
        this.workingData = this.workingData.withAvailablePoints(Math.max(0, this.workingData.availablePoints() + delta));
        updateButtonsState();
    }

    private void toggleAdminLock() {
        if (!this.adminEditMode) return;
        this.workingData = this.workingData.withLocked(!this.workingData.locked());
        if (this.adminLockToggleBtn != null) {
            this.adminLockToggleBtn.setMessage(getLockToggleLabel());
        }
        updateButtonsState();
    }

    private Component getLockToggleLabel() {
        return this.workingData.locked()
                ? Component.literal("Bloqueado").withStyle(ChatFormatting.RED)
                : Component.literal("Liberado").withStyle(ChatFormatting.GREEN);
    }

    private Component getSaveButtonLabel() {
        if (this.adminEditMode) {
            return Component.literal("Salvar").withStyle(ChatFormatting.GOLD);
        }
        if (this.confirmSaveArmed) {
            return Component.literal("Confirmar?").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
        }
        return Component.literal("Salvar");
    }

    private void adjustAttribute(PersonaAttribute attr, int delta) {
        if (!isEditable()) return;

        int currentLevel = this.workingData.getLevel(attr);
        int targetLevel = attr.clampLevel(currentLevel + delta);
        if (targetLevel == currentLevel) return;

        int pointCost = (targetLevel - currentLevel) * attr.getCostPerLevel();
        if (!this.adminEditMode && this.workingData.availablePoints() < pointCost) {
            return;
        }

        int newAvailable = this.workingData.availablePoints() - pointCost;
        if (this.adminEditMode && newAvailable < 0) {
            newAvailable = 0;
        }

        this.workingData = this.workingData
                .withAttribute(attr, targetLevel)
                .withAvailablePoints(newAvailable);

        this.confirmSaveArmed = false;
        if (this.saveButton != null) {
            this.saveButton.setMessage(getSaveButtonLabel());
        }
        updateButtonsState();
    }

    private void resetBars() {
        if (!isEditable()) return;
        int restoredPool = this.adminEditMode
                ? Math.max(PersonaData.DEFAULT_INITIAL_POINTS, this.workingData.getTotalPointsPool())
                : this.initialTotalPool;

        this.workingData = new PersonaData(0, 0, 0, 0, 0, 0, restoredPool, this.workingData.locked());
        this.confirmSaveArmed = false;
        if (this.saveButton != null) {
            this.saveButton.setMessage(getSaveButtonLabel());
        }
        updateButtonsState();
    }

    private void onSaveClicked() {
        if (!isEditable()) return;
        if (!this.adminEditMode && !this.confirmSaveArmed) {
            this.confirmSaveArmed = true;
            if (this.saveButton != null) {
                this.saveButton.setMessage(getSaveButtonLabel());
            }
            return;
        }

        PersonaData payload = this.adminEditMode
                ? this.workingData
                : this.workingData.withLocked(true);

        PacketDistributor.sendToServer(new SavePersonaSheetC2SPacket(this.targetUuid, payload, this.adminEditMode));
        this.onClose();
    }

    private void updateButtonsState() {
        boolean editable = isEditable();
        for (PersonaAttribute attr : PersonaAttribute.values()) {
            int level = this.workingData.getLevel(attr);
            int cost = attr.getCostPerLevel();

            Button minusBtn = this.minusButtons.get(attr);
            if (minusBtn != null) {
                minusBtn.active = editable && level > attr.getMinLevel();
            }

            Button plusBtn = this.plusButtons.get(attr);
            if (plusBtn != null) {
                boolean hasPoints = this.adminEditMode || this.workingData.availablePoints() >= cost;
                plusBtn.active = editable && level < attr.getMaxLevel() && hasPoints;
            }
        }

        if (this.saveButton != null) {
            this.saveButton.active = editable && (this.adminEditMode || this.workingData.availablePoints() >= 0);
        }
        if (this.resetButton != null) {
            this.resetButton.active = editable;
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gui, mouseX, mouseY, partialTick);

        int panelX = (this.width - PANEL_W) / 2;
        int panelY = (this.height - PANEL_H) / 2;

        // 1. Main Panel
        gui.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xEE121820);
        gui.renderOutline(panelX, panelY, PANEL_W, PANEL_H, 0xFF3E5A78);
        gui.renderOutline(panelX + 2, panelY + 2, PANEL_W - 4, PANEL_H - 4, 0xFF213142);

        // 2. Header Bar (Player Name only)
        gui.fill(panelX + 4, panelY + 4, panelX + PANEL_W - 4, panelY + 26, 0xFF192534);
        gui.renderOutline(panelX + 4, panelY + 4, PANEL_W - 8, 22, 0xFF2E4760);
        gui.drawString(this.font, this.targetName.toUpperCase(), panelX + 10, panelY + 11, 0xFF7FD4FF, true);

        // 3. Points Ribbon
        int ribbonY = panelY + 29;
        gui.fill(panelX + 6, ribbonY, panelX + PANEL_W - 6, ribbonY + 18, 0x88101822);
        gui.renderOutline(panelX + 6, ribbonY, PANEL_W - 12, 18, 0xFF2B3F54);

        int avail = this.workingData.availablePoints();
        int pointsColor = avail > 0 ? 0xFFFFD700 : (avail == 0 ? 0xFFAAFFAA : 0xFFFF5555);
        gui.drawString(this.font, "PONTOS DISPONÍVEIS: " + avail, panelX + 12, ribbonY + 5, pointsColor, true);

        // 4. Attribute Rows & Segmented Bars
        PersonaAttribute hoveredAttr = null;
        PersonaAttribute[] attributes = PersonaAttribute.values();
        for (int i = 0; i < attributes.length; i++) {
            PersonaAttribute attr = attributes[i];
            int rowY = panelY + ROW_START_Y_OFFSET + (i * ROW_HEIGHT);
            int level = this.workingData.getLevel(attr);

            boolean rowHovered = mouseX >= panelX + 6 && mouseX <= panelX + PANEL_W - 6
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT - 2;
            if (rowHovered) {
                hoveredAttr = attr;
            }

            int rowBg = rowHovered ? 0x99203042 : (i % 2 == 0 ? 0x6616202C : 0x661A2634);
            gui.fill(panelX + 6, rowY, panelX + PANEL_W - 6, rowY + ROW_HEIGHT - 2, rowBg);
            if (rowHovered) {
                gui.renderOutline(panelX + 6, rowY, PANEL_W - 12, ROW_HEIGHT - 2, 0xFF4A7298);
            }

            gui.drawString(this.font, attr.getDisplayName(), panelX + 12, rowY + 3, 0xFFE8F1F8, true);
            String costLabel = "Custo: " + attr.getCostPerLevel() + (attr.getCostPerLevel() == 1 ? " pt" : " pts");
            gui.drawString(this.font, costLabel, panelX + 12, rowY + 13, 0xFF7C93A8, false);

            int barX = panelX + 136;
            int barY = rowY + 4;
            int barW = 120;
            int barH = 14;
            renderAttributeBar(gui, attr, level, barX, barY, barW, barH);
        }

        // 5. Render buttons
        for (net.minecraft.client.gui.components.Renderable renderable : this.renderables) {
            renderable.render(gui, mouseX, mouseY, partialTick);
        }

        // 6. Render clean tooltip when hovering an attribute row
        if (hoveredAttr != null) {
            int currentLvl = this.workingData.getLevel(hoveredAttr);
            gui.renderComponentTooltip(this.font, hoveredAttr.buildTooltip(currentLvl), mouseX, mouseY);
        }
    }

    private void renderAttributeBar(GuiGraphics gui, PersonaAttribute attr, int level, int barX, int barY, int barW, int barH) {
        gui.fill(barX, barY, barX + barW, barY + barH, 0xFF0D131A);
        gui.renderOutline(barX, barY, barW, barH, 0xFF33495E);

        int maxSteps = attr.getMaxLevel();
        int centerX = barX + (barW / 2);
        int halfInnerW = (barW / 2) - 3;
        int gap = 2;
        int segW = (halfInnerW - (gap * (maxSteps - 1))) / maxSteps;

        // Left side: Negative segments
        for (int s = 1; s <= maxSteps; s++) {
            int segRight = centerX - 2 - ((s - 1) * (segW + gap));
            int segLeft = segRight - segW;
            boolean active = level <= -s;
            int fillColor = active ? 0xFFD64242 : 0xFF1B242E;
            int outlineColor = active ? 0xFFFF8080 : 0xFF283542;
            gui.fill(segLeft, barY + 2, segRight, barY + barH - 2, fillColor);
            gui.renderOutline(segLeft, barY + 2, segW, barH - 4, outlineColor);
        }

        // Center zero divider
        gui.fill(centerX - 1, barY + 1, centerX + 1, barY + barH - 1, level == 0 ? 0xFFFFFFFF : 0xFF667788);

        // Right side: Positive segments
        for (int s = 1; s <= maxSteps; s++) {
            int segLeft = centerX + 2 + ((s - 1) * (segW + gap));
            int segRight = segLeft + segW;
            boolean active = level >= s;
            int fillColor = active ? 0xFF28C074 : 0xFF1B242E;
            int outlineColor = active ? 0xFF72FFB4 : 0xFF283542;
            gui.fill(segLeft, barY + 2, segRight, barY + barH - 2, fillColor);
            gui.renderOutline(segLeft, barY + 2, segW, barH - 4, outlineColor);
        }

        String lvlStr = level > 0 ? "+" + level : String.valueOf(level);
        int lvlColor = level > 0 ? 0xFFAAFFCC : (level < 0 ? 0xFFFFAAAA : 0xFFDDDDDD);
        gui.drawCenteredString(this.font, lvlStr, centerX, barY + 3, lvlColor);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
