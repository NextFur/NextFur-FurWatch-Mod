package net.nextfur.fwc.util.client;

import net.nextfur.fwc.ClientConfig;
import net.minecraft.util.Mth;

public final class FurWatchShaderState {
    private static boolean enabled;
    private static float intensity;
    private static String preset = "default";
    private static boolean filmGrainEnabled;
    private static boolean vignetteEnabled;
    private static boolean scanlinesEnabled;
    private static boolean chromaticAberrationEnabled;

    private FurWatchShaderState() {
    }

    public static void reloadFromConfig() {
        enabled = ClientConfig.isShaderEnabled();
        intensity = (float) ClientConfig.getShaderIntensity();
        preset = ClientConfig.getShaderPreset();
        filmGrainEnabled = ClientConfig.isFilmGrainEnabled();
        vignetteEnabled = ClientConfig.isVignetteEnabled();
        scanlinesEnabled = ClientConfig.isScanlinesEnabled();
        chromaticAberrationEnabled = ClientConfig.isChromaticAberrationEnabled();
    }

    public static void reset() {
        enabled = false;
        intensity = 1.0F;
        preset = "default";
        filmGrainEnabled = true;
        vignetteEnabled = true;
        scanlinesEnabled = true;
        chromaticAberrationEnabled = true;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        FurWatchShaderState.enabled = enabled;
    }

    public static void toggleEnabled() {
        enabled = !enabled;
    }

    public static float getIntensity() {
        return intensity;
    }

    public static void setIntensity(float intensity) {
        FurWatchShaderState.intensity = Mth.clamp(intensity, 0.0F, 2.0F);
    }

    public static String getPreset() {
        return preset;
    }

    public static void setPreset(String preset) {
        FurWatchShaderState.preset = preset;
    }

    public static boolean isFilmGrainEnabled() {
        return filmGrainEnabled;
    }

    public static void setFilmGrainEnabled(boolean filmGrainEnabled) {
        FurWatchShaderState.filmGrainEnabled = filmGrainEnabled;
    }

    public static boolean isVignetteEnabled() {
        return vignetteEnabled;
    }

    public static void setVignetteEnabled(boolean vignetteEnabled) {
        FurWatchShaderState.vignetteEnabled = vignetteEnabled;
    }

    public static boolean isScanlinesEnabled() {
        return scanlinesEnabled;
    }

    public static void setScanlinesEnabled(boolean scanlinesEnabled) {
        FurWatchShaderState.scanlinesEnabled = scanlinesEnabled;
    }

    public static boolean isChromaticAberrationEnabled() {
        return chromaticAberrationEnabled;
    }

    public static void setChromaticAberrationEnabled(boolean chromaticAberrationEnabled) {
        FurWatchShaderState.chromaticAberrationEnabled = chromaticAberrationEnabled;
    }

    public static void persist() {
        ClientConfig.setShaderEnabled(enabled);
        ClientConfig.setShaderIntensity(intensity);
        ClientConfig.setShaderPreset(preset);
        ClientConfig.setFilmGrainEnabled(filmGrainEnabled);
        ClientConfig.setVignetteEnabled(vignetteEnabled);
        ClientConfig.setScanlinesEnabled(scanlinesEnabled);
        ClientConfig.setChromaticAberrationEnabled(chromaticAberrationEnabled);
        ClientConfig.save();
    }

    public static void restoreDefaults() {
        enabled = false;
        intensity = 1.0F;
        preset = "default";
        filmGrainEnabled = true;
        vignetteEnabled = true;
        scanlinesEnabled = true;
        chromaticAberrationEnabled = true;
    }

    public static int getPresetIndex() {
        return switch (preset) {
            case "cinematic" -> 1;
            case "surveillance" -> 2;
            default -> 0;
        };
    }
}