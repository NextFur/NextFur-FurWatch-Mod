package net.nextfur.fwc;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.nextfur.fwc.util.client.FurWatchShaderState;

@EventBusSubscriber(modid = FwMain.MODID)
public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue SHADER_ENABLED = BUILDER
            .comment("Enables the FurWatch post-processing shader pipeline.")
            .define("shader.enabled", false);
    private static final ModConfigSpec.DoubleValue SHADER_INTENSITY = BUILDER
            .comment("Global intensity multiplier for the FurWatch post-processing shader.")
            .defineInRange("shader.intensity", 1.0D, 0.0D, 2.0D);
    private static final ModConfigSpec.ConfigValue<String> SHADER_PRESET = BUILDER
            .comment("Selected FurWatch shader preset.")
            .define("shader.preset", "default");
    private static final ModConfigSpec.BooleanValue FILM_GRAIN_ENABLED = BUILDER
            .comment("Enables film grain in the FurWatch shader pipeline.")
            .define("shader.effects.filmGrain", true);
    private static final ModConfigSpec.BooleanValue VIGNETTE_ENABLED = BUILDER
            .comment("Enables vignette in the FurWatch shader pipeline.")
            .define("shader.effects.vignette", true);
    private static final ModConfigSpec.BooleanValue SCANLINES_ENABLED = BUILDER
            .comment("Enables scanlines in the FurWatch shader pipeline.")
            .define("shader.effects.scanlines", true);
    private static final ModConfigSpec.BooleanValue CHROMATIC_ABERRATION_ENABLED = BUILDER
            .comment("Enables chromatic aberration in the FurWatch shader pipeline.")
            .define("shader.effects.chromaticAberration", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean shaderEnabled;
    private static double shaderIntensity;
    private static String shaderPreset = "default";
    private static boolean filmGrainEnabled;
    private static boolean vignetteEnabled;
    private static boolean scanlinesEnabled;
    private static boolean chromaticAberrationEnabled;

    private ClientConfig() {
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() != ClientConfig.SPEC) {
            return;
        }

        shaderEnabled = SHADER_ENABLED.get();
        shaderIntensity = SHADER_INTENSITY.get();
        shaderPreset = SHADER_PRESET.get();
        filmGrainEnabled = FILM_GRAIN_ENABLED.get();
        vignetteEnabled = VIGNETTE_ENABLED.get();
        scanlinesEnabled = SCANLINES_ENABLED.get();
        chromaticAberrationEnabled = CHROMATIC_ABERRATION_ENABLED.get();
        FurWatchShaderState.reloadFromConfig();
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        onLoad(new ModConfigEvent.Loading(event.getConfig()));
    }

    public static boolean isShaderEnabled() {
        return shaderEnabled;
    }

    public static double getShaderIntensity() {
        return shaderIntensity;
    }

    public static String getShaderPreset() {
        return shaderPreset;
    }

    public static boolean isFilmGrainEnabled() {
        return filmGrainEnabled;
    }

    public static boolean isVignetteEnabled() {
        return vignetteEnabled;
    }

    public static boolean isScanlinesEnabled() {
        return scanlinesEnabled;
    }

    public static boolean isChromaticAberrationEnabled() {
        return chromaticAberrationEnabled;
    }

    public static void setShaderEnabled(boolean enabled) {
        SHADER_ENABLED.set(enabled);
        shaderEnabled = enabled;
    }

    public static void setShaderIntensity(double intensity) {
        SHADER_INTENSITY.set(intensity);
        shaderIntensity = intensity;
    }

    public static void setShaderPreset(String preset) {
        SHADER_PRESET.set(preset);
        shaderPreset = preset;
    }

    public static void setFilmGrainEnabled(boolean enabled) {
        FILM_GRAIN_ENABLED.set(enabled);
        filmGrainEnabled = enabled;
    }

    public static void setVignetteEnabled(boolean enabled) {
        VIGNETTE_ENABLED.set(enabled);
        vignetteEnabled = enabled;
    }

    public static void setScanlinesEnabled(boolean enabled) {
        SCANLINES_ENABLED.set(enabled);
        scanlinesEnabled = enabled;
    }

    public static void setChromaticAberrationEnabled(boolean enabled) {
        CHROMATIC_ABERRATION_ENABLED.set(enabled);
        chromaticAberrationEnabled = enabled;
    }

    public static void save() {
        SPEC.save();
    }
}