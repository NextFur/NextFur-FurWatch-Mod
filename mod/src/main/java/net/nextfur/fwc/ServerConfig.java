package net.nextfur.fwc;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.nextfur.fwc.api.NextFurAPI;


@EventBusSubscriber(modid = FwMain.MODID)
public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<String> LOGIN_URL = BUILDER.comment("Player authentication URL - NextFur").define("loginUrl", "");
    private static final ModConfigSpec.ConfigValue<String> NEXTFUR_API_URL = BUILDER.comment("API URL - NextFur").define("nextfurApiUrl", "");
    private static final ModConfigSpec.ConfigValue<String> NEXTFUR_API_KEY = BUILDER.comment("API Key - NextFur").define("nextfurApiKey", "");

    static final ModConfigSpec SPEC = BUILDER.build();

    public static String login_url;
    public static String api_url;
    public static String api_key;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == ServerConfig.SPEC) {
            login_url = LOGIN_URL.get();
            api_url = NEXTFUR_API_URL.get();
            api_key = NEXTFUR_API_KEY.get();

            if (FMLEnvironment.dist.isDedicatedServer()) {
                FwMain.FUR_API = new NextFurAPI(api_url, api_key);
                FwMain.LOGGER.info("NextFur API inicializada com sucesso.");
            }
        }
    }
}