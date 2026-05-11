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

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class FurWatchShaderOptionsScreen extends Screen {
    private static final String[] PRESETS = {"balanced", "warm", "moonlit"};

    private final Screen parent;
    private Button enabledButton;
    private Button presetButton;
    private Button occlusionButton;
    private Button postEffectsButton;

    public FurWatchShaderOptionsScreen(Screen parent) {
        super(Component.translatable("screen.fursmp.shader_options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 110;
        int top = this.height / 2 - 132;
        int rowHeight = 22;
        int fullWidth = 220;
        int halfWidth = 106;

        this.enabledButton = this.addRenderableWidget(Button.builder(enabledLabel(), button -> {
            FurWatchShaderState.toggleEnabled();
            FurWatchShaderState.persist();
            refreshLabels();
        }).pos(left, top).size(fullWidth, 20).build());

        this.addRenderableWidget(new LightingSlider(left, top + rowHeight, fullWidth, 20,
                "option.fursmp.shader.global_intensity",
                0.0D,
                2.0D,
                FurWatchShaderState::getGlobalIntensity,
                value -> FurWatchShaderState.setGlobalIntensity((float) value),
                "%.2f"));

        this.addRenderableWidget(new LightingSlider(left, top + rowHeight * 2, fullWidth, 20,
                "option.fursmp.shader.ambient_intensity",
                0.0D,
                1.5D,
                FurWatchShaderState::getAmbientIntensity,
                value -> FurWatchShaderState.setAmbientIntensity((float) value),
                "%.2f"));

        this.addRenderableWidget(new LightingSlider(left, top + rowHeight * 3, fullWidth, 20,
                "option.fursmp.shader.directional_intensity",
                0.0D,
                2.0D,
                FurWatchShaderState::getDirectionalIntensity,
                value -> FurWatchShaderState.setDirectionalIntensity((float) value),
                "%.2f"));

        this.addRenderableWidget(new LightingSlider(left, top + rowHeight * 4, fullWidth, 20,
                "option.fursmp.shader.local_radius",
                8.0D,
                96.0D,
                FurWatchShaderState::getLocalLightRadius,
                value -> FurWatchShaderState.setLocalLightRadius((float) value),
                "%.0f"));

        this.addRenderableWidget(new LightingSlider(left, top + rowHeight * 5, fullWidth, 20,
                "option.fursmp.shader.local_brightness",
                0.1D,
                4.0D,
                FurWatchShaderState::getLocalLightBrightness,
                value -> FurWatchShaderState.setLocalLightBrightness((float) value),
                "%.2f"));

        this.presetButton = this.addRenderableWidget(Button.builder(presetLabel(), button -> {
            cyclePreset();
            FurWatchShaderState.persist();
            refreshLabels();
        }).pos(left, top + rowHeight * 6).size(fullWidth, 20).build());

        this.occlusionButton = this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.occlusion", FurWatchShaderState.isOcclusionEnabled()), button -> {
            FurWatchShaderState.setOcclusionEnabled(!FurWatchShaderState.isOcclusionEnabled());
            FurWatchShaderState.persist();
            refreshLabels();
        }).pos(left, top + rowHeight * 7).size(fullWidth, 20).build());

        this.postEffectsButton = this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.post_effects", FurWatchShaderState.isPostEffectsEnabled()), button -> {
            FurWatchShaderState.setPostEffectsEnabled(!FurWatchShaderState.isPostEffectsEnabled());
            FurWatchShaderState.persist();
            refreshLabels();
        }).pos(left, top + rowHeight * 8).size(fullWidth, 20).build());

        this.addRenderableWidget(new LightingSlider(left, top + rowHeight * 9, fullWidth, 20,
            "option.fursmp.shader.post_strength",
            0.0D,
            1.5D,
            FurWatchShaderState::getPostEffectsStrength,
            value -> FurWatchShaderState.setPostEffectsStrength((float) value),
            "%.2f"));

        this.addRenderableWidget(new LightingSlider(left, top + rowHeight * 10, fullWidth, 20,
            "option.fursmp.shader.blur_strength",
            0.0D,
            1.0D,
            FurWatchShaderState::getBlurStrength,
            value -> FurWatchShaderState.setBlurStrength((float) value),
            "%.2f"));

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
            .pos(left, top + rowHeight * 12).size(halfWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("controls.reset"), button -> {
            FurWatchShaderState.restoreDefaults();
            FurWatchShaderState.persist();
            this.rebuildWidgets();
        }).pos(left + halfWidth + 8, top + rowHeight * 12).size(halfWidth, 20).build());
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
        this.occlusionButton.setMessage(toggleLabel("option.fursmp.shader.occlusion", FurWatchShaderState.isOcclusionEnabled()));
        this.postEffectsButton.setMessage(toggleLabel("option.fursmp.shader.post_effects", FurWatchShaderState.isPostEffectsEnabled()));
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

    private static class LightingSlider extends AbstractSliderButton {
        private final String translationKey;
        private final double minValue;
        private final double maxValue;
        private final DoubleConsumer setter;
        private final String format;

        private LightingSlider(int x, int y, int width, int height, String translationKey, double minValue, double maxValue, DoubleSupplier getter, DoubleConsumer setter, String format) {
            super(x, y, width, height, Component.empty(), normalize(getter.getAsDouble(), minValue, maxValue));
            this.translationKey = translationKey;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.setter = setter;
            this.format = format;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable(this.translationKey, String.format(this.format, denormalize(this.value, this.minValue, this.maxValue))));
        }

        @Override
        protected void applyValue() {
            this.setter.accept(denormalize(this.value, this.minValue, this.maxValue));
            FurWatchShaderState.persist();
            this.updateMessage();
        }

        private static double normalize(double value, double minValue, double maxValue) {
            return Mth.clamp((value - minValue) / (maxValue - minValue), 0.0D, 1.0D);
        }

        private static double denormalize(double sliderValue, double minValue, double maxValue) {
            return Mth.clamp(minValue + ((maxValue - minValue) * sliderValue), minValue, maxValue);
        }
    }
}