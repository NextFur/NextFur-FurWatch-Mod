package net.nextfur.fwc.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;

public class FwModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FwMain.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FWC_TAB = CREATIVE_MODE_TABS.register("fursmp_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(FwModItems.ANIMATED_SERVER_RACK_ITEM.get()))
                    .title(Component.translatable("creative.fursmp_tab"))
                    .displayItems((displayParameters, output) -> {               
                        //Accept Blocks
                        output.accept(FwModItems.ANIMATED_SERVER_RACK_ITEM.get());
                        output.accept(FwModItems.BLACK_STEEL_PLATING_ITEM.get());
                        output.accept(FwModItems.POLISHED_BLACK_STEEL_PLATING.get());
                        output.accept(FwModItems.GRAY_STEEL_PLATING_ITEM.get());
                        output.accept(FwModItems.LIGHT_GRAY_STEEL_PLATING_ITEM.get());
                        output.accept(FwModItems.RED_STEEL_PLATING_ITEM.get());
                        output.accept(FwModItems.BLUE_STEEL_PLATING_ITEM.get());
                        output.accept(FwModItems.GREEN_STEEL_PLATING_ITEM.get());
                        output.accept(FwModItems.YELLOW_STEEL_PLATING_ITEM.get());
                        
                        output.accept(FwModItems.LIGHT_STONE_BRICKS_ITEM.get());
                        output.accept(FwModItems.SANDED_STONE_BLOCK.get());
                        output.accept(FwModBlocks.SANDED_DEEPSLATE_BLOCK.get());
                        output.accept(FwModItems.WHITE_TILES_BLOCK.get());
                        output.accept(FwModItems.WHITE_DIRTY_TILES_BLOCK.get());
                        output.accept(FwModItems.RED_TILES_BLOCK.get());
                    })
                    .build());

        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FWC_ITEMS_TAB = CREATIVE_MODE_TABS.register("fursmp_items_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(FwModItems.SCRAP_METAL.get()))
                    .title(Component.translatable("creative.fursmp_items_tab"))
                    .displayItems((displayParameters, output) -> {
                        //Accept Items
                        output.accept(FwModItems.SCRAP_METAL.get());
                        output.accept(PotionContents.createItemStack(FwModItems.MOON_GRAVITY_POTION.get(), FwModPotions.MOON_GRAVITY_POTION));
                        output.accept(PotionContents.createItemStack(FwModItems.MOON_GRAVITY_SPLASH_POTION.get(), FwModPotions.MOON_GRAVITY_POTION));
                    })
                    .build());
}