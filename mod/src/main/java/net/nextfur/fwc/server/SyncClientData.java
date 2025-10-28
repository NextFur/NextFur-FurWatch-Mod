package net.nextfur.fwc.server;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.network.furguard.ModListRequestPacket;
import net.nextfur.fwc.network.nextfur.packets.AuthRequestPacket;
import net.nextfur.fwc.network.world.SkyColorSyncPacket;
import net.nextfur.fwc.util.world.SkyColorData;

@EventBusSubscriber(modid = FwMain.MODID)
public class SyncClientData {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new ModListRequestPacket());
            PacketDistributor.sendToPlayer(player, new SkyColorSyncPacket(SkyColorData.getCurrentFogColor(), SkyColorData.getCurrentBoxColor()));
        }
    }
}
