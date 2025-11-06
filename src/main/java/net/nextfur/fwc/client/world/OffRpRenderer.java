package net.nextfur.fwc.client.world;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class OffRpRenderer {
    public static final Set<UUID> OFFRP_PLAYERS = new HashSet<>();

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        for (Player player : mc.level.players()) {
            if (!OFFRP_PLAYERS.contains(player.getUUID())) continue;
            if (mc.player.getUUID().equals(player.getUUID())) continue;

            double distanceSqr = mc.getEntityRenderDispatcher().distanceToSqr(player);
            if (!ClientHooks.isNameplateInRenderDistance(player, distanceSqr)) {
                continue;
            }

            renderNameTag(
                    player,
                    event.getPartialTick().getGameTimeDeltaPartialTick(true),
                    event.getPoseStack(),
                    event.getCamera()
            );
        }
    }

    private static void renderNameTag(Player player, float partialTick, PoseStack matrixStack, Camera camera) {
        Minecraft instance = Minecraft.getInstance();
        Vec3 vec3 = player.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, player.getViewYRot(partialTick));

        Vec3 camPos = camera.getPosition();
        float offset = 0.7f;

        double playerX = player.xo + (player.getX() - player.xo) * partialTick;
        double playerY = player.yo + (player.getY() - player.yo) * partialTick;
        double playerZ = player.zo + (player.getZ() - player.zo) * partialTick;

        double nameTagYOffset = (vec3 != null ? vec3.y : player.getBbHeight()) + offset;
        double nameTagXOffset = vec3 != null ? vec3.x : 0;
        double nameTagZOffset = vec3 != null ? vec3.z : 0;

        matrixStack.pushPose();
        matrixStack.translate(
                (playerX + nameTagXOffset) - camPos.x,
                (playerY + nameTagYOffset) - camPos.y,
                (playerZ + nameTagZOffset) - camPos.z
        );
        matrixStack.mulPose(instance.getEntityRenderDispatcher().cameraOrientation());
        matrixStack.scale(0.025F, -0.025F, 0.025F);

        String text = ChatFormatting.LIGHT_PURPLE + "[OFF-RP]";

        Font font = instance.font;
        float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int)(f * 255.0F) << 24;
        float f1 = (float)(-font.width(text) / 2);


        boolean flag = !player.isDiscrete();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        int packedLight = LightTexture.pack(15, 15);

        instance.font.drawInBatch(text, f1, -offset, 553648127, false, matrixStack.last().pose(), bufferSource, flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, packedLight);
        if (flag) instance.font.drawInBatch(text, f1, -offset, -1, false, matrixStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight);

        matrixStack.popPose();
    }
}
