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
            int guiLeft = (screen.width - 176) / 2;
            int guiTop = (screen.height - 166) / 2;

            // Place dedicated wallet slot at (x=77, y=43), directly above the offhand/shield slot
            int slotX = guiLeft + 77;
            int slotY = guiTop + 43;

            event.addListener(new WalletSlotWidget(screen, slotX, slotY));
        }
    }
}
