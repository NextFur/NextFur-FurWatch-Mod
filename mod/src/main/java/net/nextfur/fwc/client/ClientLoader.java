package net.nextfur.fwc.client;

import net.nextfur.fwc.Config; //Config class to manage client-side settings
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.network.ClientAuthPacket;
import net.nextfur.fwc.network.PacketHandler;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ClientPlayerNetworkEvent;

@Mod.EventBusSubscriber(modid = FwMain.MODID, value = Dist.CLIENT)
public class ClientLoader {
    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        FwMain.LOGGER.info("[FURSMP] Client logging In");

        String token = Config.getClientToken(); // Retrieve the token from the config
        if(token != null && token.isEmpty()) {
            PacketHandler.CHANNEL.sendToServer(new ClientAuthPacket(token));
            FwMain.LOGGER.info("[FURSMP] Authentication token sent to the server for user: " + event.getPlayer().getName().getString());
        } else {
            FwMain.LOGGER.warn("[FURSMP] No authentication token found for user: " + event.getPlayer().getName().getString() + "Aborting authentication.");
        }
    }
}