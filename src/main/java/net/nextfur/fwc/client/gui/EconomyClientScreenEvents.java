package net.nextfur.fwc.client.gui;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.nextfur.fwc.FwMain;

@EventBusSubscriber(modid = FwMain.MODID, value = Dist.CLIENT)
public class EconomyClientScreenEvents {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen screen) {
            int guiLeft = screen.getGuiLeft();
            int guiTop = screen.getGuiTop();

            // 1. Dedicated wallet equipment slot above the offhand/shield slot
            event.addListener(new WalletSlotWidget(screen, guiLeft + 77, guiTop + 43));

            // 2. Side tab on the left of the UI (only shown when a wallet is equipped)
            event.addListener(new WalletSideTabWidget(screen, guiLeft - 28, guiTop + 24));
        }
    }
}
