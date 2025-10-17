package net.nextfur.fwc;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
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

import net.nextfur.fwc.client.world.CustomSkyRenderer;
import net.nextfur.fwc.commands.SkyColorCommand;
import net.nextfur.fwc.commands.TitleMenuCommand;
import net.nextfur.fwc.init.FwModBlocks;
import net.nextfur.fwc.init.FwModCreativeTabs;
import net.nextfur.fwc.init.FwModItems;
import net.nextfur.fwc.init.FwModEffects;
import net.nextfur.fwc.init.FwModPotions;
import net.nextfur.fwc.commands.OffRpCommand;
import net.nextfur.fwc.commands.LoveLevelCommand;
import net.nextfur.fwc.network.ClientAuthPacket;
import net.nextfur.fwc.network.gui.OpenSkyColorMenuPacket;
import net.nextfur.fwc.network.gui.OpenTitleMenuPacket;
import net.nextfur.fwc.network.world.SkyColorChangePacket;
import net.nextfur.fwc.network.world.SkyColorSyncPacket;
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
        FwModPotions.POTIONS.register(modEventBus);

        NeoForge.EVENT_BUS.register(new HologramEventHandler());
        NeoForge.EVENT_BUS.register(new CustomSkyRenderer());
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

        registrar.playToClient(
                OpenTitleMenuPacket.TYPE,
                OpenTitleMenuPacket.STREAM_CODEC,
                (packet, ctx) -> {
                    OpenTitleMenuPacket.handle(packet);
                }
        );

        registrar.playToClient(
                OpenSkyColorMenuPacket.TYPE,
                OpenSkyColorMenuPacket.STREAM_CODEC,
                (packet, ctx) -> {
                    OpenSkyColorMenuPacket.handle(packet);
                }
        );

        registrar.playToServer(
                SkyColorChangePacket.TYPE,
                SkyColorChangePacket.STREAM_CODEC,
                (packet, ctx) -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        SkyColorChangePacket.handle(packet, player);
                    }
                }
        );

        registrar.playToClient(
                SkyColorSyncPacket.TYPE,
                SkyColorSyncPacket.STREAM_CODEC,
                (packet, ctx) -> SkyColorSyncPacket.handle(packet)
        );
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[FURSMP] Initializing server components.");

        OffRpCommand.register(event.getServer().getCommands().getDispatcher()); //offrp
        LoveLevelCommand.register(event.getServer().getCommands().getDispatcher()); //lovelevel
        TitleMenuCommand.register(event.getServer().getCommands().getDispatcher()); //tmenu
        SkyColorCommand.register(event.getServer().getCommands().getDispatcher()); //skycolor
        
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