package net.nextfur.fwc;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.nextfur.fwc.util.client.FurWatchShaderState;

@EventBusSubscriber(modid = FwMain.MODID)
public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue LIGHTING_ENABLED = BUILDER
        .comment("Enables the FurWatch raster lighting pipeline.")
        .define("lighting.enabled", false);
    private static final ModConfigSpec.DoubleValue GLOBAL_INTENSITY = BUILDER
        .comment("Master intensity multiplier for FurWatch lighting.")
        .defineInRange("lighting.globalIntensity", 1.0D, 0.0D, 2.0D);
    private static final ModConfigSpec.DoubleValue AMBIENT_INTENSITY = BUILDER
        .comment("Ambient fill light strength.")
        .defineInRange("lighting.ambientIntensity", 0.35D, 0.0D, 1.5D);
    private static final ModConfigSpec.DoubleValue DIRECTIONAL_INTENSITY = BUILDER
        .comment("Sun or moon directional lighting strength.")
        .defineInRange("lighting.directionalIntensity", 0.75D, 0.0D, 2.0D);
    private static final ModConfigSpec.DoubleValue LOCAL_LIGHT_RADIUS = BUILDER
        .comment("Radius multiplier for local emissive lights.")
        .defineInRange("lighting.localLightRadius", 24.0D, 8.0D, 96.0D);
    private static final ModConfigSpec.DoubleValue LOCAL_LIGHT_BRIGHTNESS = BUILDER
        .comment("Brightness multiplier for local emissive lights.")
        .defineInRange("lighting.localLightBrightness", 1.0D, 0.1D, 4.0D);
    private static final ModConfigSpec.ConfigValue<String> LIGHTING_PRESET = BUILDER
        .comment("Selected FurWatch lighting preset.")
        .define("lighting.preset", "balanced");
    private static final ModConfigSpec.BooleanValue OCCLUSION_ENABLED = BUILDER
        .comment("Enables occlusion for FurWatch scene lights.")
        .define("lighting.occlusion", false);
    private static final ModConfigSpec.BooleanValue POST_EFFECTS_ENABLED = BUILDER
        .comment("Enables FurWatch post effects after lighting has been composited.")
        .define("effects.enabled", false);
    private static final ModConfigSpec.DoubleValue POST_EFFECTS_STRENGTH = BUILDER
        .comment("Strength multiplier for FurWatch legacy post effects.")
        .defineInRange("effects.strength", 0.65D, 0.0D, 1.5D);
    private static final ModConfigSpec.DoubleValue BLUR_STRENGTH = BUILDER
        .comment("Blend amount for FurWatch visual blur.")
        .defineInRange("effects.blur", 0.15D, 0.0D, 1.0D);
    private static final ModConfigSpec.DoubleValue REFLECTION_STRENGTH = BUILDER
        .comment("Screen-space reflection and highlight strength.")
        .defineInRange("effects.reflectionStrength", 0.45D, 0.0D, 1.5D);
    private static final ModConfigSpec.DoubleValue REFLECTION_SOFTNESS = BUILDER
        .comment("Softness of FurWatch reflective highlights.")
        .defineInRange("effects.reflectionSoftness", 0.35D, 0.0D, 1.0D);
    private static final ModConfigSpec.BooleanValue WATER_EFFECTS_ENABLED = BUILDER
        .comment("Enables FurWatch water reflections and parallax distortion.")
        .define("effects.water.enabled", true);
    private static final ModConfigSpec.DoubleValue FOG_INTENSITY = BUILDER
        .comment("Intensity of FurWatch atmospheric fog.")
        .defineInRange("effects.fogIntensity", 0.65D, 0.0D, 1.5D);
    private static final ModConfigSpec.DoubleValue FOG_VARIATION = BUILDER
        .comment("Noise variation amount for FurWatch atmospheric fog.")
        .defineInRange("effects.fogVariation", 0.3D, 0.0D, 1.0D);
    private static final ModConfigSpec.DoubleValue LIGHT_VARIATION = BUILDER
        .comment("Variation amount applied to FurWatch local light sources and composite lighting.")
        .defineInRange("effects.lightVariation", 0.35D, 0.0D, 1.0D);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean lightingEnabled;
    private static double globalIntensity;
    private static double ambientIntensity;
    private static double directionalIntensity;
    private static double localLightRadius;
    private static double localLightBrightness;
    private static String lightingPreset = "balanced";
    private static boolean occlusionEnabled;
    private static boolean postEffectsEnabled;
    private static double postEffectsStrength;
    private static double blurStrength;
    private static double reflectionStrength;
    private static double reflectionSoftness;
    private static boolean waterEffectsEnabled;
    private static double fogIntensity;
    private static double fogVariation;
    private static double lightVariation;

    private ClientConfig() {
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() != ClientConfig.SPEC) {
            return;
        }

        lightingEnabled = LIGHTING_ENABLED.get();
        globalIntensity = GLOBAL_INTENSITY.get();
        ambientIntensity = AMBIENT_INTENSITY.get();
        directionalIntensity = DIRECTIONAL_INTENSITY.get();
        localLightRadius = LOCAL_LIGHT_RADIUS.get();
        localLightBrightness = LOCAL_LIGHT_BRIGHTNESS.get();
        lightingPreset = LIGHTING_PRESET.get();
        occlusionEnabled = OCCLUSION_ENABLED.get();
        postEffectsEnabled = POST_EFFECTS_ENABLED.get();
        postEffectsStrength = POST_EFFECTS_STRENGTH.get();
        blurStrength = BLUR_STRENGTH.get();
        reflectionStrength = REFLECTION_STRENGTH.get();
        reflectionSoftness = REFLECTION_SOFTNESS.get();
        waterEffectsEnabled = WATER_EFFECTS_ENABLED.get();
        fogIntensity = FOG_INTENSITY.get();
        fogVariation = FOG_VARIATION.get();
        lightVariation = LIGHT_VARIATION.get();
        FurWatchShaderState.reloadFromConfig();
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        onLoad(new ModConfigEvent.Loading(event.getConfig()));
    }

    public static boolean isLightingEnabled() {
        return lightingEnabled;
    }

    public static double getGlobalIntensity() {
        return globalIntensity;
    }

    public static double getAmbientIntensity() {
        return ambientIntensity;
    }

    public static double getDirectionalIntensity() {
        return directionalIntensity;
    }

    public static double getLocalLightRadius() {
        return localLightRadius;
    }

    public static double getLocalLightBrightness() {
        return localLightBrightness;
    }

    public static String getLightingPreset() {
        return lightingPreset;
    }

    public static boolean isOcclusionEnabled() {
        return occlusionEnabled;
    }

    public static boolean isPostEffectsEnabled() {
        return postEffectsEnabled;
    }

    public static double getPostEffectsStrength() {
        return postEffectsStrength;
    }

    public static double getBlurStrength() {
        return blurStrength;
    }

    public static double getReflectionStrength() {
        return reflectionStrength;
    }

    public static double getReflectionSoftness() {
        return reflectionSoftness;
    }

    public static boolean isWaterEffectsEnabled() {
        return waterEffectsEnabled;
    }

    public static double getFogIntensity() {
        return fogIntensity;
    }

    public static double getFogVariation() {
        return fogVariation;
    }

    public static double getLightVariation() {
        return lightVariation;
    }

    public static void setLightingEnabled(boolean enabled) {
        LIGHTING_ENABLED.set(enabled);
        lightingEnabled = enabled;
    }

    public static void setGlobalIntensity(double intensity) {
        GLOBAL_INTENSITY.set(intensity);
        globalIntensity = intensity;
    }

    public static void setAmbientIntensity(double intensity) {
        AMBIENT_INTENSITY.set(intensity);
        ambientIntensity = intensity;
    }

    public static void setDirectionalIntensity(double intensity) {
        DIRECTIONAL_INTENSITY.set(intensity);
        directionalIntensity = intensity;
    }

    public static void setLocalLightRadius(double radius) {
        LOCAL_LIGHT_RADIUS.set(radius);
        localLightRadius = radius;
    }

    public static void setLocalLightBrightness(double brightness) {
        LOCAL_LIGHT_BRIGHTNESS.set(brightness);
        localLightBrightness = brightness;
    }

    public static void setLightingPreset(String preset) {
        LIGHTING_PRESET.set(preset);
        lightingPreset = preset;
    }

    public static void setOcclusionEnabled(boolean enabled) {
        OCCLUSION_ENABLED.set(enabled);
        occlusionEnabled = enabled;
    }

    public static void setPostEffectsEnabled(boolean enabled) {
        POST_EFFECTS_ENABLED.set(enabled);
        postEffectsEnabled = enabled;
    }

    public static void setPostEffectsStrength(double strength) {
        POST_EFFECTS_STRENGTH.set(strength);
        postEffectsStrength = strength;
    }

    public static void setBlurStrength(double strength) {
        BLUR_STRENGTH.set(strength);
        blurStrength = strength;
    }

    public static void setReflectionStrength(double strength) {
        REFLECTION_STRENGTH.set(strength);
        reflectionStrength = strength;
    }

    public static void setReflectionSoftness(double softness) {
        REFLECTION_SOFTNESS.set(softness);
        reflectionSoftness = softness;
    }

    public static void setWaterEffectsEnabled(boolean enabled) {
        WATER_EFFECTS_ENABLED.set(enabled);
        waterEffectsEnabled = enabled;
    }

    public static void setFogIntensity(double intensity) {
        FOG_INTENSITY.set(intensity);
        fogIntensity = intensity;
    }

    public static void setFogVariation(double variation) {
        FOG_VARIATION.set(variation);
        fogVariation = variation;
    }

    public static void setLightVariation(double variation) {
        LIGHT_VARIATION.set(variation);
        lightVariation = variation;
    }

    public static void save() {
        SPEC.save();
    }
}