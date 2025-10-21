package net.nextfur.fwc;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(value = Dist.CLIENT, modid = FwMain.MODID)
public class CommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue DEBUG_MODE = BUILDER.comment("Define if Debug Mode is True || False").define("debug", true);
    private static final ModConfigSpec.BooleanValue ENABLE_LOGIN_SYSTEM = BUILDER.comment("Enable Login System (Requires NEXTFUR Launcher)").define("enableLoginSystem", true);
    private static final ModConfigSpec.ConfigValue<String> AUTH_TOKEN = BUILDER.comment("NEXTFUR Launcher Generated Auth Token").define("authToken", "");
    
    static final ModConfigSpec SPEC = BUILDER.build();

    public static String authToken;
    public static boolean enableLoginSystem;
    public static boolean debugMode;

    public static String getAuthToken() {
        return authToken;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == CommonConfig.SPEC) {
            authToken = AUTH_TOKEN.get();
            enableLoginSystem = ENABLE_LOGIN_SYSTEM.get();
            debugMode = DEBUG_MODE.get();
        }
    }
}