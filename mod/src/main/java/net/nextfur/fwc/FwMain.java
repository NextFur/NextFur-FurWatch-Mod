package net.nextfur.fwc;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import net.nextfur.fwc.client.world.CustomSkyRenderer;
import net.nextfur.fwc.init.*;
import net.nextfur.fwc.server.BlockClientInteractions;
import net.nextfur.fwc.server.ServerAuthManager;
import net.nextfur.fwc.util.events.HologramEventHandler;
import org.slf4j.Logger;

@Mod(FwMain.MODID)
public class FwMain {
    public static final String MODID = "fursmp";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FwMain(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(FwModPackets::register);

        FwModBlocks.BLOCKS.register(modEventBus);
        FwModItems.ITEMS.register(modEventBus);
        FwModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        FwModEffects.EFFECTS.register(modEventBus);
        FwModPotions.POTIONS.register(modEventBus);

        NeoForge.EVENT_BUS.register(new HologramEventHandler());
        NeoForge.EVENT_BUS.register(new CustomSkyRenderer());
        NeoForge.EVENT_BUS.register(new ServerAuthManager());
        NeoForge.EVENT_BUS.register(new BlockClientInteractions());
        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC); // Client
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC); // Server

        new ServerAuthManager();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        FwModCommands.register(event); // Comandos uwu
    }
}