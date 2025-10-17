package net.nextfur.fwc.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.nextfur.fwc.FwMain;

public class EffectMoonGravity extends MobEffect {
    private int lastDuration = -1;
    private int firstDuration = -1;

    public EffectMoonGravity() {
        super(MobEffectCategory.BENEFICIAL, 0xAAAAFF);
        this.addAttributeModifier(
            Attributes.GRAVITY,
            ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "moon_gravity"), 
            -0.06, 
            AttributeModifier.Operation.ADD_VALUE
        );
    }

    public boolean canBeAppliedBy(MobEffectInstance instance) {
        return true;
    }

    @Override
    public String getDescriptionId() {
        return "effect.fursmp.moon_gravity";
    }
}