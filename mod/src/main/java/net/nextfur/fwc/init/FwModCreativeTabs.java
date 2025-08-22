package net.nextfur.fwc.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;

public class FwModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FwMain.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FWC_TAB = CREATIVE_MODE_TABS.register("fursmp_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(FwModItems.ANIMATED_SERVER_RACK_ITEM.get()))
                    .title(Component.translatable("creativetab.fursmp_tab"))
                    .displayItems((displayParameters, output) -> {
                        
                        output.accept(FwModItems.ANIMATED_SERVER_RACK_ITEM.get());
                    })
                    .build());
}