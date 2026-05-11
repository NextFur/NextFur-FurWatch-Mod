package net.nextfur.fwc.util.client;

import net.nextfur.fwc.ClientConfig;
import net.minecraft.util.Mth;

public final class FurWatchShaderState {
    private static boolean enabled;
    private static float globalIntensity;
    private static float ambientIntensity;
    private static float directionalIntensity;
    private static float localLightRadius;
    private static float localLightBrightness;
    private static String preset = "balanced";
    private static boolean occlusionEnabled;

    private FurWatchShaderState() {
    }

    public static void reloadFromConfig() {
        enabled = ClientConfig.isLightingEnabled();
        globalIntensity = (float) ClientConfig.getGlobalIntensity();
        ambientIntensity = (float) ClientConfig.getAmbientIntensity();
        directionalIntensity = (float) ClientConfig.getDirectionalIntensity();
        localLightRadius = (float) ClientConfig.getLocalLightRadius();
        localLightBrightness = (float) ClientConfig.getLocalLightBrightness();
        preset = ClientConfig.getLightingPreset();
        occlusionEnabled = ClientConfig.isOcclusionEnabled();
    }

    public static void reset() {
        enabled = false;
        globalIntensity = 1.0F;
        ambientIntensity = 0.35F;
        directionalIntensity = 0.75F;
        localLightRadius = 24.0F;
        localLightBrightness = 1.0F;
        preset = "balanced";
        occlusionEnabled = false;
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

    public static float getGlobalIntensity() {
        return globalIntensity;
    }

    public static void setGlobalIntensity(float intensity) {
        FurWatchShaderState.globalIntensity = Mth.clamp(intensity, 0.0F, 2.0F);
    }

    public static float getAmbientIntensity() {
        return ambientIntensity;
    }

    public static void setAmbientIntensity(float intensity) {
        FurWatchShaderState.ambientIntensity = Mth.clamp(intensity, 0.0F, 1.5F);
    }

    public static float getDirectionalIntensity() {
        return directionalIntensity;
    }

    public static void setDirectionalIntensity(float intensity) {
        FurWatchShaderState.directionalIntensity = Mth.clamp(intensity, 0.0F, 2.0F);
    }

    public static float getLocalLightRadius() {
        return localLightRadius;
    }

    public static void setLocalLightRadius(float radius) {
        FurWatchShaderState.localLightRadius = Mth.clamp(radius, 8.0F, 96.0F);
    }

    public static float getLocalLightBrightness() {
        return localLightBrightness;
    }

    public static void setLocalLightBrightness(float brightness) {
        FurWatchShaderState.localLightBrightness = Mth.clamp(brightness, 0.1F, 4.0F);
    }

    public static String getPreset() {
        return preset;
    }

    public static void setPreset(String preset) {
        FurWatchShaderState.preset = preset;
    }

    public static boolean isOcclusionEnabled() {
        return occlusionEnabled;
    }

    public static void setOcclusionEnabled(boolean occlusionEnabled) {
        FurWatchShaderState.occlusionEnabled = occlusionEnabled;
    }

    public static void persist() {
        ClientConfig.setLightingEnabled(enabled);
        ClientConfig.setGlobalIntensity(globalIntensity);
        ClientConfig.setAmbientIntensity(ambientIntensity);
        ClientConfig.setDirectionalIntensity(directionalIntensity);
        ClientConfig.setLocalLightRadius(localLightRadius);
        ClientConfig.setLocalLightBrightness(localLightBrightness);
        ClientConfig.setLightingPreset(preset);
        ClientConfig.setOcclusionEnabled(occlusionEnabled);
        ClientConfig.save();
    }

    public static void restoreDefaults() {
        enabled = false;
        globalIntensity = 1.0F;
        ambientIntensity = 0.35F;
        directionalIntensity = 0.75F;
        localLightRadius = 24.0F;
        localLightBrightness = 1.0F;
        preset = "balanced";
        occlusionEnabled = false;
    }

    public static int getPresetIndex() {
        return switch (preset) {
            case "warm" -> 1;
            case "moonlit" -> 2;
            default -> 0;
        };
    }
}