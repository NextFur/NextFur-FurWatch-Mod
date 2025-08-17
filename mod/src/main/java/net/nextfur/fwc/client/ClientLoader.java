package net.nextfur.fwc.client;

import net.nextfur.fwc.Config;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.network.ClientAuthPacket; 
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = FwMain.MODID, value = Dist.CLIENT)
public class ClientLoader {
    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        FwMain.LOGGER.info("[FURSMP] Connecting to server. Preparing to send auth token.");
        String token = Config.getAuthToken();

        if (token != null && !token.isEmpty()) {
            PacketDistributor.sendToServer(new ClientAuthPacket(token));
            FwMain.LOGGER.info("[FURSMP] Authentication token sent to server.");
        } else {
            FwMain.LOGGER.warn("[FURSMP] No authentication token found in config. Cannot authenticate.");
        }
    }
}