package net.nextfur.fwc.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;

import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.network.nextfur.packets.AuthTaskPayload;

@EventBusSubscriber(modid = FwMain.MODID)
public class FwModConfigurationTasks {
    @SubscribeEvent
    public static void register(final RegisterConfigurationTasksEvent event) {
        event.register(new AuthTaskPayload());
    }
}