package net.nextfur.fwc;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = FwMain.MODID)
public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<String>> ALLOWED_MODS = BUILDER.comment("List of Allowed Mods").define("allowedMods", new ArrayList<String>());
    private static final ModConfigSpec.ConfigValue<String> FURWATCH_WEBHOOK = BUILDER.comment("Furwatch Webhook").define("furwatchWebhook", "");

    static final ModConfigSpec SPEC = BUILDER.build();

    public static String furwatch_webhook;
    public static List<String> allowedMods;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == ServerConfig.SPEC) {
            furwatch_webhook = FURWATCH_WEBHOOK.get();
            allowedMods = ALLOWED_MODS.get();
        }
    }
}