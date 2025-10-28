package net.nextfur.fwc.init;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.nextfur.fwc.api.WebhookManager;
import net.nextfur.fwc.network.furguard.ModListPacket;
import net.nextfur.fwc.network.furguard.ModListRequestPacket;
import net.nextfur.fwc.network.gui.OpenGamerulesMenuPacket;
import net.nextfur.fwc.network.gui.OpenSkyColorMenuPacket;
import net.nextfur.fwc.network.gui.OpenTitleMenuPacket;
import net.nextfur.fwc.network.nextfur.packets.AuthRequestPacket;
import net.nextfur.fwc.network.nextfur.packets.AuthResponsePacket;
import net.nextfur.fwc.network.world.SkyColorChangePacket;
import net.nextfur.fwc.network.world.SkyColorSyncPacket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nextfur.fwc.FwMain;

import java.util.List;

public class FwModPackets {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(FwMain.MODID).versioned("1.0");

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
                OpenGamerulesMenuPacket.TYPE,
                OpenGamerulesMenuPacket.STREAM_CODEC,
                (packet, ctx) -> OpenGamerulesMenuPacket.handle(packet)
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

                        WebhookManager.postWebhook(playerName, modList);
                    }
                }
        );
    }
}