package net.nextfur.fwc.render;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.util.client.FurWatchShaderState;
import net.nextfur.fwc.util.client.SkyColorState;

public final class FurWatchPostEffectRender {
    private static final ResourceLocation PIPELINE_ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "furwatch");
    private static final ResourceLocation SHADER_ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "furwatch/global");

    private FurWatchPostEffectRender() {
    }

    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            clearEffects();
            return;
        }
        if (!FurWatchShaderState.isEnabled()) {
            clearEffects();
            return;
        }
        if (VeilRenderSystem.renderer() == null) {
            return;
        }

        PostProcessingManager postProcessingManager = VeilRenderSystem.renderer().getPostProcessingManager();
        PostPipeline pipeline = postProcessingManager.getPipeline(PIPELINE_ID);
        if (pipeline == null) {
            clearEffects();
            return;
        }
        if (!postProcessingManager.isActive(PIPELINE_ID)) {
            postProcessingManager.add(PIPELINE_ID);
        }

        ShaderManager shaderManager = VeilRenderSystem.renderer().getShaderManager();
        ShaderProgram shader = shaderManager.getShader(SHADER_ID);
        if (shader == null || !shader.isValid()) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float time = (client.level.getGameTime() + partialTick) / 20.0F;
        boolean effectEnabled = FurWatchShaderState.isPostEffectsEnabled();
        float legacyStrength = effectEnabled ? FurWatchShaderState.getPostEffectsStrength() : 0.0F;

        shader.getUniformSafe("Intensity").setFloat(legacyStrength);
        shader.getUniformSafe("BlurAmount").setFloat(effectEnabled ? FurWatchShaderState.getBlurStrength() : 0.0F);
        shader.getUniformSafe("ReflectionStrength").setFloat(FurWatchShaderState.getReflectionStrength());
        shader.getUniformSafe("ReflectionSoftness").setFloat(FurWatchShaderState.getReflectionSoftness());
        shader.getUniformSafe("WaterEffectsEnabled").setInt(FurWatchShaderState.isWaterEffectsEnabled() ? 1 : 0);
        shader.getUniformSafe("NightSkyStrength").setFloat(resolveNightSkyStrength(client, partialTick));
        shader.getUniformSafe("FogIntensity").setFloat(FurWatchShaderState.getFogIntensity());
        shader.getUniformSafe("FogVariation").setFloat(FurWatchShaderState.getFogVariation());
        shader.getUniformSafe("LightVariation").setFloat(FurWatchShaderState.getLightVariation());
        shader.getUniformSafe("GameTime").setFloat(time);
        shader.getUniformSafe("PresetIndex").setInt(FurWatchShaderState.getPresetIndex());
        shader.getUniformSafe("FilmGrainEnabled").setInt(effectEnabled ? 1 : 0);
        shader.getUniformSafe("VignetteEnabled").setInt(effectEnabled ? 1 : 0);
        shader.getUniformSafe("ScanlinesEnabled").setInt(effectEnabled ? 1 : 0);
        shader.getUniformSafe("ChromaticAberrationEnabled").setInt(effectEnabled ? 1 : 0);
    }

    public static void clearEffects() {
        if (VeilRenderSystem.renderer() == null) {
            return;
        }

        PostProcessingManager postProcessingManager = VeilRenderSystem.renderer().getPostProcessingManager();
        if (postProcessingManager.isActive(PIPELINE_ID)) {
            postProcessingManager.remove(PIPELINE_ID);
        }
    }

    private static float resolveNightSkyStrength(Minecraft client, float partialTick) {
        if (client.level == null || client.level.dimension() != Level.OVERWORLD || SkyColorState.getBoxColor() != -1) {
            return 0.0F;
        }

        float skyAngle = client.level.getTimeOfDay(partialTick) * ((float) Math.PI * 2.0F);
        float sunHeight = Mth.cos(skyAngle);
        float nightBlend = 1.0F - smoothStep(-0.18F, 0.08F, sunHeight);
        float weatherDimming = 1.0F - Mth.clamp((client.level.getRainLevel(partialTick) * 0.45F) + (client.level.getThunderLevel(partialTick) * 0.55F), 0.0F, 0.75F);
        return Mth.clamp(nightBlend * weatherDimming, 0.0F, 1.0F);
    }

    private static float smoothStep(float edge0, float edge1, float value) {
        float scaled = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return scaled * scaled * (3.0F - (2.0F * scaled));
    }
}