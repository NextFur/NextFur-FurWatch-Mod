package net.nextfur.fwc.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TitleMenuScreen extends Screen {
    private EditBox textBox;
    private EditBox targetBox;
    private EditBox colorBox;
    private EditBox subtitleBox;
    private Button previewButton;
    private Button sendButton;
    private Button cancelButton;

    public TitleMenuScreen(Component menuname) {
        super(menuname);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int boxWidth = 220;
        int boxHeight = 20;

        textBox = new EditBox(this.font, centerX - boxWidth / 2, centerY - 70, boxWidth, boxHeight, Component.literal("Título"));
        subtitleBox = new EditBox(this.font, centerX - boxWidth / 2, centerY - 40, boxWidth, boxHeight, Component.literal("Subtítulo"));
        targetBox = new EditBox(this.font, centerX - boxWidth / 2, centerY - 10, boxWidth, boxHeight, Component.literal("Alvo"));
        colorBox = new EditBox(this.font, centerX - boxWidth / 2, centerY + 20, boxWidth, boxHeight, Component.literal("Cor (#RRGGBB)"));

        textBox.setMaxLength(200); // ;3
        subtitleBox.setMaxLength(200);
        targetBox.setMaxLength(32);
        colorBox.setMaxLength(7); // #123456

        textBox.setHint(Component.literal("Titulo *"));
        subtitleBox.setHint(Component.literal("Subtitulo (Opcional)"));
        targetBox.setHint(Component.literal("Seletor"));
        colorBox.setHint(Component.literal("Cor"));

        targetBox.setValue("@a");
        colorBox.setValue("#FFFFFF"); // Branquinhu

        previewButton = Button.builder(Component.literal(ChatFormatting.GREEN + "Preview"), b -> previewTitle())
                .pos(centerX - 110, centerY + 60).size(100, 20).build();

        sendButton = Button.builder(Component.literal(ChatFormatting.GOLD + "Enviar"), b -> sendTitle())
                .pos(centerX + 10, centerY + 60).size(100, 20).build();

        cancelButton = Button.builder(Component.literal(ChatFormatting.RED + "Cancelar"), b -> onClose())
                .pos(centerX - 50, centerY + 90).size(100, 20).build();

        addRenderableWidget(textBox);
        addRenderableWidget(subtitleBox);
        addRenderableWidget(targetBox);
        addRenderableWidget(colorBox);
        addRenderableWidget(previewButton);
        addRenderableWidget(sendButton);
        addRenderableWidget(cancelButton);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);
        gui.drawCenteredString(this.font, ChatFormatting.LIGHT_PURPLE + "FurSMP - Title Menu", this.width / 2, this.height / 2 - 100, 0xFFFFFF);

        String c = colorBox.getValue();
        if (c.startsWith("#") && c.length() == 7) {
            try {
                int col = (int) Long.parseLong(c.substring(1), 16) | 0xFF000000;
                int x1 = colorBox.getX() + colorBox.getWidth() + 5;
                int y1 = colorBox.getY();
                int x2 = x1 + 20;
                int y2 = y1 + colorBox.getHeight();

                gui.fill(x1, y1, x2, y2, col);
            } catch (Exception ignored) {}
        }
    }

    private void previewTitle() {
        Minecraft mc = Minecraft.getInstance();
        String text = textBox.getValue();
        String subtitle = subtitleBox.getValue();
        String target = "@p"; // preview
        String color = colorBox.getValue();

        mc.player.connection.sendCommand(String.format("title %s title {\"text\":\"%s\",\"color\":\"%s\"}", target, text, color));
        if (!subtitle.isEmpty()) {
            mc.player.connection.sendCommand(String.format("title %s subtitle {\"text\":\"%s\"}", target, subtitle));
        }
    }

    private void sendTitle() {
        Minecraft mc = Minecraft.getInstance();
        String text = textBox.getValue();
        String subtitle = subtitleBox.getValue();
        String target = targetBox.getValue();
        String color = colorBox.getValue();

        mc.player.connection.sendCommand(String.format("title %s title {\"text\":\"%s\",\"color\":\"%s\"}", target, text, color));
        if (!subtitle.isEmpty()) {
            mc.player.connection.sendCommand(String.format("title %s subtitle {\"text\":\"%s\"}", target, subtitle));
        }
        mc.player.connection.sendCommand(String.format("title %s times 10 70 20", target));
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }
}
