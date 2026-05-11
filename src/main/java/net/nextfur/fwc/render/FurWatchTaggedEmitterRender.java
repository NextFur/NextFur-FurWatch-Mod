package net.nextfur.fwc.render;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.util.client.FurWatchShaderState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class FurWatchTaggedEmitterRender {
    private static final TagKey<Block> WARM_EMITTERS = blockTag("warm_emitters");
    private static final TagKey<Block> COLD_EMITTERS = blockTag("cold_emitters");
    private static final TagKey<Block> SCREEN_EMITTERS = blockTag("screen_emitters");
    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final int MAX_SCAN_RADIUS = 12;
    private static final Map<BlockPos, EmitterLight> ACTIVE_LIGHTS = new HashMap<>();

    private static BlockPos lastScanCenter;
    private static long lastScanGameTime = Long.MIN_VALUE;

    private FurWatchTaggedEmitterRender() {
    }

    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            clearAllLights();
            return;
        }
        if (!FurWatchShaderState.isEnabled()) {
            clearAllLights();
            return;
        }
        if (VeilRenderSystem.renderer() == null || VeilRenderSystem.renderer().getLightRenderer() == null) {
            clearAllLights();
            return;
        }

        BlockPos center = client.player.blockPosition();
        long gameTime = client.level.getGameTime();
        if (shouldRescan(center, gameTime)) {
            scanEmitters(center);
            lastScanCenter = center.immutable();
            lastScanGameTime = gameTime;
        }

        refreshActiveLights();
    }

    public static void clearAllLights() {
        for (EmitterLight emitterLight : ACTIVE_LIGHTS.values()) {
            emitterLight.free();
        }
        ACTIVE_LIGHTS.clear();
        lastScanCenter = null;
        lastScanGameTime = Long.MIN_VALUE;
    }

    private static boolean shouldRescan(BlockPos center, long gameTime) {
        return lastScanCenter == null
                || !lastScanCenter.equals(center)
                || gameTime - lastScanGameTime >= SCAN_INTERVAL_TICKS;
    }

    private static void scanEmitters(BlockPos center) {
        Minecraft client = Minecraft.getInstance();
        int scanRadius = Mth.clamp(Mth.ceil(FurWatchShaderState.getLocalLightRadius() * 0.4F), 5, MAX_SCAN_RADIUS);
        int verticalRadius = Math.max(4, scanRadius / 2);
        Set<BlockPos> keep = new HashSet<>();

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-scanRadius, -verticalRadius, -scanRadius), center.offset(scanRadius, verticalRadius, scanRadius))) {
            BlockState state = client.level.getBlockState(pos);
            EmitterProfile profile = resolveProfile(state);
            if (profile == null) {
                continue;
            }

            BlockPos immutablePos = pos.immutable();
            keep.add(immutablePos);
            EmitterLight emitterLight = ACTIVE_LIGHTS.get(immutablePos);
            if (emitterLight == null || !emitterLight.isValid()) {
                ACTIVE_LIGHTS.put(immutablePos, createLight(immutablePos, state, profile));
            } else {
                emitterLight.profile = profile;
                updateLight(emitterLight.handle, immutablePos, state, profile);
            }
        }

        removeInactiveLights(keep);
    }

    private static void refreshActiveLights() {
        Minecraft client = Minecraft.getInstance();
        Set<BlockPos> toRemove = new HashSet<>();

        for (Map.Entry<BlockPos, EmitterLight> entry : ACTIVE_LIGHTS.entrySet()) {
            BlockPos pos = entry.getKey();
            EmitterLight emitterLight = entry.getValue();
            if (!emitterLight.isValid()) {
                toRemove.add(pos);
                continue;
            }

            BlockState state = client.level.getBlockState(pos);
            EmitterProfile profile = resolveProfile(state);
            if (profile == null) {
                toRemove.add(pos);
                continue;
            }

            emitterLight.profile = profile;
            updateLight(emitterLight.handle, pos, state, profile);
        }

        for (BlockPos pos : toRemove) {
            EmitterLight removed = ACTIVE_LIGHTS.remove(pos);
            if (removed != null) {
                removed.free();
            }
        }
    }

    private static void removeInactiveLights(Set<BlockPos> keep) {
        Set<BlockPos> toRemove = new HashSet<>(ACTIVE_LIGHTS.keySet());
        toRemove.removeAll(keep);

        for (BlockPos pos : toRemove) {
            EmitterLight emitterLight = ACTIVE_LIGHTS.remove(pos);
            if (emitterLight != null) {
                emitterLight.free();
            }
        }
    }

    private static EmitterLight createLight(BlockPos pos, BlockState state, EmitterProfile profile) {
        AreaLightData light = new AreaLightData();
        LightRenderHandle<AreaLightData> handle = VeilRenderSystem.renderer().getLightRenderer().addLight(light);
        EmitterLight emitterLight = new EmitterLight(handle, profile);
        updateLight(handle, pos, state, profile);
        return emitterLight;
    }

    private static void updateLight(LightRenderHandle<AreaLightData> handle, BlockPos pos, BlockState state, EmitterProfile profile) {
        Direction direction = resolveDirection(state, profile.directional);
        Vec3 position = resolvePosition(pos, direction, profile.directional);
        Quaternionf rotation = rotationForDirection(new Vector3f(direction.getStepX(), direction.getStepY(), direction.getStepZ()));
        float distance = FurWatchShaderState.getLocalLightRadius() * profile.distanceMultiplier;
        float brightness = FurWatchShaderState.getLocalLightBrightness() * profile.brightnessMultiplier;

        AreaLightData light = handle.getLightData();
        light.setColor(profile.color.x, profile.color.y, profile.color.z);
        light.setBrightness(brightness);
        light.setDistance(distance);
        light.setAngle(profile.angle);
        light.setSize(profile.size, profile.size);
        light.setOcclusionEnabled(FurWatchShaderState.isOcclusionEnabled());
        light.getPosition().set(position.x, position.y, position.z);
        light.getOrientation().set(rotation);
        handle.markDirty();
    }

    private static Direction resolveDirection(BlockState state, boolean directional) {
        if (!directional) {
            return Direction.DOWN;
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return Direction.DOWN;
    }

    private static Vec3 resolvePosition(BlockPos pos, Direction direction, boolean directional) {
        Vec3 center = Vec3.atCenterOf(pos);
        if (!directional) {
            return center.add(0.0D, 0.2D, 0.0D);
        }
        return center.add(direction.getStepX() * 0.35D, direction.getStepY() * 0.2D, direction.getStepZ() * 0.35D);
    }

    private static EmitterProfile resolveProfile(BlockState state) {
        if (state.is(SCREEN_EMITTERS)) {
            return EmitterProfile.SCREEN;
        }
        if (state.is(COLD_EMITTERS)) {
            return EmitterProfile.COLD;
        }
        if (state.is(WARM_EMITTERS)) {
            return EmitterProfile.WARM;
        }
        return null;
    }

    private static Quaternionf rotationForDirection(Vector3f direction) {
        Vector3f normalized = new Vector3f(direction).normalize();
        Vector3f up = Math.abs(normalized.y) > 0.95F ? new Vector3f(0.0F, 0.0F, 1.0F) : new Vector3f(0.0F, 1.0F, 0.0F);
        return new Quaternionf().lookAlong(normalized.negate(), up);
    }

    private static TagKey<Block> blockTag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(FwMain.MODID, path));
    }

    private static final class EmitterLight {
        private final LightRenderHandle<AreaLightData> handle;
        private EmitterProfile profile;

        private EmitterLight(LightRenderHandle<AreaLightData> handle, EmitterProfile profile) {
            this.handle = handle;
            this.profile = profile;
        }

        private boolean isValid() {
            return this.handle != null && this.handle.isValid();
        }

        private void free() {
            if (this.handle != null) {
                this.handle.free();
            }
        }
    }

    private static final class EmitterProfile {
        private static final EmitterProfile WARM = new EmitterProfile(new Vector3f(1.0F, 0.78F, 0.52F), 0.95F, 0.55F, (float) Math.toRadians(82.0D), 0.45F, false);
        private static final EmitterProfile COLD = new EmitterProfile(new Vector3f(0.52F, 0.74F, 1.0F), 0.9F, 0.6F, (float) Math.toRadians(82.0D), 0.45F, false);
        private static final EmitterProfile SCREEN = new EmitterProfile(new Vector3f(0.42F, 0.86F, 1.0F), 1.15F, 0.75F, (float) Math.toRadians(58.0D), 0.8F, true);

        private final Vector3f color;
        private final float brightnessMultiplier;
        private final float distanceMultiplier;
        private final float angle;
        private final float size;
        private final boolean directional;

        private EmitterProfile(Vector3f color, float brightnessMultiplier, float distanceMultiplier, float angle, float size, boolean directional) {
            this.color = color;
            this.brightnessMultiplier = brightnessMultiplier;
            this.distanceMultiplier = distanceMultiplier;
            this.angle = angle;
            this.size = size;
            this.directional = directional;
        }
    }
}