package net.nextfur.fwc.render;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.api.client.render.shader.ShaderManager;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.util.client.FurWatchShaderState;

public final class FurWatchShaderRender {
    private static final ResourceLocation PIPELINE_ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "furwatch");
    private static final ResourceLocation SHADER_ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "furwatch/global");

    private FurWatchShaderRender() {
    }

    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            deactivatePipeline();
            return;
        }
        if (!FurWatchShaderState.isEnabled()) {
            deactivatePipeline();
            return;
        }
        if (VeilRenderSystem.renderer() == null) {
            return;
        }

        PostProcessingManager postProcessingManager = VeilRenderSystem.renderer().getPostProcessingManager();
        PostPipeline pipeline = postProcessingManager.getPipeline(PIPELINE_ID);
        if (pipeline == null) {
            postProcessingManager.remove(PIPELINE_ID);
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

        shader.getUniformSafe("Intensity").setFloat(FurWatchShaderState.getIntensity());
        shader.getUniformSafe("GameTime").setFloat(time);
        shader.getUniformSafe("PresetIndex").setInt(FurWatchShaderState.getPresetIndex());
        shader.getUniformSafe("FilmGrainEnabled").setInt(FurWatchShaderState.isFilmGrainEnabled() ? 1 : 0);
        shader.getUniformSafe("VignetteEnabled").setInt(FurWatchShaderState.isVignetteEnabled() ? 1 : 0);
        shader.getUniformSafe("ScanlinesEnabled").setInt(FurWatchShaderState.isScanlinesEnabled() ? 1 : 0);
        shader.getUniformSafe("ChromaticAberrationEnabled").setInt(FurWatchShaderState.isChromaticAberrationEnabled() ? 1 : 0);
    }

    private static void deactivatePipeline() {
        if (VeilRenderSystem.renderer() == null) {
            return;
        }

        PostProcessingManager postProcessingManager = VeilRenderSystem.renderer().getPostProcessingManager();
        if (postProcessingManager.isActive(PIPELINE_ID)) {
            postProcessingManager.remove(PIPELINE_ID);
        }
    }
}