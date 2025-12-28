package net.nextfur.fwc.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.nextfur.fwc.FwMain;

public class EffectPowerDown extends MobEffect {
    public EffectPowerDown() {
        super(MobEffectCategory.HARMFUL, 0x000000);
        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "power_down_speed"), 
            -0.1, 
            AttributeModifier.Operation.ADD_VALUE
        );
        this.addAttributeModifier(
            Attributes.JUMP_STRENGTH,
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "power_down_jump"), 
            -0.42, 
            AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.hasEffect(MobEffects.DARKNESS)) {
            entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 40, 0, false, false));
        }
        
        return true;
    }

    public boolean canBeAppliedBy(MobEffectInstance instance) {
        return true;
    }

    @Override
    public String getDescriptionId() {
        return "effect.fursmp.power_down";
    }
}