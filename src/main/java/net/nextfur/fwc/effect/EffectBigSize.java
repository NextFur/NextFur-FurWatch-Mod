package net.nextfur.fwc.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.nextfur.fwc.FwMain;

public class EffectBigSize extends MobEffect {
    public EffectBigSize() {
        super(MobEffectCategory.BENEFICIAL, 0xAAAAFF);
        this.addAttributeModifier(
            Attributes.SCALE,
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "big_size"), 
            0.4, 
            AttributeModifier.Operation.ADD_VALUE
        );
    }

    public boolean canBeAppliedBy(MobEffectInstance instance) {
        return true;
    }

    @Override
    public String getDescriptionId() {
        return "effect.fursmp.big_size";
    }
}