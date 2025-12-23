package net.nextfur.fwc.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.network.world.SoundControlPacket;

public class SoundMenuScreen extends Screen {
    private EditBox soundIdBox;
    private EditBox radiusBox;
    private EditBox fadeInBox;
    private EditBox fadeOutBox;
    private Checkbox loopCheckbox;

    private float volume = 1.0f;
    private boolean isPlaying = false;
    private long startTime = 0;

    public SoundMenuScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int boxWidth = 200;
        int boxHeight = 20;

        this.soundIdBox = new EditBox(this.font, centerX - 100, centerY - 80, boxWidth, boxHeight, Component.literal("Sound Url"));
        this.soundIdBox.setMaxLength(256);
        this.soundIdBox.setHint(Component.literal("URL do Som..."));
        this.addRenderableWidget(this.soundIdBox);

        this.radiusBox = new EditBox(this.font, centerX - 100, centerY - 45, 95, boxHeight, Component.literal("Radius"));
        this.radiusBox.setValue("32");
        this.radiusBox.setHint(Component.literal("Raio"));
        this.addRenderableWidget(this.radiusBox);

        this.addRenderableWidget(new AbstractSliderButton(centerX + 5, centerY - 45, 95, boxHeight, Component.literal("Volume: 100%"), 1.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Volume: " + (int)(this.value * 100) + "%"));
            }

            @Override
            protected void applyValue() {
                SoundMenuScreen.this.volume = (float) this.value;
            }
        });

        this.fadeInBox = new EditBox(this.font, centerX - 100, centerY - 10, 95, boxHeight, Component.literal("Fade In"));
        this.fadeInBox.setHint(Component.literal("Fade In (s)"));
        this.fadeInBox.setValue("0");
        this.addRenderableWidget(this.fadeInBox);

        this.fadeOutBox = new EditBox(this.font, centerX + 5, centerY - 10, 95, boxHeight, Component.literal("Fade Out"));
        this.fadeOutBox.setHint(Component.literal("Fade Out (s)"));
        this.fadeOutBox.setValue("0");
        this.addRenderableWidget(this.fadeOutBox);

        this.loopCheckbox = Checkbox.builder(Component.literal("Looping"), this.font)
                .pos(centerX - 100, centerY + 20)
                .build();
        this.addRenderableWidget(this.loopCheckbox);

        int btnY = centerY + 50;

        this.addRenderableWidget(Button.builder(Component.literal("PLAY"), button -> {
            sendSoundPacket("PLAY");
            this.isPlaying = true;
            this.startTime = System.currentTimeMillis();
        }).bounds(centerX - 105, btnY, 60, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("PAUSE"), button -> {
            String action = this.isPlaying ? "PAUSE" : "RESUME";
            sendSoundPacket(action);

            this.isPlaying = !this.isPlaying;
            button.setMessage(Component.literal(this.isPlaying ? "PAUSE" : "RESUME"));
        }).bounds(centerX - 30, btnY, 60, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("STOP"), button -> {
            sendSoundPacket("STOP");
            this.isPlaying = false;
            this.startTime = 0;
        }).bounds(centerX + 45, btnY, 60, 20).build());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.render(gui, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        gui.drawCenteredString(this.font, this.title, centerX, centerY - 110, 0xFFFFFF);

        gui.drawString(this.font, "Sound URL:", centerX - 100, centerY - 92, 0xA0A0A0, false);
        gui.drawString(this.font, "Raio:", centerX - 100, centerY - 57, 0xA0A0A0, false);
        gui.drawString(this.font, "Fade In (s):", centerX - 100, centerY - 22, 0xA0A0A0, false);
        gui.drawString(this.font, "Fade Out (s):", centerX + 5, centerY - 22, 0xA0A0A0, false);

        if (this.isPlaying) {
            gui.drawCenteredString(this.font, Component.literal("Tocando..."), centerX, centerY + 80, 0x00ff00);
        }
    }

    private void sendSoundPacket(String action) {
        String url = this.soundIdBox.getValue();
        if (url == null || url.trim().isEmpty() && !action.equals("STOP")) {
            return;
        }

        int radius = 32, fadeIn = 0, fadeOut = 0;

        try {
            if (!this.radiusBox.getValue().isEmpty()) radius = Integer.parseInt(this.radiusBox.getValue());
            if (!this.fadeInBox.getValue().isEmpty()) fadeIn = Integer.parseInt(this.fadeInBox.getValue());
            if (!this.fadeOutBox.getValue().isEmpty()) fadeOut = Integer.parseInt(this.fadeOutBox.getValue());
        } catch (NumberFormatException ignored) { return; }

        boolean isLooping = this.loopCheckbox.selected();

        PacketDistributor.sendToServer(new SoundControlPacket(
                action,
                url,
                this.volume,
                radius,
                fadeIn,
                fadeOut,
                isLooping
        ));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        Minecraft.getInstance().setScreen(null);
    }
}
