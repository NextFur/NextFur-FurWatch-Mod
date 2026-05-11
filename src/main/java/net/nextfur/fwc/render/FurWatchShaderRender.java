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
        LightingColors preset = LightingColors.forPreset(FurWatchShaderState.getPreset());
        Vector3f sunDirection = getSunDirection(client, partialTick);

        VeilRenderSystem.setShaderLights(sunDirection, new Vector3f(sunDirection).negate());

        Quaternionf ambientRotation = rotationForDirection(new Vector3f(0.0F, -1.0F, 0.0F));
        Vec3 ambientPos = cameraPos.add(0.0D, localRadius * 0.25D, 0.0D);
        float ambientBrightness = globalIntensity * FurWatchShaderState.getAmbientIntensity();
        ambientHandle = ensureLight(
                ambientHandle,
                ambientPos,
                ambientRotation,
                preset.ambientColor(),
                ambientBrightness,
                localRadius,
                (float) Math.toRadians(85.0D),
                localRadius * 1.5F,
                FurWatchShaderState.isOcclusionEnabled()
        );

        Quaternionf sunRotation = rotationForDirection(new Vector3f(sunDirection));
        Vec3 directionalPos = cameraPos.subtract(sunDirection.x * localRadius * 0.75F, sunDirection.y * localRadius * 0.75F, sunDirection.z * localRadius * 0.75F);
        float directionalBrightness = globalIntensity * FurWatchShaderState.getDirectionalIntensity();
        directionalHandle = ensureLight(
                directionalHandle,
                directionalPos,
                sunRotation,
                preset.directionalColor(),
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

    private static Vector3f getSunDirection(Minecraft client, float partialTick) {
        float skyAngle = client.level.getTimeOfDay(partialTick) * ((float) Math.PI * 2.0F);
        return new Vector3f(Mth.cos(skyAngle), Math.max(0.15F, Mth.sin(skyAngle)), 0.35F).normalize();
    }

    private static Quaternionf rotationForDirection(Vector3f direction) {
        Vector3f normalized = new Vector3f(direction).normalize();
        Vector3f up = Math.abs(normalized.y) > 0.95F ? new Vector3f(0.0F, 0.0F, 1.0F) : new Vector3f(0.0F, 1.0F, 0.0F);
        return new Quaternionf().lookAlong(normalized.negate(), up);
    }

    private record LightingColors(Vector3f ambientColor, Vector3f directionalColor) {
        private static LightingColors forPreset(String preset) {
            return switch (preset) {
                case "warm" -> new LightingColors(new Vector3f(0.95F, 0.82F, 0.72F), new Vector3f(1.0F, 0.9F, 0.72F));
                case "moonlit" -> new LightingColors(new Vector3f(0.58F, 0.66F, 0.9F), new Vector3f(0.75F, 0.84F, 1.0F));
                default -> new LightingColors(new Vector3f(0.82F, 0.84F, 0.88F), new Vector3f(1.0F, 0.98F, 0.92F));
            };
        }
    }
}