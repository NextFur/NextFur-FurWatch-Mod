package net.nextfur.fwc.init;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;

import java.util.function.Supplier;

public class FwModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, FwMain.MODID);

    public static final Supplier<SimpleParticleType> EMP_PARTICLE = PARTICLES.register(
            "emp_particle", () -> new SimpleParticleType(false)
    );

    public static final Supplier<SimpleParticleType> SPURS_PARTICLE = PARTICLES.register(
            "spurs_particle", () -> new SimpleParticleType(false)
    );

    public static final Supplier<SimpleParticleType> CARMESIM_SPURS_PARTICLE = PARTICLES.register(
            "carmesim_spurs_particle", () -> new SimpleParticleType(false)
    );
}
