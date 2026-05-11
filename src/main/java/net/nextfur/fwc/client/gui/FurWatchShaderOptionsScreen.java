package net.nextfur.fwc.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.nextfur.fwc.util.client.FurWatchShaderState;

public class FurWatchShaderOptionsScreen extends Screen {
    private static final String[] PRESETS = {"default", "cinematic", "surveillance"};

    private final Screen parent;
    private Button enabledButton;
    private Button presetButton;
    private Button filmGrainButton;
    private Button vignetteButton;
    private Button scanlinesButton;
    private Button chromaticAberrationButton;

    public FurWatchShaderOptionsScreen(Screen parent) {
        super(Component.translatable("screen.fursmp.shader_options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 110;
        int top = this.height / 2 - 94;
        int rowHeight = 24;
        int fullWidth = 220;
        int halfWidth = 106;

        this.enabledButton = this.addRenderableWidget(Button.builder(enabledLabel(), button -> {
            FurWatchShaderState.toggleEnabled();
            FurWatchShaderState.persist();
            refreshLabels();
        }).pos(left, top).size(fullWidth, 20).build());

        this.addRenderableWidget(new IntensitySlider(left, top + rowHeight, fullWidth, 20));

        this.presetButton = this.addRenderableWidget(Button.builder(presetLabel(), button -> {
            cyclePreset();
            FurWatchShaderState.persist();
            refreshLabels();
        }).pos(left, top + rowHeight * 2).size(fullWidth, 20).build());

        this.filmGrainButton = this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.film_grain", FurWatchShaderState.isFilmGrainEnabled()), button -> {
            FurWatchShaderState.setFilmGrainEnabled(!FurWatchShaderState.isFilmGrainEnabled());
            FurWatchShaderState.persist();
            refreshLabels();
        }).pos(left, top + rowHeight * 3).size(halfWidth, 20).build());

        this.vignetteButton = this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.vignette", FurWatchShaderState.isVignetteEnabled()), button -> {
            FurWatchShaderState.setVignetteEnabled(!FurWatchShaderState.isVignetteEnabled());
            FurWatchShaderState.persist();
            refreshLabels();
        }).pos(left + halfWidth + 8, top + rowHeight * 3).size(halfWidth, 20).build());

        this.scanlinesButton = this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.scanlines", FurWatchShaderState.isScanlinesEnabled()), button -> {
            FurWatchShaderState.setScanlinesEnabled(!FurWatchShaderState.isScanlinesEnabled());
            FurWatchShaderState.persist();
            refreshLabels();
        }).pos(left, top + rowHeight * 4).size(halfWidth, 20).build());

        this.chromaticAberrationButton = this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.chromatic_aberration", FurWatchShaderState.isChromaticAberrationEnabled()), button -> {
            FurWatchShaderState.setChromaticAberrationEnabled(!FurWatchShaderState.isChromaticAberrationEnabled());
            FurWatchShaderState.persist();
            refreshLabels();
        }).pos(left + halfWidth + 8, top + rowHeight * 4).size(halfWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .pos(left, top + rowHeight * 6).size(halfWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("controls.reset"), button -> {
            FurWatchShaderState.restoreDefaults();
            FurWatchShaderState.persist();
            this.rebuildWidgets();
        }).pos(left + halfWidth + 8, top + rowHeight * 6).size(halfWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, ChatFormatting.AQUA + this.title.getString(), this.width / 2, this.height / 2 - 120, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.translatable("screen.fursmp.shader_options.subtitle").getString(), this.width / 2, this.height / 2 - 102, 0xA0A0A0);
    }

    @Override
    public void onClose() {
        FurWatchShaderState.persist();
        Minecraft.getInstance().setScreen(this.parent);
    }

    private void cyclePreset() {
        int currentIndex = 0;
        String currentPreset = FurWatchShaderState.getPreset();
        for (int index = 0; index < PRESETS.length; index++) {
            if (PRESETS[index].equals(currentPreset)) {
                currentIndex = index;
                break;
            }
        }
        FurWatchShaderState.setPreset(PRESETS[(currentIndex + 1) % PRESETS.length]);
    }

    private void refreshLabels() {
        this.enabledButton.setMessage(enabledLabel());
        this.presetButton.setMessage(presetLabel());
        this.filmGrainButton.setMessage(toggleLabel("option.fursmp.shader.film_grain", FurWatchShaderState.isFilmGrainEnabled()));
        this.vignetteButton.setMessage(toggleLabel("option.fursmp.shader.vignette", FurWatchShaderState.isVignetteEnabled()));
        this.scanlinesButton.setMessage(toggleLabel("option.fursmp.shader.scanlines", FurWatchShaderState.isScanlinesEnabled()));
        this.chromaticAberrationButton.setMessage(toggleLabel("option.fursmp.shader.chromatic_aberration", FurWatchShaderState.isChromaticAberrationEnabled()));
    }

    private Component enabledLabel() {
        return toggleLabel("option.fursmp.shader.enabled", FurWatchShaderState.isEnabled());
    }

    private Component presetLabel() {
        return Component.translatable("option.fursmp.shader.preset", Component.translatable("option.fursmp.shader.preset." + FurWatchShaderState.getPreset()));
    }

    private Component toggleLabel(String key, boolean enabled) {
        return Component.translatable(key, Component.translatable(enabled ? "options.on" : "options.off"));
    }

    private static class IntensitySlider extends AbstractSliderButton {
        private IntensitySlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), normalize(FurWatchShaderState.getIntensity()));
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable("option.fursmp.shader.intensity", String.format("%.2f", denormalize(this.value))));
        }

        @Override
        protected void applyValue() {
            FurWatchShaderState.setIntensity(denormalize(this.value));
            FurWatchShaderState.persist();
            this.updateMessage();
        }

        private static double normalize(float intensity) {
            return Mth.clamp(intensity / 2.0F, 0.0F, 1.0F);
        }

        private static float denormalize(double sliderValue) {
            return (float) Mth.clamp(sliderValue * 2.0D, 0.0D, 2.0D);
        }
    }
}