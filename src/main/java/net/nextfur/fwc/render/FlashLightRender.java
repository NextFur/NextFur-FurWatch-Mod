package net.nextfur.fwc.render;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.nextfur.fwc.util.client.PlayerComponent;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FlashLightRender {

    private static final float LIGHT_BRIGHTNESS = 1.0F;
    private static final float LIGHT_DISTANCE = 24.0F;
    private static final float LIGHT_ANGLE = 0.26F;
    private static final float LIGHT_FILL_ANGLE = (float) Math.toRadians(85.0F);
    private static final float ORIENTATION_SMOOTHING = 0.35F;
    private static final double MAX_DISTANCE_SQR = 96.0D * 96.0D;
    private static final Map<UUID, PlayerAreaLights> ACTIVE_LIGHTS = new HashMap<>();

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
        if (VeilRenderSystem.renderer() == null || VeilRenderSystem.renderer().getLightRenderer() == null) {
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
            if (player.isSpectator() && !playerId.equals(client.player.getUUID())) {
                continue;
            }
            if (client.player.distanceToSqr(player) > MAX_DISTANCE_SQR) {
                continue;
            }

                Vec3 lightPos = player.getEyePosition(partialTick);
                Quaternionf targetRotation = createOrientation(player, partialTick);

                PlayerAreaLights playerLights = ACTIVE_LIGHTS.get(playerId);
                if (playerLights == null || !playerLights.isValid()) {
                AreaLightData fillLight = new AreaLightData()
                    .setBrightness(LIGHT_BRIGHTNESS * 0.75F)
                    .setDistance(LIGHT_DISTANCE)
                    .setAngle(LIGHT_FILL_ANGLE)
                    .setSize(0.0F, 0.0F)
                    .setOcclusionEnabled(false);
                fillLight.getPosition().set(lightPos.x, lightPos.y, lightPos.z);
                fillLight.getOrientation().set(targetRotation);

                AreaLightData coneLight = new AreaLightData()
                    .setBrightness(LIGHT_BRIGHTNESS)
                    .setDistance(LIGHT_DISTANCE)
                    .setAngle(LIGHT_ANGLE)
                    .setSize(0.0F, 0.0F)
                    .setOcclusionEnabled(false);
                coneLight.getPosition().set(lightPos.x, lightPos.y, lightPos.z);
                coneLight.getOrientation().set(targetRotation);

                LightRenderHandle<AreaLightData> fillHandle = VeilRenderSystem.renderer().getLightRenderer().addLight(fillLight);
                LightRenderHandle<AreaLightData> coneHandle = VeilRenderSystem.renderer().getLightRenderer().addLight(coneLight);
                ACTIVE_LIGHTS.put(playerId, new PlayerAreaLights(fillHandle, coneHandle));
            } else {
                updateLight(playerLights.fillHandle(), lightPos, targetRotation);
                updateLight(playerLights.coneHandle(), lightPos, targetRotation);
            }

            visibleActiveLights.add(playerId);
        }

        removeInactiveLights(visibleActiveLights);
    }

    private static void removeInactiveLights(Set<UUID> keep) {
        Set<UUID> toRemove = new HashSet<>(ACTIVE_LIGHTS.keySet());
        toRemove.removeAll(keep);

        for (UUID uuid : toRemove) {
            PlayerAreaLights lights = ACTIVE_LIGHTS.remove(uuid);
            if (lights != null) {
                lights.free();
            }
        }
    }

    public static void clearAllLights() {
        for (PlayerAreaLights lights : ACTIVE_LIGHTS.values()) {
            lights.free();
        }
        ACTIVE_LIGHTS.clear();
    }

    private static Quaternionf createOrientation(Player player, float partialTick) {
        float yaw = (float) Math.toRadians(Mth.rotLerp(partialTick, player.yRotO, player.getYRot()));
        float pitch = (float) -Math.toRadians(Mth.lerp(partialTick, player.xRotO, player.getXRot()));
        return new Quaternionf().rotateXYZ(pitch, yaw, 0.0F);
    }

    private static void updateLight(LightRenderHandle<AreaLightData> handle, Vec3 lightPos, Quaternionf targetRotation) {
        AreaLightData light = handle.getLightData();
        light.getOrientation().slerp(targetRotation, ORIENTATION_SMOOTHING);
        light.getPosition().set(lightPos.x, lightPos.y, lightPos.z);
        handle.markDirty();
    }

    private record PlayerAreaLights(
            LightRenderHandle<AreaLightData> fillHandle,
            LightRenderHandle<AreaLightData> coneHandle
    ) {
        private boolean isValid() {
            return this.fillHandle != null
                    && this.fillHandle.isValid()
                    && this.coneHandle != null
                    && this.coneHandle.isValid();
        }

        private void free() {
            if (this.fillHandle != null) {
                this.fillHandle.free();
            }
            if (this.coneHandle != null) {
                this.coneHandle.free();
            }
        }
    }
}
