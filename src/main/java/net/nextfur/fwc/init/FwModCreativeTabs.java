package net.nextfur.fwc.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;

public class FwModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FwMain.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FWC_TAB = CREATIVE_MODE_TABS.register("fursmp_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(FwModBlocks.ANIMATED_SERVER_RACK.get()))
                    .title(Component.translatable("creative.fursmp_tab"))
                    .displayItems((displayParameters, output) -> {               
                        for (DeferredHolder<Block, ? extends Block> block : FwModBlocks.BLOCKS.getEntries()) {
                            output.accept(block.get());
                        }
                    })
                    .build());

        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FWC_ITEMS_TAB = CREATIVE_MODE_TABS.register("fursmp_items_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(FwModItems.LUXMARK_COIN.get()))
                    .title(Component.translatable("creative.fursmp_items_tab"))
                    .displayItems((displayParameters, output) -> {
                        for (DeferredHolder<Item, ? extends Item> item : FwModItems.ITEMS.getEntries()) {
                            if (!(item.get() instanceof BlockItem)) {
                                output.accept(item.get());
                            }
                        }
                    })
                    .build());
}