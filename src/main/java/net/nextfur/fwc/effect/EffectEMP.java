package net.nextfur.fwc.effect;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.function.Supplier;

public class EffectEMP extends MobEffect {
    private final Supplier<SimpleParticleType> particleSupplier;

    public EffectEMP(Supplier<SimpleParticleType> particleSupplier) {
        super(MobEffectCategory.NEUTRAL, 0x000000);
        this.particleSupplier = particleSupplier;
    }

    @Override
    public ParticleOptions createParticleOptions(MobEffectInstance effect) {
        return this.particleSupplier.get();
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            player.getInventory().items.forEach(stack -> drainEnergy(stack, amplifier));
            player.getInventory().armor.forEach(stack -> drainEnergy(stack, amplifier));
            player.getInventory().offhand.forEach(stack -> drainEnergy(stack, amplifier));

            IEnergyStorage playerEnergy = player.getCapability(Capabilities.EnergyStorage.ENTITY, null);
            if (playerEnergy != null && playerEnergy.getEnergyStored() > 0) {
                playerEnergy.extractEnergy(playerEnergy.getEnergyStored(), false);
            }
        }
        return true;
    }

    private void drainEnergy(ItemStack stack, int amplifier) {
        if(stack.isEmpty()) return;
        var cap = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (cap != null && cap.getEnergyStored() > 0) {
            int factor = Math.min(1, amplifier + 1);
            int toExtract = Math.min(cap.getEnergyStored(), cap.getMaxEnergyStored() * factor);

            cap.extractEnergy(toExtract, false);
        }
    }

    @Override
    public String getDescriptionId() {
        return "effect.fursmp.emp";
    }
}
