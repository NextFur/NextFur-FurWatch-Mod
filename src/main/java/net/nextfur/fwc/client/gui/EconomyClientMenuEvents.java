package net.nextfur.fwc.client.gui;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.init.FwModMenus;

public class EconomyClientMenuEvents {

    public static void register(net.neoforged.bus.api.IEventBus modBus) {
        modBus.addListener(RegisterMenuScreensEvent.class, event -> {
            event.register(FwModMenus.WALLET_MENU.get(), WalletScreen::new);
        });
    }
}
