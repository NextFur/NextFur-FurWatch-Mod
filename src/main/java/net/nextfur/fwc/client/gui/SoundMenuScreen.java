package net.nextfur.fwc.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SoundMenuScreen extends Screen {
    private EditBox soundIdBox;
    private EditBox durationBox;
    private EditBox radiusBox;
    private EditBox fadeInBox;
    private EditBox fadeOutBox;

    private float volume = 1.0f;
    private boolean isPlaying = false;
    private long startTime = 0;
    private int maxDurationSeconds = 60;

    public SoundMenuScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int boxWidth = 200;
        int boxHeight = 20;

        this.soundIdBox = new EditBox(this.font, centerX - 100, centerY - 80, boxWidth, boxHeight, Component.literal("Sound ID/URL"));
        this.soundIdBox.setMaxLength(256);
        this.soundIdBox.setHint(Component.literal("URL do Som..."));
        this.addRenderableWidget(this.soundIdBox);

        this.durationBox = new EditBox(this.font, centerX - 100, centerY - 55, 95, boxHeight, Component.literal("Duration"));
        this.durationBox.setValue("60");
        this.durationBox.setHint(Component.literal("Duracao (s)"));
        this.addRenderableWidget(this.durationBox);

        this.radiusBox = new EditBox(this.font, centerX + 5, centerY - 55, 95, boxHeight, Component.literal("Radius"));
        this.radiusBox.setValue("32");
        this.radiusBox.setHint(Component.literal("Raio"));
        this.addRenderableWidget(this.radiusBox);

        this.addRenderableWidget(new AbstractSliderButton(centerX - 100, centerY - 30, boxWidth, 20, Component.literal("Volume: 100%"), 1.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Volume: " + (int)(this.value * 100) + "%"));
            }

            @Override
            protected void applyValue() {
                SoundMenuScreen.this.volume = (float) this.value;
            }
        });

        this.fadeInBox = new EditBox(this.font, centerX - 100, centerY - 5, 95, boxHeight, Component.literal("Fade In"));
        this.fadeInBox.setHint(Component.literal("Fade In (s)"));
        this.addRenderableWidget(this.fadeInBox);

        this.fadeOutBox = new EditBox(this.font, centerX + 5, centerY - 5, 95, boxHeight, Component.literal("Fade Out"));
        this.fadeOutBox.setHint(Component.literal("Fade Out (s)"));
        this.addRenderableWidget(this.fadeOutBox);

        this.addRenderableWidget(Button.builder(Component.literal("PLAY"), button -> {
            sendSoundPacket("PLAY");
            this.isPlaying = true;
            this.startTime = System.currentTimeMillis();
            try {
                this.maxDurationSeconds = Integer.parseInt(this.durationBox.getValue());
            } catch (NumberFormatException e) {
                this.maxDurationSeconds = 60;
            }
        }).bounds(centerX - 105, centerY + 50, 60, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("PAUSE"), button -> {
            sendSoundPacket("PAUSE");
            this.isPlaying = false;
        }).bounds(centerX - 30, centerY + 50, 60, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("STOP"), button -> {
            sendSoundPacket("STOP");
            this.isPlaying = false;
            this.startTime = 0;
        }).bounds(centerX + 45, centerY + 50, 60, 20).build());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        gui.drawCenteredString(this.font, this.title, centerX, centerY - 110, 0xFFFFFF);

        renderTrackline(gui, centerX, centerY + 30);
    }

    private void renderTrackline(GuiGraphics gui, int centerX, int y) {
        int width = 200;
        int startX = centerX - (width / 2);

        gui.fill(startX, y, startX + width, y + 4, 0xFF444444);

        if (this.isPlaying && this.maxDurationSeconds > 0) {
            long elapsed = System.currentTimeMillis() - this.startTime;
            float percent = (float) elapsed / (this.maxDurationSeconds * 1000f);

            if (percent > 1.0f) {
                percent = 1.0f;
            }

            int progressWidth = (int) (width * percent);
            gui.fill(startX, y, startX + progressWidth, y + 4, 0xFF55FF55);
        }
    }

    private void sendSoundPacket(String action) {
        String url = this.soundIdBox.getValue();
        int radius = 32;
        int fadeIn = 0;
        int fadeOut = 0;

        try {
            radius = Integer.parseInt(this.radiusBox.getValue());
            fadeIn = Integer.parseInt(this.fadeInBox.getValue());
            fadeOut = Integer.parseInt(this.fadeOutBox.getValue());
        } catch (Exception ignored) {}

        // TODO:
        // PacketDistributor.sendToServer();
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
