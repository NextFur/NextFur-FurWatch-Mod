package net.nextfur.fwc.economy.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.nextfur.fwc.economy.currency.CurrencyUnit;
import net.nextfur.fwc.economy.items.CurrencyItem;
import net.nextfur.fwc.init.FwModItems;

import java.util.HashMap;
import java.util.Map;

public class CurrencyExchangeRecipe extends CustomRecipe {
    public static final SimpleCraftingRecipeSerializer<CurrencyExchangeRecipe> SERIALIZER =
            new SimpleCraftingRecipeSerializer<>(CurrencyExchangeRecipe::new);

    private static final Map<Long, ItemStack> TARGET_ITEMS = new HashMap<>();

    static {
        TARGET_ITEMS.put(1L, new ItemStack(FwModItems.COIN_1C.get()));
        TARGET_ITEMS.put(5L, new ItemStack(FwModItems.COIN_5C.get()));
        TARGET_ITEMS.put(20L, new ItemStack(FwModItems.COIN_20C.get()));
        TARGET_ITEMS.put(25L, new ItemStack(FwModItems.COIN_25C.get()));
        TARGET_ITEMS.put(50L, new ItemStack(FwModItems.COIN_50C.get()));
        TARGET_ITEMS.put(100L, new ItemStack(FwModItems.COIN_1M.get()));
        TARGET_ITEMS.put(200L, new ItemStack(FwModItems.COIN_2M.get()));
        TARGET_ITEMS.put(500L, new ItemStack(FwModItems.BILL_5M.get()));
        TARGET_ITEMS.put(1000L, new ItemStack(FwModItems.BILL_10M.get()));
        TARGET_ITEMS.put(2000L, new ItemStack(FwModItems.BILL_20M.get()));
        TARGET_ITEMS.put(5000L, new ItemStack(FwModItems.BILL_50M.get()));
        TARGET_ITEMS.put(10000L, new ItemStack(FwModItems.BILL_100M.get()));
        TARGET_ITEMS.put(20000L, new ItemStack(FwModItems.BILL_200M.get()));
    }

    public CurrencyExchangeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        long totalCents = 0;
        int itemCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof CurrencyItem currencyItem) {
                totalCents += currencyItem.getValueInCents();
                itemCount++;
            } else {
                return false; // Non-currency item present
            }
        }

        // Must be at least 2 items to combine, and the total must equal a target denomination
        return itemCount >= 2 && TARGET_ITEMS.containsKey(totalCents);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        long totalCents = 0;
        int itemCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof CurrencyItem currencyItem) {
                totalCents += currencyItem.getValueInCents();
                itemCount++;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (itemCount >= 2 && TARGET_ITEMS.containsKey(totalCents)) {
            return TARGET_ITEMS.get(totalCents).copy();
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
