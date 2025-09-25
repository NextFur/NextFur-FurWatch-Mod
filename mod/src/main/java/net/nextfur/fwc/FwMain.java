package net.nextfur.fwc;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.nextfur.fwc.init.FwModBlocks;
import net.nextfur.fwc.init.FwModCreativeTabs;
import net.nextfur.fwc.init.FwModItems;
import net.nextfur.fwc.init.FwModEffects;
import net.nextfur.fwc.commands.OffRpCommand;
import net.nextfur.fwc.network.ClientAuthPacket;
import net.nextfur.fwc.server.ServerLoader;
import net.nextfur.fwc.util.events.HologramEventHandler;
import org.slf4j.Logger;

@Mod(FwMain.MODID)
public class FwMain {
    public static final String MODID = "fursmp";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FwMain(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPackets);

        FwModBlocks.BLOCKS.register(modEventBus);
        FwModItems.ITEMS.register(modEventBus);
        FwModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        FwModEffects.EFFECTS.register(modEventBus);

        NeoForge.EVENT_BUS.register(new HologramEventHandler());
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[FURSMP] Common setup complete.");

        if(Config.debugMode) {
            LOGGER.info("[FURSMP] Debug mode is enabled.");
        } else {
            LOGGER.info("[FURSMP] Debug mode is disabled.");
        }
    }

    private void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID);

        registrar.playToServer(
                ClientAuthPacket.TYPE,
                ClientAuthPacket.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        var player = context.player();
                        if (player != null) {
                            String username = player.getName().getString();
                            LOGGER.info("[FURSMP] Received auth token from user: " + username);
                            ServerLoader.pendingTokens.put(username, payload.getToken());
                        }
                    });
                }
        );
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[FURSMP] Initializing server components.");
        
        // Register the OffRP command
        OffRpCommand.register(event.getServer().getCommands().getDispatcher());
        LOGGER.info("[FURSMP] Commands registered successfully.");
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("[FURSMP MOD] Client Setup");
        }
    }
}