package net.nextfur.fwc.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.economy.recipe.CurrencyExchangeRecipe;

public class FwModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, FwMain.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CurrencyExchangeRecipe>> CURRENCY_EXCHANGE =
            RECIPE_SERIALIZERS.register("currency_exchange", () -> CurrencyExchangeRecipe.SERIALIZER);
}
