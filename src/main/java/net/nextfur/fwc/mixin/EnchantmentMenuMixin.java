package net.nextfur.fwc.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.nextfur.fwc.persona.PersonaData;
import net.nextfur.fwc.persona.PersonaEvents;
import net.nextfur.fwc.persona.PersonaService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
    @Unique
    private Player furwatch$openingPlayer;

    @Inject(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL")
    )
    private void furwatch$capturePlayer(int containerId, Inventory playerInventory, ContainerLevelAccess access, CallbackInfo ci) {
        if (playerInventory != null) {
            this.furwatch$openingPlayer = playerInventory.player;
        }
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    private void furwatch$onSlotsChangedHead(Container inventory, CallbackInfo ci) {
        if (this.furwatch$openingPlayer != null) {
            PersonaEvents.CURRENT_ENCHANTING_PLAYER.set(this.furwatch$openingPlayer);
        }
    }

    @Inject(method = "slotsChanged", at = @At("RETURN"))
    private void furwatch$onSlotsChangedReturn(Container inventory, CallbackInfo ci) {
        PersonaEvents.CURRENT_ENCHANTING_PLAYER.remove();
    }

    @ModifyVariable(
            method = "getEnchantmentList",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true
    )
    private int furwatch$modifyEnchantmentQualityLevel(int cost) {
        if (this.furwatch$openingPlayer == null || cost <= 0) {
            return cost;
        }
        PersonaData data = PersonaService.getPersonaData(this.furwatch$openingPlayer);
        if (data.wisdom() >= 3) {
            // Restore pre-discount level and add +5 quality affinity bonus
            return Math.min(40, (int) Math.ceil(cost / 0.75f) + 5);
        } else if (data.wisdom() <= -3) {
            // Lower quality for negative Wisdom (-3)
            return Math.max(1, (int) Math.floor(cost * 0.65f));
        }
        return cost;
    }
}
