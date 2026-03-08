package net.nextfur.fwc.render;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import foundry.veil.api.client.render.rendertype.layer.RenderTypeLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.nextfur.fwc.util.client.PlayerComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FlashLightRender {

    private static final Class<?> VEIL_RENDER_LAYER = RenderTypeLayer.class;
    private static final float LIGHT_RADIUS = 10.0F;
    private static final float LIGHT_BRIGHTNESS = 1.2F;
    private static final double MAX_DISTANCE_SQR = 96.0D * 96.0D;
    private static final Map<UUID, LightRenderHandle<PointLightData>> ACTIVE_LIGHTS = new HashMap<>();

    private FlashLightRender() {
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

        Set<UUID> visibleActiveLights = new HashSet<>();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        for (Player player : client.level.players()) {
            UUID playerId = player.getUUID();
            if (!PlayerComponent.isFlashlightEnabled(playerId)) {
                continue;
            }
            if (!player.isAlive() || player.isRemoved()) {
                continue;
            }
            if (client.player.distanceToSqr(player) > MAX_DISTANCE_SQR) {
                continue;
            }

            Vec3 look = player.getViewVector(partialTick);
            Vec3 lightPos = player.getEyePosition(partialTick).add(look.scale(0.45D));

            LightRenderHandle<PointLightData> handle = ACTIVE_LIGHTS.get(playerId);
            if (handle == null || !handle.isValid()) {
                PointLightData light = new PointLightData()
                        .setColor(1.0F, 0.95F, 0.82F)
                        .setBrightness(LIGHT_BRIGHTNESS)
                        .setRadius(LIGHT_RADIUS)
                        .setOcclusionEnabled(false)
                        .setPosition(lightPos.x, lightPos.y, lightPos.z);

                handle = VeilRenderSystem.renderer().getLightRenderer().addLight(light);
                ACTIVE_LIGHTS.put(playerId, handle);
            } else {
                PointLightData light = handle.getLightData();
                light.setPosition(lightPos.x, lightPos.y, lightPos.z);
                light.setBrightness(LIGHT_BRIGHTNESS);
                light.setRadius(LIGHT_RADIUS);
                handle.markDirty();
            }

            visibleActiveLights.add(playerId);
        }

        removeInactiveLights(visibleActiveLights);
    }

    private static void removeInactiveLights(Set<UUID> keep) {
        Set<UUID> toRemove = new HashSet<>(ACTIVE_LIGHTS.keySet());
        toRemove.removeAll(keep);

        for (UUID uuid : toRemove) {
            LightRenderHandle<PointLightData> handle = ACTIVE_LIGHTS.remove(uuid);
            if (handle != null) {
                handle.free();
            }
        }
    }

    public static void clearAllLights() {
        for (LightRenderHandle<PointLightData> handle : ACTIVE_LIGHTS.values()) {
            handle.free();
        }
        ACTIVE_LIGHTS.clear();
    }
}
