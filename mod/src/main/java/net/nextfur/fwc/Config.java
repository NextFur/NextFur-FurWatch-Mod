package net.nextfur.fwc;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = FwMain.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue DEBUG_MODE = BUILDER.comment("Define if Debug Mode is True || False").define("debug", true);
    private static final ModConfigSpec.ConfigValue<String> AUTH_TOKEN = BUILDER.comment("NEXTFUR Launcher Generated Auth Token").define("authToken", "");
    
    static final ModConfigSpec SPEC = BUILDER.build();

    public static String authToken;
    public static boolean debugMode;

    public static String getAuthToken() {
        return authToken;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == Config.SPEC) {
            authToken = AUTH_TOKEN.get();
            debugMode = DEBUG_MODE.get();

            FwMain.LOGGER.info("[FURSMP MOD] Loaded Mod Config. Token status: {}", authToken.isEmpty() ? "NOT SET" : "SET");
        }
    }
}