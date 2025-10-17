package net.nextfur.fwc.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.network.world.SkyColorChangePacket;

public class SkyColorMenuScreen extends Screen {
    private EditBox colorBox;
    private EditBox colorFog;
    private Button applyButton;
    private Button cancelButton;
    private Button resetButton;

    public SkyColorMenuScreen(Component title) {
        super(Component.literal(ChatFormatting.AQUA + "FurWatch - SkyColor ;3"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int boxWidth = 220;
        int boxHeight = 20;

        colorBox = new EditBox(this.font, centerX - boxWidth / 2, centerY - 10, boxWidth, boxHeight, Component.literal("Cor (#RRGGBB)"));
        colorBox.setMaxLength(7);
        colorBox.setHint(Component.literal("SkyBox Color #HEX"));

        colorFog = new EditBox(this.font, centerX - boxWidth / 2, centerY + 20, boxWidth, boxHeight, Component.literal("Cor (#RRGGBB)"));
        colorFog.setMaxLength(7);
        colorFog.setHint(Component.literal("Fog/Clouds Color #HEX"));

        resetButton = Button.builder(Component.literal(ChatFormatting.GOLD + "Resetar"), b -> resetSky())
                .pos(centerX + 10, centerY + 60).size(100, 20).build();

        applyButton = Button.builder(Component.literal(ChatFormatting.GREEN + "Aplicar"), b -> modifySky())
                .pos(centerX - 110, centerY + 60).size(100, 20).build();

        cancelButton = Button.builder(Component.literal(ChatFormatting.RED + "Cancelar"), b -> onClose())
                .pos(centerX - 50, centerY + 90).size(100, 20).build();


        addRenderableWidget(colorBox);
        addRenderableWidget(colorFog);
        addRenderableWidget(resetButton);
        addRenderableWidget(applyButton);
        addRenderableWidget(cancelButton);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        gui.drawCenteredString(this.font, ChatFormatting.LIGHT_PURPLE + "FurSMP - SkyColor", this.width / 2, this.height / 2 - 100, 0xFFFFFF);

        // Preview SkyBox color
        String boxVal = colorBox.getValue();
        if (boxVal.startsWith("#") && boxVal.length() == 7) {
            try {
                int col = (int) Long.parseLong(boxVal.substring(1), 16) | 0xFF000000;
                int x1 = colorBox.getX() + colorBox.getWidth() + 5;
                int y1 = colorBox.getY();
                int x2 = x1 + 20;
                int y2 = y1 + colorBox.getHeight();
                gui.fill(x1, y1, x2, y2, col);
            } catch (Exception ignored) {}
        }

        // Preview Fog color
        String fogVal = colorFog.getValue();
        if (fogVal.startsWith("#") && fogVal.length() == 7) {
            try {
                int col = (int) Long.parseLong(fogVal.substring(1), 16) | 0xFF000000;
                int x1 = colorFog.getX() + colorFog.getWidth() + 5;
                int y1 = colorFog.getY();
                int x2 = x1 + 20;
                int y2 = y1 + colorFog.getHeight();
                gui.fill(x1, y1, x2, y2, col);
            } catch (Exception ignored) {}
        }
    }


    private void resetSky() {
        PacketDistributor.sendToServer(new SkyColorChangePacket(-1, -1));
        this.onClose();
    }

    private void modifySky() {
        String fog = colorFog.getValue();
        String box = colorBox.getValue();

        int colorFog = 0;
        int colorBox = 0;

        if (fog.startsWith("#") && fog.length() == 7) {
            try {
                colorFog = (int) Long.parseLong(fog.substring(1), 16) | 0xFF000000;
            } catch (Exception ignored) {}
        }

        if (box.startsWith("#") && box.length() == 7) {
            try {
                colorBox = (int) Long.parseLong(box.substring(1), 16) | 0xFF000000;
            } catch (Exception ignored) {}
        }

        PacketDistributor.sendToServer(new SkyColorChangePacket(colorFog, colorBox));
        this.onClose();
    }

}
