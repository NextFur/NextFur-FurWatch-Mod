package net.nextfur.fwc.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.nextfur.fwc.FwMain;
import net.neoforged.neoforge.registries.DeferredHolder;

public class FwModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, FwMain.MODID);

    public static final DeferredHolder<Potion, Potion> MOON_GRAVITY_POTION = POTIONS.register("moon_gravity_potion", 
        () -> new Potion(new MobEffectInstance(FwModEffects.MOON_GRAVITY, 3600)));

    public static final DeferredHolder<Potion, Potion> MOON_GRAVITY_SPLASH_POTION = POTIONS.register("moon_gravity_splash_potion",
        () -> new Potion(new MobEffectInstance(FwModEffects.MOON_GRAVITY, 3600)));
}
