package net.nextfur.fwc.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.*;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL21C.GL_PIXEL_UNPACK_BUFFER;
import static org.lwjgl.opengl.GL30C.GL_R8;

@Mixin(targets = "foundry.veil.impl.client.render.light.VoxelShadowGrid", remap = false)
public abstract class VoxelShadowGridMixin {
    @Shadow
    private static int textureId;

    @Inject(method = "ensureTexture", at = @At("HEAD"), cancellable = true)
    private static void furwatch$safeEnsureTexture(CallbackInfo ci) {
        if (textureId != 0) {
            ci.cancel();
            return;
        }

        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
        glPixelStorei(GL_UNPACK_IMAGE_HEIGHT, 0);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
        glPixelStorei(GL_UNPACK_SKIP_IMAGES, 0);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_3D, textureId);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        // Safe texture allocation without reading client memory buffer (prevents EXCEPTION_ACCESS_VIOLATION in Nvidia driver)
        glTexImage3D(GL_TEXTURE_3D, 0, GL_R8, 64, 64, 64, 0, GL_RED, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glBindTexture(GL_TEXTURE_3D, 0);
        ci.cancel();
    }

    @Inject(method = "uploadBuffer", at = @At("HEAD"))
    private static void furwatch$resetPixelStoreBeforeUpload(ByteBuffer buffer, CallbackInfo ci) {
        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
        glPixelStorei(GL_UNPACK_IMAGE_HEIGHT, 0);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
        glPixelStorei(GL_UNPACK_SKIP_IMAGES, 0);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    }

    @Inject(method = "uploadBuffer", at = @At("RETURN"))
    private static void furwatch$resetPixelStoreAfterUpload(ByteBuffer buffer, CallbackInfo ci) {
        glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
    }
}
