package net.nextfur.fwc.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.nextfur.fwc.client.gui.FurWatchShaderOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.screens.options.VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin extends Screen {
    protected VideoSettingsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void furwatch$addButton(CallbackInfo callbackInfo) {
        this.addRenderableWidget(Button.builder(Component.translatable("button.fursmp.open_shader_settings"), button ->
                Minecraft.getInstance().setScreen(new FurWatchShaderOptionsScreen((Screen) (Object) this)))
                .pos(this.width - 114, 8)
                .size(106, 20)
                .build());
    }
}