package net.nextfur.fwc;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import net.nextfur.fwc.api.NextFurAPI;
import net.nextfur.fwc.init.*;
import org.slf4j.Logger;

@Mod(FwMain.MODID)
public class FwMain {
    public static final String MODID = "fursmp";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static NextFurAPI FUR_API;
    public static String CLIENT_TOKEN, CLIENT_ID;

    public FwMain(IEventBus modEventBus, ModContainer modContainer) {
        CLIENT_TOKEN = System.getProperty("net.nextfur.fwc.authtoken", "Invalid User Token");
        CLIENT_ID = System.getProperty("net.nextfur.fwc.nextfurid", "Undefined");

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        modEventBus.addListener(FwModPackets::register);

        FwModParticles.PARTICLES.register(modEventBus);
        FwModSounds.SOUND_EVENTS.register(modEventBus);
        FwModBlocks.BLOCKS.register(modEventBus);
        FwModItems.ITEMS.register(modEventBus);
        FwModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        FwModEffects.EFFECTS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        FwModEvents.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        FwModCommands.register(event); // Comandos uwu
    }
}