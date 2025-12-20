package net.nextfur.fwc.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import net.nextfur.fwc.util.client.SkyColorState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LevelRenderer.class, priority = 900)
public class LevelRendererMixin {
    @Mutable
    @Shadow
    private ClientLevel level;

    @Redirect(
            method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientLevel;getSkyColor(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"
            )
    )
    private Vec3 onGetSkyColor(ClientLevel level, Vec3 position, float partialTick) {
        int color = SkyColorState.getBoxColor();

        if (color == -1) {
            return level.getSkyColor(position, partialTick);
        }

        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        return new Vec3(r, g, b);
    }
}
