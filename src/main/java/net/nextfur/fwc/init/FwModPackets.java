package net.nextfur.fwc.init;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.nextfur.fwc.client.world.OffRpRenderer;
import net.nextfur.fwc.network.common.RollDiceC2SPacket;
import net.nextfur.fwc.network.common.RollDiceS2CPacket;
import net.nextfur.fwc.network.furguard.ModListPacket;
import net.nextfur.fwc.network.furguard.ModListRequestPacket;
import net.nextfur.fwc.network.gui.OpenDiceRollMenuPacket;
import net.nextfur.fwc.network.gui.OpenSkyColorMenuPacket;
import net.nextfur.fwc.network.gui.OpenTitleMenuPacket;
import net.nextfur.fwc.network.nextfur.packets.AuthRequestPacket;
import net.nextfur.fwc.network.nextfur.packets.AuthResponsePacket;
import net.nextfur.fwc.network.world.OffRpSyncPacket;
import net.nextfur.fwc.network.world.SkyColorChangePacket;
import net.nextfur.fwc.network.world.SkyColorSyncPacket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nextfur.fwc.FwMain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FwModPackets {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(FwMain.MODID).versioned("1.0");

        registrar.playToClient(
                OpenDiceRollMenuPacket.TYPE,
                OpenDiceRollMenuPacket.STREAM_CODEC,
                (packet, ctx) -> OpenDiceRollMenuPacket.handle(packet)
        );

        registrar.playToClient(
                RollDiceS2CPacket.TYPE,
                RollDiceS2CPacket.STREAM_CODEC,
                RollDiceS2CPacket::handle
        );

        registrar.playToServer(
                RollDiceC2SPacket.TYPE,
                RollDiceC2SPacket.STREAM_CODEC,
                (packet, ctx) -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        RollDiceC2SPacket.handle(packet, player);
                    }
                }
        );

        registrar.configurationToClient(
                AuthRequestPacket.TYPE,
                AuthRequestPacket.STREAM_CODEC,
                AuthRequestPacket::handle
        );

        registrar.configurationToServer(
                AuthResponsePacket.TYPE,
                AuthResponsePacket.STREAM_CODEC,
                AuthResponsePacket::handle
        );

        registrar.playToClient(
                OffRpSyncPacket.TYPE,
                OffRpSyncPacket.STREAM_CODEC,
                (packet, ctx) -> {
                    ctx.enqueueWork(() -> {
                        OffRpRenderer.OFFRP_PLAYERS.clear();
                        OffRpRenderer.OFFRP_PLAYERS.addAll(packet.getOffRpPlayers());
                        LOGGER.info("Sincronizando lista contendo "+packet.getOffRpPlayers().size() + " jogadores offrp");
                    });
                }
        );

        registrar.playToClient(
                OpenTitleMenuPacket.TYPE,
                OpenTitleMenuPacket.STREAM_CODEC,
                (packet, ctx) -> OpenTitleMenuPacket.handle(packet)
        );

        registrar.playToClient(
                OpenSkyColorMenuPacket.TYPE,
                OpenSkyColorMenuPacket.STREAM_CODEC,
                (packet, ctx) -> OpenSkyColorMenuPacket.handle(packet)
        );

        // Cliente -> Servidor
        registrar.playToServer(
                SkyColorChangePacket.TYPE,
                SkyColorChangePacket.STREAM_CODEC,
                (packet, ctx) -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        if(!ctx.player().hasPermissions(2)) return;
                        SkyColorChangePacket.handle(packet, player);
                    }
                }
        );

        // Servidor -> Cliente
        registrar.playToClient(
                SkyColorSyncPacket.TYPE,
                SkyColorSyncPacket.STREAM_CODEC,
                (packet, ctx) -> SkyColorSyncPacket.handle(packet)
        );


        registrar.playToClient(
                ModListRequestPacket.TYPE,
                ModListRequestPacket.STREAM_CODEC,
                (packet, ctx) -> ModListRequestPacket.handle(packet)
        );

        registrar.playToServer(
                ModListPacket.TYPE,
                ModListPacket.STREAM_CODEC,
                (packet, ctx) -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        String playerName = packet.getUsername();
                        List<String> modList = packet.getModList();
                        Map<String, String> modFileHashes = packet.getModFileHashes();

                        HashMap<String, Object> data = new HashMap<>();

                        data.put("username", playerName);
                        data.put("ip_address", player.getIpAddress());
                        data.put("modlist", modList);
                        data.put("modFileHashes", modFileHashes);

                        FwMain.FUR_API.postAsync("security", data);
                    }
                }
        );
    }
}