package net.nextfur.fwc.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.nextfur.fwc.persona.PersonaData;
import net.nextfur.fwc.persona.PersonaService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {
    protected VillagerMixin(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "updateSpecialPrices", at = @At("TAIL"))
    private void furwatch$applyCharismaTradingModifier(Player player, CallbackInfo ci) {
        if (player == null) return;
        PersonaData data = PersonaService.getPersonaData(player);
        double discountFactor = data.getCharismaVillagerDiscountFactor();
        if (Math.abs(discountFactor) < 1.0E-6D) {
            return;
        }

        for (MerchantOffer offer : this.getOffers()) {
            int baseCount = offer.getBaseCostA().getCount();
            if (discountFactor > 0) {
                int discount = Math.max((int) Math.floor(discountFactor * (double) baseCount), 1);
                offer.addToSpecialPriceDiff(-discount);
            } else {
                int surcharge = Math.max((int) Math.floor(Math.abs(discountFactor) * (double) baseCount), 1);
                offer.addToSpecialPriceDiff(surcharge);
            }
        }
    }
}
