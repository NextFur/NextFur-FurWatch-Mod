package net.nextfur.fwc.init;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.nextfur.fwc.client.world.CustomSkyRenderer;
import net.nextfur.fwc.util.events.HologramEventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FwModEvents {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register() {
        IEventBus eventBus = NeoForge.EVENT_BUS;

        eventBus.register(new HologramEventHandler());

        if (FMLLoader.getDist() == Dist.CLIENT) {
            ClientHandlerOnly.register(eventBus);
        }
    }

    private static class ClientHandlerOnly {
        public static void register(IEventBus eventBus) {
            eventBus.register(new CustomSkyRenderer());
        }
    }

}
