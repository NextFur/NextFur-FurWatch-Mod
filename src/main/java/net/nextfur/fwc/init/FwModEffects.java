package net.nextfur.fwc.init;

import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.effect.*;

public class FwModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, FwMain.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> EMP_EFFECT = EFFECTS.register("emp", () -> new EffectEMP(FwModParticles.EMP_PARTICLE));
    public static final DeferredHolder<MobEffect, MobEffect> POWER_DOWN = EFFECTS.register("power_down", EffectPowerDown::new);
    public static final DeferredHolder<MobEffect, MobEffect> MOON_GRAVITY = EFFECTS.register("moon_gravity", EffectMoonGravity::new);
    public static final DeferredHolder<MobEffect, MobEffect> BIG_SIZE = EFFECTS.register("big_size", EffectBigSize::new);
    public static final DeferredHolder<MobEffect, MobEffect> SMALL_SIZE = EFFECTS.register("small_size", EffectSmallSize::new);
}