package net.nextfur.fwc.util.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.nextfur.fwc.FwMain;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = FwMain.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class PlayerComponent {

	private static final String KEY_CATEGORY = "key.categories.fursmp";
	private static final KeyMapping SHADER_TOGGLE_KEY = new KeyMapping(
			"key.fursmp.shader_toggle",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_G,
			KEY_CATEGORY
	);

	private PlayerComponent() {
	}

	@SubscribeEvent
	public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(SHADER_TOGGLE_KEY);
	}

	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) {
			return;
		}

		if (!mc.isPaused()) {
			while (SHADER_TOGGLE_KEY.consumeClick()) {
				FurWatchShaderState.toggleEnabled();
				FurWatchShaderState.persist();
			}
		}
	}

	public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		FurWatchShaderState.reloadFromConfig();
	}
}
