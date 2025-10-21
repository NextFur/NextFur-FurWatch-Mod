package net.nextfur.fwc;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// TODO: Finish the webhook config implementation
// Sends player modlist to the webhook & check with the api prevent cheating

public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    static final ModConfigSpec SPEC = BUILDER.build();

    private static final ModConfigSpec.ConfigValue<String> FURWATCH_WEBHOOK = BUILDER.comment("Furwatch Webhook").define("furwatchWebhook", "");
    public static String furwatch_webhook;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == ServerConfig.SPEC) {
            furwatch_webhook = FURWATCH_WEBHOOK.get();
        }
    }
}