package net.nextfur.fwc.render;

import foundry.veil.api.client.render.dynamicbuffer.DynamicBufferType;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.util.client.FurWatchShaderState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class FurWatchShaderRender {
    private static final ResourceLocation BUFFER_ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "furwatch_lighting");
    private static LightRenderHandle<AreaLightData> ambientHandle;
    private static LightRenderHandle<AreaLightData> directionalHandle;

    private FurWatchShaderRender() {
    }

    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            clearLighting();
            return;
        }
        if (!FurWatchShaderState.isEnabled()) {
            clearLighting();
            return;
        }
        if (VeilRenderSystem.renderer() == null || VeilRenderSystem.renderer().getLightRenderer() == null) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        VeilRenderSystem.renderer().enableBuffers(BUFFER_ID, DynamicBufferType.ALBEDO, DynamicBufferType.NORMAL);

        Vec3 cameraPos = client.gameRenderer.getMainCamera().getPosition();
        float localRadius = FurWatchShaderState.getLocalLightRadius();
        float globalIntensity = FurWatchShaderState.getGlobalIntensity();
        CelestialLighting celestialLighting = sampleCelestialLighting(client, partialTick, FurWatchShaderState.getPreset());
        Vector3f lightDirection = celestialLighting.direction();

        VeilRenderSystem.setShaderLights(lightDirection, new Vector3f(lightDirection).negate());

        Quaternionf ambientRotation = rotationForDirection(new Vector3f(0.0F, -1.0F, 0.0F));
        Vec3 ambientPos = cameraPos.add(0.0D, localRadius * 0.25D, 0.0D);
        float ambientBrightness = globalIntensity * FurWatchShaderState.getAmbientIntensity();
        ambientHandle = ensureLight(
                ambientHandle,
                ambientPos,
                ambientRotation,
            celestialLighting.ambientColor(),
                ambientBrightness,
                localRadius,
                (float) Math.toRadians(85.0D),
                localRadius * 1.5F,
                FurWatchShaderState.isOcclusionEnabled()
        );

        Quaternionf sunRotation = rotationForDirection(new Vector3f(lightDirection));
        Vec3 directionalPos = cameraPos.subtract(lightDirection.x * localRadius * 0.75F, lightDirection.y * localRadius * 0.75F, lightDirection.z * localRadius * 0.75F);
        float directionalBrightness = globalIntensity * FurWatchShaderState.getDirectionalIntensity() * celestialLighting.directionalStrength();
        directionalHandle = ensureLight(
                directionalHandle,
                directionalPos,
                sunRotation,
            celestialLighting.directionalColor(),
                directionalBrightness,
                localRadius * 1.2F,
                (float) Math.toRadians(55.0D),
                localRadius * 2.0F,
                FurWatchShaderState.isOcclusionEnabled()
        );
    }

    public static void clearLighting() {
        if (VeilRenderSystem.renderer() == null) {
            return;
        }

        VeilRenderSystem.renderer().disableBuffers(BUFFER_ID, DynamicBufferType.ALBEDO, DynamicBufferType.NORMAL);
        if (ambientHandle != null) {
            ambientHandle.free();
            ambientHandle = null;
        }
        if (directionalHandle != null) {
            directionalHandle.free();
            directionalHandle = null;
        }
    }

    private static LightRenderHandle<AreaLightData> ensureLight(
            LightRenderHandle<AreaLightData> handle,
            Vec3 position,
            Quaternionf rotation,
            Vector3f color,
            float brightness,
            float size,
            float angle,
            float distance,
            boolean occlusionEnabled
    ) {
        if (handle == null || !handle.isValid()) {
            AreaLightData light = new AreaLightData()
                    .setColor(color.x, color.y, color.z)
                    .setBrightness(brightness)
                    .setDistance(distance)
                    .setAngle(angle)
                    .setSize(size, size)
                    .setOcclusionEnabled(occlusionEnabled);
            light.getPosition().set(position.x, position.y, position.z);
            light.getOrientation().set(rotation);
            return VeilRenderSystem.renderer().getLightRenderer().addLight(light);
        }

        AreaLightData light = handle.getLightData();
        light.setColor(color.x, color.y, color.z);
        light.setBrightness(brightness);
        light.setDistance(distance);
        light.setAngle(angle);
        light.setSize(size, size);
        light.setOcclusionEnabled(occlusionEnabled);
        light.getPosition().set(position.x, position.y, position.z);
        light.getOrientation().set(rotation);
        handle.markDirty();
        return handle;
    }

    private static CelestialLighting sampleCelestialLighting(Minecraft client, float partialTick, String preset) {
        LightingColors presetColors = LightingColors.forPreset(preset);
        float skyAngle = client.level.getTimeOfDay(partialTick) * ((float) Math.PI * 2.0F);
        Vector3f sunDirection = new Vector3f(
                Mth.sin(skyAngle),
                Mth.cos(skyAngle),
                Mth.sin(skyAngle * 0.5F) * 0.45F
        ).normalize();
        Vector3f moonDirection = new Vector3f(sunDirection).negate();

        float daylight = smoothStep(-0.14F, 0.1F, sunDirection.y);
        Vector3f direction = new Vector3f(moonDirection).lerp(sunDirection, daylight).normalize();
        Vector3f ambientColor = new Vector3f(presetColors.nightAmbientColor()).lerp(presetColors.dayAmbientColor(), daylight);
        Vector3f directionalColor = new Vector3f(presetColors.nightDirectionalColor()).lerp(presetColors.dayDirectionalColor(), daylight);
        float directionalStrength = Mth.lerp(daylight, 0.42F, 1.0F);
        return new CelestialLighting(direction, ambientColor, directionalColor, directionalStrength);
    }

    private static float smoothStep(float edge0, float edge1, float value) {
        float scaled = Mth.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return scaled * scaled * (3.0F - (2.0F * scaled));
    }

    private static Quaternionf rotationForDirection(Vector3f direction) {
        Vector3f normalized = new Vector3f(direction).normalize();
        Vector3f up = Math.abs(normalized.y) > 0.95F ? new Vector3f(0.0F, 0.0F, 1.0F) : new Vector3f(0.0F, 1.0F, 0.0F);
        return new Quaternionf().lookAlong(normalized.negate(), up);
    }

    private record CelestialLighting(Vector3f direction, Vector3f ambientColor, Vector3f directionalColor, float directionalStrength) {
    }

    private record LightingColors(Vector3f dayAmbientColor, Vector3f dayDirectionalColor, Vector3f nightAmbientColor, Vector3f nightDirectionalColor) {
        private static LightingColors forPreset(String preset) {
            return switch (preset) {
                case "warm" -> new LightingColors(
                        new Vector3f(0.95F, 0.82F, 0.72F),
                        new Vector3f(1.0F, 0.9F, 0.72F),
                        new Vector3f(0.38F, 0.42F, 0.58F),
                        new Vector3f(0.58F, 0.7F, 0.88F)
                );
                case "moonlit" -> new LightingColors(
                        new Vector3f(0.7F, 0.74F, 0.9F),
                        new Vector3f(0.82F, 0.9F, 1.0F),
                        new Vector3f(0.42F, 0.5F, 0.74F),
                        new Vector3f(0.68F, 0.8F, 1.0F)
                );
                default -> new LightingColors(
                        new Vector3f(0.82F, 0.84F, 0.88F),
                        new Vector3f(1.0F, 0.98F, 0.92F),
                        new Vector3f(0.4F, 0.46F, 0.62F),
                        new Vector3f(0.62F, 0.74F, 0.98F)
                );
            };
        }
    }
}