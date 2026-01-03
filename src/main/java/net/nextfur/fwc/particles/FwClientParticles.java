package net.nextfur.fwc.particles;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.init.FwModParticles;

@EventBusSubscriber(modid = FwMain.MODID, value = Dist.CLIENT)
public class FwClientParticles {

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(FwModParticles.EMP_PARTICLE.get(), ParticleProviderEMP::new);

        event.registerSpriteSet(FwModParticles.CARMESIM_SPURS_PARTICLE.get(), ParticleProviderSpurs::new);
        event.registerSpriteSet(FwModParticles.SPURS_PARTICLE.get(), ParticleProviderSpurs::new);
    }

}
