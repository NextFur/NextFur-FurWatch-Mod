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
        FwModBlocks.BLOCKS.register(modEventBus);
        FwModItems.ITEMS.register(modEventBus);
        FwDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        FwAttachments.ATTACHMENT_TYPES.register(modEventBus);
        FwModMenus.MENUS.register(modEventBus);
        FwModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        FwModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        FwModEffects.EFFECTS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);

        if (net.neoforged.fml.loading.FMLLoader.getDist() == net.neoforged.api.distmarker.Dist.CLIENT) {
            net.nextfur.fwc.client.gui.EconomyClientMenuEvents.register(modEventBus);
        }

        FwModEvents.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        net.nextfur.fwc.economy.db.EconomyDatabaseManager.getInstance().initialize(event.getServer());
        FwModCommands.register(event); // Comandos uwu
    }

    @SubscribeEvent
    public void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        net.nextfur.fwc.economy.db.EconomyDatabaseManager.getInstance().close();
    }
}