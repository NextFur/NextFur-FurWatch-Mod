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
        if (!FurWatchShaderState.isEffectiveShaderEnabled()) {
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

        float yaw = client.gameRenderer.getMainCamera().getYRot();
        float pitch = client.gameRenderer.getMainCamera().getXRot();
        float fov = (float) client.options.fov().get().intValue();
        float aspect = (float) Math.max(1, client.getWindow().getWidth()) / (float) Math.max(1, client.getWindow().getHeight());
        float skyAngle = client.level.getTimeOfDay(partialTick);

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
        shader.getUniformSafe("FilmGrainEnabled").setInt((effectEnabled && FurWatchShaderState.isFilmGrainEnabled()) ? 1 : 0);
        shader.getUniformSafe("VignetteEnabled").setInt((effectEnabled && FurWatchShaderState.isVignetteEnabled()) ? 1 : 0);
        shader.getUniformSafe("ScanlinesEnabled").setInt((effectEnabled && FurWatchShaderState.isScanlinesEnabled()) ? 1 : 0);
        shader.getUniformSafe("ChromaticAberrationEnabled").setInt((effectEnabled && FurWatchShaderState.isChromaticAberrationEnabled()) ? 1 : 0);

        shader.getUniformSafe("StarsEnabled").setInt(FurWatchShaderState.isStarsEnabled() ? 1 : 0);
        shader.getUniformSafe("StarBrightness").setFloat(FurWatchShaderState.getStarBrightness());
        shader.getUniformSafe("StarTwinkle").setFloat(FurWatchShaderState.getStarTwinkle());
        shader.getUniformSafe("CelestialSphere").setInt(FurWatchShaderState.isCelestialSphere() ? 1 : 0);
        shader.getUniformSafe("CameraYaw").setFloat(yaw);
        shader.getUniformSafe("CameraPitch").setFloat(pitch);
        shader.getUniformSafe("CameraFov").setFloat(fov);
        shader.getUniformSafe("AspectRatio").setFloat(aspect);
        shader.getUniformSafe("SkyAngle").setFloat(skyAngle);
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
        if (!FurWatchShaderState.isStarsEnabled() || client.level == null || client.level.dimension() != Level.OVERWORLD) {
            return 0.0F;
        }

        float starBrightness = client.level.getStarBrightness(partialTick) * 2.0F;
        return Mth.clamp(starBrightness, 0.0F, 1.0F);
    }
}