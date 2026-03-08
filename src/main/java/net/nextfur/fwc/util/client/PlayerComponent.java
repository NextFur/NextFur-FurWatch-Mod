package net.nextfur.fwc.util.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.network.common.FlashlightToggleC2SPacket;
import net.nextfur.fwc.init.FwModSounds;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = FwMain.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class PlayerComponent {

	private static final String KEY_CATEGORY = "key.categories.fursmp";
	private static final KeyMapping FLASHLIGHT_KEY = new KeyMapping(
			"key.fursmp.flashlight",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_F,
			KEY_CATEGORY
	);

	private static final Map<UUID, Boolean> FLASHLIGHT_STATES = new HashMap<>();

	private PlayerComponent() {
	}

	@SubscribeEvent
	public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(FLASHLIGHT_KEY);
	}

	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) {
			return;
		}

		while (FLASHLIGHT_KEY.consumeClick()) {
			UUID playerId = mc.player.getUUID();
			boolean nextState = !isFlashlightEnabled(playerId);
			setPlayerFlashlight(playerId, nextState);
			PacketDistributor.sendToServer(new FlashlightToggleC2SPacket(nextState));
		}
	}

	public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		FLASHLIGHT_STATES.clear();
	}

	public static boolean isFlashlightEnabled(UUID playerId) {
		return FLASHLIGHT_STATES.getOrDefault(playerId, false);
	}

	public static void setPlayerFlashlight(UUID playerId, boolean enabled) {
		if (enabled) {
			FLASHLIGHT_STATES.put(playerId, true);
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null) {
			mc.getSoundManager().play(SimpleSoundInstance.forUI(FwModSounds.FLASHLIGHT_TOGGLE.get(), 1.0F));
		}
		FLASHLIGHT_STATES.remove(playerId);
	}

	public static Map<UUID, Boolean> getSnapshot() {
		return new HashMap<>(FLASHLIGHT_STATES);
	}

	public static void applySnapshot(Map<UUID, Boolean> snapshot) {
		FLASHLIGHT_STATES.clear();
		snapshot.forEach((uuid, enabled) -> {
			if (enabled) {
				FLASHLIGHT_STATES.put(uuid, true);
			}
		});
	}
}
