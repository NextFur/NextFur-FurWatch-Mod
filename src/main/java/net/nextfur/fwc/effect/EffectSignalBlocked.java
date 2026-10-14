package net.nextfur.fwc.effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.nextfur.fwc.init.FwModParticles;

public class EffectSignalBlocked extends MobEffect {
    public EffectSignalBlocked() {
        super(MobEffectCategory.HARMFUL, 0x4B6E40);
    }

    @Override
    public ParticleOptions createParticleOptions(MobEffectInstance effect) {
        return FwModParticles.EMP_PARTICLE.get();
    }

    @Override
    public String getDescriptionId() {
        return "effect.fursmp.signal_blocked";
    }
}
