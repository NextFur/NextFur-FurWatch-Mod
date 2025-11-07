package net.nextfur.fwc.item;

import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.registries.DeferredHolder;

public class MoonGravityPotionItem extends PotionItem {
    private final DeferredHolder<Potion, Potion> potion;

    public MoonGravityPotionItem(Properties properties, DeferredHolder<Potion, Potion> potion) {
        super(properties);
        this.potion = potion;
    }

    @Override
    public ItemStack getDefaultInstance() {
        return PotionContents.createItemStack(this, this.potion);
    }
}
