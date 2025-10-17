package net.nextfur.fwc.init;

import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.effect.EffectPowerDown;
import net.nextfur.fwc.effect.EffectMoonGravity;

public class FwModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, FwMain.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> POWER_DOWN = EFFECTS.register(
        "power_down",
        EffectPowerDown::new
    );
    
    public static final DeferredHolder<MobEffect, MobEffect> MOON_GRAVITY = EFFECTS.register(
        "moon_gravity",
        EffectMoonGravity::new
    );

    public static final DeferredHolder<MobEffect, MobEffect> BIG_SIZE = EFFECTS.register(
        "big_size",
        () -> new net.nextfur.fwc.effect.EffectBigSize()
    );

    public static final DeferredHolder<MobEffect, MobEffect> SMALL_SIZE = EFFECTS.register(
        "small_size",
        () -> new net.nextfur.fwc.effect.EffectSmallSize()
    );
}