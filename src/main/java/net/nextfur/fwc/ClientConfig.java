package net.nextfur.fwc;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.nextfur.fwc.util.client.FurWatchShaderState;

@EventBusSubscriber(modid = FwMain.MODID)
public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue SHADERS_ENABLED = BUILDER
        .comment("Master switch to enable or disable FurWatch shaders.")
        .define("shaders.enabled", false);
    private static final ModConfigSpec.BooleanValue DISABLE_ON_IRIS = BUILDER
        .comment("Automatically disable FurWatch shaders when Iris/Oculus shaderpacks are active.")
        .define("compatibility.disableOnIris", true);

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

    private static final ModConfigSpec.BooleanValue STARS_ENABLED = BUILDER
        .comment("Enables custom star rendering at night.")
        .define("sky.stars.enabled", true);
    private static final ModConfigSpec.DoubleValue STAR_BRIGHTNESS = BUILDER
        .comment("Brightness multiplier for starry night sky.")
        .defineInRange("sky.stars.brightness", 1.0D, 0.0D, 3.0D);
    private static final ModConfigSpec.DoubleValue STAR_TWINKLE = BUILDER
        .comment("Twinkle effect strength for stars.")
        .defineInRange("sky.stars.twinkle", 0.5D, 0.0D, 1.0D);
    private static final ModConfigSpec.BooleanValue CELESTIAL_SPHERE = BUILDER
        .comment("Whether stars map to the rotating celestial sky dome instead of screen coordinates.")
        .define("sky.stars.celestialSphere", true);

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

    private static final ModConfigSpec.BooleanValue FILM_GRAIN = BUILDER
        .comment("Enables cinematic film grain effect.")
        .define("effects.filmGrain", true);
    private static final ModConfigSpec.BooleanValue VIGNETTE = BUILDER
        .comment("Enables screen edge vignette effect.")
        .define("effects.vignette", true);
    private static final ModConfigSpec.BooleanValue SCANLINES = BUILDER
        .comment("Enables retro scanlines effect.")
        .define("effects.scanlines", false);
    private static final ModConfigSpec.BooleanValue CHROMATIC_ABERRATION = BUILDER
        .comment("Enables chromatic aberration color fringe.")
        .define("effects.chromaticAberration", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean shadersEnabled;
    private static boolean disableOnIris;
    private static boolean lightingEnabled;
    private static double globalIntensity;
    private static double ambientIntensity;
    private static double directionalIntensity;
    private static double localLightRadius;
    private static double localLightBrightness;
    private static String lightingPreset = "balanced";
    private static boolean occlusionEnabled;
    private static boolean starsEnabled;
    private static double starBrightness;
    private static double starTwinkle;
    private static boolean celestialSphere;
    private static boolean postEffectsEnabled;
    private static double postEffectsStrength;
    private static double blurStrength;
    private static double reflectionStrength;
    private static double reflectionSoftness;
    private static boolean waterEffectsEnabled;
    private static double fogIntensity;
    private static double fogVariation;
    private static double lightVariation;
    private static boolean filmGrain;
    private static boolean vignette;
    private static boolean scanlines;
    private static boolean chromaticAberration;

    private ClientConfig() {
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() != ClientConfig.SPEC) {
            return;
        }

        shadersEnabled = SHADERS_ENABLED.get();
        disableOnIris = DISABLE_ON_IRIS.get();
        lightingEnabled = LIGHTING_ENABLED.get() || shadersEnabled;
        globalIntensity = GLOBAL_INTENSITY.get();
        ambientIntensity = AMBIENT_INTENSITY.get();
        directionalIntensity = DIRECTIONAL_INTENSITY.get();
        localLightRadius = LOCAL_LIGHT_RADIUS.get();
        localLightBrightness = LOCAL_LIGHT_BRIGHTNESS.get();
        lightingPreset = LIGHTING_PRESET.get();
        occlusionEnabled = OCCLUSION_ENABLED.get();
        starsEnabled = STARS_ENABLED.get();
        starBrightness = STAR_BRIGHTNESS.get();
        starTwinkle = STAR_TWINKLE.get();
        celestialSphere = CELESTIAL_SPHERE.get();
        postEffectsEnabled = POST_EFFECTS_ENABLED.get();
        postEffectsStrength = POST_EFFECTS_STRENGTH.get();
        blurStrength = BLUR_STRENGTH.get();
        reflectionStrength = REFLECTION_STRENGTH.get();
        reflectionSoftness = REFLECTION_SOFTNESS.get();
        waterEffectsEnabled = WATER_EFFECTS_ENABLED.get();
        fogIntensity = FOG_INTENSITY.get();
        fogVariation = FOG_VARIATION.get();
        lightVariation = LIGHT_VARIATION.get();
        filmGrain = FILM_GRAIN.get();
        vignette = VIGNETTE.get();
        scanlines = SCANLINES.get();
        chromaticAberration = CHROMATIC_ABERRATION.get();
        FurWatchShaderState.reloadFromConfig();
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        onLoad(new ModConfigEvent.Loading(event.getConfig()));
    }

    public static boolean isShadersEnabled() {
        return shadersEnabled;
    }

    public static boolean isDisableOnIris() {
        return disableOnIris;
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

    public static boolean isStarsEnabled() {
        return starsEnabled;
    }

    public static double getStarBrightness() {
        return starBrightness;
    }

    public static double getStarTwinkle() {
        return starTwinkle;
    }

    public static boolean isCelestialSphere() {
        return celestialSphere;
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

    public static boolean isFilmGrain() {
        return filmGrain;
    }

    public static boolean isVignette() {
        return vignette;
    }

    public static boolean isScanlines() {
        return scanlines;
    }

    public static boolean isChromaticAberration() {
        return chromaticAberration;
    }

    public static void setShadersEnabled(boolean enabled) {
        SHADERS_ENABLED.set(enabled);
        shadersEnabled = enabled;
        setLightingEnabled(enabled);
    }

    public static void setDisableOnIris(boolean disable) {
        DISABLE_ON_IRIS.set(disable);
        disableOnIris = disable;
    }

    public static void setLightingEnabled(boolean enabled) {
        LIGHTING_ENABLED.set(enabled);
        lightingEnabled = enabled;
        shadersEnabled = enabled;
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

    public static void setStarsEnabled(boolean enabled) {
        STARS_ENABLED.set(enabled);
        starsEnabled = enabled;
    }

    public static void setStarBrightness(double brightness) {
        STAR_BRIGHTNESS.set(brightness);
        starBrightness = brightness;
    }

    public static void setStarTwinkle(double twinkle) {
        STAR_TWINKLE.set(twinkle);
        starTwinkle = twinkle;
    }

    public static void setCelestialSphere(boolean enabled) {
        CELESTIAL_SPHERE.set(enabled);
        celestialSphere = enabled;
    }

    public static void setFilmGrain(boolean enabled) {
        FILM_GRAIN.set(enabled);
        filmGrain = enabled;
    }

    public static void setVignette(boolean enabled) {
        VIGNETTE.set(enabled);
        vignette = enabled;
    }

    public static void setScanlines(boolean enabled) {
        SCANLINES.set(enabled);
        scanlines = enabled;
    }

    public static void setChromaticAberration(boolean enabled) {
        CHROMATIC_ABERRATION.set(enabled);
        chromaticAberration = enabled;
    }

    public static void save() {
        SPEC.save();
    }
}