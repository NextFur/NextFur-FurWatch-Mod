package net.nextfur.fwc.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public class GameruleMenuScreen extends Screen {

    // TODO: Esse treco todo tá meio gambiarra, refatorar depois


    private Checkbox dayCycleToggle;
    private Checkbox mobSpawnToggle;
    private Checkbox keepInventoryToggle;

    public GameruleMenuScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int centerY = height / 2;
        int spacing = 22;
        int startY = centerY - 100;

        dayCycleToggle = Checkbox.builder(Component.literal("Ciclo Diurno"), font)
                .pos(centerX - 150, startY)
                .selected(getGamerule(GameRules.RULE_DAYLIGHT))
                .build();
        mobSpawnToggle = Checkbox.builder(Component.literal("Spawn de Mobs"), font)
                .pos(centerX, startY)
                .selected(getGamerule(GameRules.RULE_DOMOBSPAWNING))
                .build();
        keepInventoryToggle = Checkbox.builder(Component.literal("KeepInventory"), font)
                .pos(centerX + 150, startY)
                .selected(getGamerule(GameRules.RULE_KEEPINVENTORY))
                .build();

        addRenderableWidget(dayCycleToggle);
        addRenderableWidget(mobSpawnToggle);
        addRenderableWidget(keepInventoryToggle);

        startY += spacing * 3;
        addRenderableWidget(Button.builder(Component.literal(ChatFormatting.GREEN + "Aplicar"), b -> applyGamerules())
                .pos(centerX - 75, startY).size(150, 20).build());
    }

    private void sendCommand(String command) {
        if (Minecraft.getInstance().player != null)
            Minecraft.getInstance().player.connection.sendCommand(command);
    }

    private Level getLevel() {
        return Minecraft.getInstance().level;
    }

    private boolean getGamerule(GameRules.Key<GameRules.BooleanValue> key) {
        Level level = getLevel();
        return level != null && level.getGameRules().getBoolean(key);
    }

    private void applyGamerules() {
        if (dayCycleToggle.selected() != getGamerule(GameRules.RULE_DAYLIGHT))
            sendCommand("gamerule doDaylightCycle " + dayCycleToggle.selected());
        if (mobSpawnToggle.selected() != getGamerule(GameRules.RULE_DOMOBSPAWNING))
            sendCommand("gamerule doMobSpawning " + mobSpawnToggle.selected());
        if (keepInventoryToggle.selected() != getGamerule(GameRules.RULE_KEEPINVENTORY))
            sendCommand("gamerule keepInventory " + keepInventoryToggle.selected());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        gui.drawCenteredString(font, ChatFormatting.BOLD + "Painel Gamerules", width / 2, 40, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
