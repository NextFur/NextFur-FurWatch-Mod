package net.nextfur.fwc.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.network.common.RollDiceC2SPacket;

public class DiceRollMenuScreen extends Screen {
    private EditBox rollformula;
    private Checkbox privateRoll;
    private Button rollButton;
    private Button cancelButton;

    private static final int MENU_WIDTH = 220;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;

    private static final int[] QUICK_DICE = {4, 6, 8, 10, 12, 20, 100};

    public DiceRollMenuScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int quickButtonWidth = 28;
        int spacing = 2;
        int totalRowWidth = (quickButtonWidth * QUICK_DICE.length) + (spacing * (QUICK_DICE.length - 1));
        int startX = centerX - (totalRowWidth / 2);
        int quickButtonsY = centerY - 55;

        for (int i = 0; i < QUICK_DICE.length; i++) {
            int faces = QUICK_DICE[i];

            Button diceBtn = Button.builder(Component.literal("d" + faces), b -> performQuickRoll(faces))
                    .pos(startX + (i * (quickButtonWidth + spacing)), quickButtonsY)
                    .size(quickButtonWidth, 20)
                    .build();

            this.addRenderableWidget(diceBtn);
        }

        this.rollformula = new EditBox(this.font, centerX - (MENU_WIDTH / 2), centerY - 10, MENU_WIDTH, 20, Component.literal("Fórmula"));
        this.rollformula.setMaxLength(20);
        this.rollformula.setValue("1d20");
        this.rollformula.setHint(Component.literal("Ex: 1d20"));
        this.rollformula.setResponder(text -> this.updateButtonState());
        this.addRenderableWidget(this.rollformula);

        this.privateRoll = Checkbox.builder(Component.literal("Rolagem Privada"), this.font)
                .pos(centerX - (MENU_WIDTH / 2), centerY + 20)
                .selected(false)
                .build();
        this.addRenderableWidget(this.privateRoll);

        this.rollButton = Button.builder(Component.literal("Rolar"), button -> rollManual())
                .pos(centerX - 105, centerY + 50)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.rollButton.active = true;
        this.addRenderableWidget(this.rollButton);

        this.cancelButton = Button.builder(Component.literal("Cancelar"), button -> onClose())
                .pos(centerX + 5, centerY + 50)
                .size(BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(this.cancelButton);

        this.setInitialFocus(this.rollformula);
    }

    private void updateButtonState() {
        this.rollButton.active = isValidFormula(this.rollformula.getValue());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);

        gui.drawCenteredString(this.font, ChatFormatting.LIGHT_PURPLE + "FurSMP" + ChatFormatting.WHITE + " - Rolagem de Dados", this.width / 2, this.height / 2 - 85, 0xFFFFFF);
        gui.drawCenteredString(this.font, ChatFormatting.GOLD + "Rolagem Rapida:", this.width / 2, this.height / 2 - 65, 0xFFFFFF);

        gui.drawCenteredString(this.font, ChatFormatting.WHITE + "Formula Personalizada:", this.width / 2, this.height / 2 - 22, 0xFFFFFF);
    }

    private void performQuickRoll(int faces) {
        String formula = "1d" + faces;
        boolean isPrivate = this.privateRoll.selected();

        PacketDistributor.sendToServer(new RollDiceC2SPacket(formula, isPrivate));
        this.onClose();
    }

    public void rollManual() {
        String formula = this.rollformula.getValue();
        if (formula.isEmpty() || !isValidFormula(formula)) return;

        boolean isPrivate = this.privateRoll.selected();
        PacketDistributor.sendToServer(new RollDiceC2SPacket(formula, isPrivate));
        this.onClose();
    }

    public boolean isValidFormula(String formula) {
        if (formula == null || formula.isEmpty()) return false;
        String cleanFormula = formula.trim().toLowerCase();

        if (!cleanFormula.matches("^\\d+d\\d+$")) return false;

        try {
            String[] parts = cleanFormula.split("d");
            int qtd = Integer.parseInt(parts[0]);
            int faces = Integer.parseInt(parts[1]);
            return (qtd > 0 && qtd <= 10) && (faces > 0 && faces <= 100);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}