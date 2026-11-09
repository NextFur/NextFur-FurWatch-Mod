package net.nextfur.fwc.server;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.commands.OffRpCommand;
import net.nextfur.fwc.network.furguard.ModListRequestPacket;
import net.nextfur.fwc.network.world.OffRpSyncPacket;
import net.nextfur.fwc.network.world.SkyColorSyncPacket;
import net.nextfur.fwc.util.data.SkyColorSavedData;

@EventBusSubscriber(modid = FwMain.MODID)
public class SyncClientData {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            SkyColorSavedData data = SkyColorSavedData.get(player.serverLevel());

            PacketDistributor.sendToPlayer(player, new SkyColorSyncPacket(data.getFogColor(), data.getBoxColor()));
            PacketDistributor.sendToPlayer(player, new OffRpSyncPacket(OffRpCommand.offrp));

            if (player.getServer() != null && player.getServer().isDedicatedServer()) {
                PacketDistributor.sendToPlayer(player, new ModListRequestPacket());
            }
        }
    }
}
