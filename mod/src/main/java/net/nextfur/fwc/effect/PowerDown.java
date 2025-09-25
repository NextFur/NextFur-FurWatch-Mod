package net.nextfur.fwc.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class EffectPowerDown extends MobEffect {
    private int lastDuration = -1;
    private int firstDuration = -1;

    public EffectPowerDown() {
        super(MobEffectCategory.HARMFUL, 0x000000);
        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            "7107DE5E-7CE8-4030-940E-514C1F160890", 
            -1.0, 
            AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.hasEffect(MobEffects.DARKNESS)) {
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
        }
        
        super.applyEffectTick(entity, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    public String getDescriptionId() {
        return "effect.fursmp.power_down";
    }
}