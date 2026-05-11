package net.nextfur.fwc.init;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.nextfur.fwc.client.gui.OffRpScreenRenderer;
import net.nextfur.fwc.client.world.CustomSkyRenderer;
import net.nextfur.fwc.client.world.OffRpRenderer;
import net.nextfur.fwc.render.FlashLightRender;
import net.nextfur.fwc.render.FurWatchPostEffectRender;
import net.nextfur.fwc.render.FurWatchShaderRender;
import net.nextfur.fwc.render.FurWatchTaggedEmitterRender;
import net.nextfur.fwc.util.client.PlayerComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FwModEvents {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register() {
        IEventBus eventBus = NeoForge.EVENT_BUS;

        if (FMLLoader.getDist() == Dist.CLIENT) {
            ClientHandlerOnly.register(eventBus);
        }
    }

    private static class ClientHandlerOnly {
        public static void register(IEventBus eventBus) {
            eventBus.register(new CustomSkyRenderer());
            eventBus.addListener(OffRpRenderer::onRenderWorld);
            eventBus.addListener(OffRpScreenRenderer::onRenderOverlay);
            eventBus.addListener(PlayerComponent::onClientTick);
            eventBus.addListener(PlayerComponent::onClientLogout);
            eventBus.addListener(FlashLightRender::onRenderWorld);
            eventBus.addListener(FurWatchShaderRender::onRenderWorld);
            eventBus.addListener(FurWatchTaggedEmitterRender::onRenderWorld);
            eventBus.addListener(FurWatchPostEffectRender::onRenderWorld);
        }
    }

}
