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

    public enum Tab {
        GENERAL("option.fursmp.shader.tab.general"),
        STARS("option.fursmp.shader.tab.stars"),
        LIGHTING("option.fursmp.shader.tab.lighting"),
        ATMOSPHERE("option.fursmp.shader.tab.atmosphere"),
        EFFECTS("option.fursmp.shader.tab.effects");

        private final String translationKey;

        Tab(String translationKey) {
            this.translationKey = translationKey;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }
    }

    private static Tab currentTab = Tab.GENERAL;
    private final Screen parent;

    public FurWatchShaderOptionsScreen(Screen parent) {
        super(Component.translatable("screen.fursmp.shader_options"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int tabWidth = 60;
        int tabSpacing = 4;
        int totalTabsWidth = (Tab.values().length * tabWidth) + ((Tab.values().length - 1) * tabSpacing);
        int tabStartX = this.width / 2 - (totalTabsWidth / 2);
        int tabY = 28;

        for (int i = 0; i < Tab.values().length; i++) {
            Tab tab = Tab.values()[i];
            int x = tabStartX + i * (tabWidth + tabSpacing);
            Component tabText = Component.translatable(tab.getTranslationKey());
            if (currentTab == tab) {
                tabText = Component.literal("[").withStyle(ChatFormatting.YELLOW)
                        .append(tabText.copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                        .append(Component.literal("]").withStyle(ChatFormatting.YELLOW));
            }
            this.addRenderableWidget(Button.builder(tabText, button -> {
                currentTab = tab;
                this.rebuildWidgets();
            }).pos(x, tabY).size(tabWidth, 20).build());
        }

        int left = this.width / 2 - 120;
        int contentTop = 56;
        int rowHeight = 24;
        int fullWidth = 240;
        int halfWidth = 116;

        switch (currentTab) {
            case GENERAL -> {
                this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.enabled", FurWatchShaderState.isEnabled()), button -> {
                    FurWatchShaderState.toggleEnabled();
                    FurWatchShaderState.persist();
                    this.rebuildWidgets();
                }).pos(left, contentTop).size(fullWidth, 20).build());

                this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.disable_on_iris", FurWatchShaderState.isDisableOnIris()), button -> {
                    FurWatchShaderState.setDisableOnIris(!FurWatchShaderState.isDisableOnIris());
                    FurWatchShaderState.persist();
                    this.rebuildWidgets();
                }).pos(left, contentTop + rowHeight).size(fullWidth, 20).build());

                this.addRenderableWidget(Button.builder(presetLabel(), button -> {
                    cyclePreset();
                    FurWatchShaderState.persist();
                    this.rebuildWidgets();
                }).pos(left, contentTop + rowHeight * 2).size(fullWidth, 20).build());

                this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.occlusion", FurWatchShaderState.isOcclusionEnabled()), button -> {
                    FurWatchShaderState.setOcclusionEnabled(!FurWatchShaderState.isOcclusionEnabled());
                    FurWatchShaderState.persist();
                    this.rebuildWidgets();
                }).pos(left, contentTop + rowHeight * 3).size(fullWidth, 20).build());
            }
            case STARS -> {
                this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.stars", FurWatchShaderState.isStarsEnabled()), button -> {
                    FurWatchShaderState.setStarsEnabled(!FurWatchShaderState.isStarsEnabled());
                    FurWatchShaderState.persist();
                    this.rebuildWidgets();
                }).pos(left, contentTop).size(fullWidth, 20).build());

                this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.celestial_sphere", FurWatchShaderState.isCelestialSphere()), button -> {
                    FurWatchShaderState.setCelestialSphere(!FurWatchShaderState.isCelestialSphere());
                    FurWatchShaderState.persist();
                    this.rebuildWidgets();
                }).pos(left, contentTop + rowHeight).size(fullWidth, 20).build());

                this.addRenderableWidget(new LightingSlider(left, contentTop + rowHeight * 2, fullWidth, 20,
                        "option.fursmp.shader.star_brightness",
                        0.0D,
                        3.0D,
                        FurWatchShaderState::getStarBrightness,
                        value -> FurWatchShaderState.setStarBrightness((float) value),
                        "%.2f"));

                this.addRenderableWidget(new LightingSlider(left, contentTop + rowHeight * 3, fullWidth, 20,
                        "option.fursmp.shader.star_twinkle",
                        0.0D,
                        1.0D,
                        FurWatchShaderState::getStarTwinkle,
                        value -> FurWatchShaderState.setStarTwinkle((float) value),
                        "%.2f"));
            }
            case LIGHTING -> {
                this.addRenderableWidget(new LightingSlider(left, contentTop, fullWidth, 20,
                        "option.fursmp.shader.global_intensity",
                        0.0D,
                        2.0D,
                        FurWatchShaderState::getGlobalIntensity,
                        value -> FurWatchShaderState.setGlobalIntensity((float) value),
                        "%.2f"));

                this.addRenderableWidget(new LightingSlider(left, contentTop + rowHeight, fullWidth, 20,
                        "option.fursmp.shader.ambient_intensity",
                        0.0D,
                        1.5D,
                        FurWatchShaderState::getAmbientIntensity,
                        value -> FurWatchShaderState.setAmbientIntensity((float) value),
                        "%.2f"));

                this.addRenderableWidget(new LightingSlider(left, contentTop + rowHeight * 2, fullWidth, 20,
                        "option.fursmp.shader.directional_intensity",
                        0.0D,
                        2.0D,
                        FurWatchShaderState::getDirectionalIntensity,
                        value -> FurWatchShaderState.setDirectionalIntensity((float) value),
                        "%.2f"));

                this.addRenderableWidget(new LightingSlider(left, contentTop + rowHeight * 3, halfWidth, 20,
                        "option.fursmp.shader.local_radius",
                        8.0D,
                        96.0D,
                        FurWatchShaderState::getLocalLightRadius,
                        value -> FurWatchShaderState.setLocalLightRadius((float) value),
                        "%.0f"));

                this.addRenderableWidget(new LightingSlider(left + halfWidth + 8, contentTop + rowHeight * 3, halfWidth, 20,
                        "option.fursmp.shader.local_brightness",
                        0.1D,
                        4.0D,
                        FurWatchShaderState::getLocalLightBrightness,
                        value -> FurWatchShaderState.setLocalLightBrightness((float) value),
                        "%.2f"));

                this.addRenderableWidget(new LightingSlider(left, contentTop + rowHeight * 4, fullWidth, 20,
                        "option.fursmp.shader.light_variation",
                        0.0D,
                        1.0D,
                        FurWatchShaderState::getLightVariation,
                        value -> FurWatchShaderState.setLightVariation((float) value),
                        "%.2f"));
            }
            case ATMOSPHERE -> {
                this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.water_effects", FurWatchShaderState.isWaterEffectsEnabled()), button -> {
                    FurWatchShaderState.setWaterEffectsEnabled(!FurWatchShaderState.isWaterEffectsEnabled());
                    FurWatchShaderState.persist();
                    this.rebuildWidgets();
                }).pos(left, contentTop).size(fullWidth, 20).build());

                this.addRenderableWidget(new LightingSlider(left, contentTop + rowHeight, halfWidth, 20,
                        "option.fursmp.shader.reflection_strength",
                        0.0D,
                        1.5D,
                        FurWatchShaderState::getReflectionStrength,
                        value -> FurWatchShaderState.setReflectionStrength((float) value),
                        "%.2f"));

                this.addRenderableWidget(new LightingSlider(left + halfWidth + 8, contentTop + rowHeight, halfWidth, 20,
                        "option.fursmp.shader.reflection_softness",
                        0.0D,
                        1.0D,
                        FurWatchShaderState::getReflectionSoftness,
                        value -> FurWatchShaderState.setReflectionSoftness((float) value),
                        "%.2f"));

                this.addRenderableWidget(new LightingSlider(left, contentTop + rowHeight * 2, halfWidth, 20,
                        "option.fursmp.shader.fog_intensity",
                        0.0D,
                        1.5D,
                        FurWatchShaderState::getFogIntensity,
                        value -> FurWatchShaderState.setFogIntensity((float) value),
                        "%.2f"));

                this.addRenderableWidget(new LightingSlider(left + halfWidth + 8, contentTop + rowHeight * 2, halfWidth, 20,
                        "option.fursmp.shader.fog_variation",
                        0.0D,
                        1.0D,
                        FurWatchShaderState::getFogVariation,
                        value -> FurWatchShaderState.setFogVariation((float) value),
                        "%.2f"));
            }
            case EFFECTS -> {
                this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.post_effects", FurWatchShaderState.isPostEffectsEnabled()), button -> {
                    FurWatchShaderState.setPostEffectsEnabled(!FurWatchShaderState.isPostEffectsEnabled());
                    FurWatchShaderState.persist();
                    this.rebuildWidgets();
                }).pos(left, contentTop).size(fullWidth, 20).build());

                this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.film_grain", FurWatchShaderState.isFilmGrainEnabled()), button -> {
                    FurWatchShaderState.setFilmGrainEnabled(!FurWatchShaderState.isFilmGrainEnabled());
                    FurWatchShaderState.persist();
                    this.rebuildWidgets();
                }).pos(left, contentTop + rowHeight).size(halfWidth, 20).build());

                this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.vignette", FurWatchShaderState.isVignetteEnabled()), button -> {
                    FurWatchShaderState.setVignetteEnabled(!FurWatchShaderState.isVignetteEnabled());
                    FurWatchShaderState.persist();
                    this.rebuildWidgets();
                }).pos(left + halfWidth + 8, contentTop + rowHeight).size(halfWidth, 20).build());

                this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.chromatic_aberration", FurWatchShaderState.isChromaticAberrationEnabled()), button -> {
                    FurWatchShaderState.setChromaticAberrationEnabled(!FurWatchShaderState.isChromaticAberrationEnabled());
                    FurWatchShaderState.persist();
                    this.rebuildWidgets();
                }).pos(left, contentTop + rowHeight * 2).size(halfWidth, 20).build());

                this.addRenderableWidget(Button.builder(toggleLabel("option.fursmp.shader.scanlines", FurWatchShaderState.isScanlinesEnabled()), button -> {
                    FurWatchShaderState.setScanlinesEnabled(!FurWatchShaderState.isScanlinesEnabled());
                    FurWatchShaderState.persist();
                    this.rebuildWidgets();
                }).pos(left + halfWidth + 8, contentTop + rowHeight * 2).size(halfWidth, 20).build());

                this.addRenderableWidget(new LightingSlider(left, contentTop + rowHeight * 3, fullWidth, 20,
                        "option.fursmp.shader.post_strength",
                        0.0D,
                        1.5D,
                        FurWatchShaderState::getPostEffectsStrength,
                        value -> FurWatchShaderState.setPostEffectsStrength((float) value),
                        "%.2f"));

                this.addRenderableWidget(new LightingSlider(left, contentTop + rowHeight * 4, fullWidth, 20,
                        "option.fursmp.shader.blur_strength",
                        0.0D,
                        1.0D,
                        FurWatchShaderState::getBlurStrength,
                        value -> FurWatchShaderState.setBlurStrength((float) value),
                        "%.2f"));
            }
        }

        int bottomY = Math.min(this.height - 28, contentTop + rowHeight * 6);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .pos(left, bottomY).size(halfWidth, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("controls.reset"), button -> {
            FurWatchShaderState.restoreDefaults();
            FurWatchShaderState.persist();
            this.rebuildWidgets();
        }).pos(left + halfWidth + 8, bottomY).size(halfWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, ChatFormatting.AQUA + this.title.getString(), this.width / 2, 8, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, Component.translatable("screen.fursmp.shader_options.subtitle").getString(), this.width / 2, 18, 0x808080);
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