package net.nextfur.fwc.init;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.nextfur.fwc.network.nextfur.ClientAuthPacket;
import net.nextfur.fwc.network.gui.OpenGamerulesMenuPacket;
import net.nextfur.fwc.network.gui.OpenSkyColorMenuPacket;
import net.nextfur.fwc.network.gui.OpenTitleMenuPacket;
import net.nextfur.fwc.network.nextfur.ServerAuthRequestPacket;
import net.nextfur.fwc.network.world.SkyColorChangePacket;
import net.nextfur.fwc.network.world.SkyColorSyncPacket;
import net.nextfur.fwc.server.ServerAuthManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.nextfur.fwc.FwMain;

public class FwModPackets {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(FwMain.MODID);

        registrar.playToClient(
                ServerAuthRequestPacket.TYPE,
                ServerAuthRequestPacket.STREAM_CODEC,
                (packet, ctx) -> ServerAuthRequestPacket.handle(packet)
        );

        registrar.playToServer(
                ClientAuthPacket.TYPE,
                ClientAuthPacket.STREAM_CODEC,
                (packet, ctx) -> {
                    if (ctx.player() instanceof ServerPlayer player) {
                        String playerName = packet.getUsername();
                        String token = packet.getToken();

                        ServerAuthManager.handleAuthResponse(player, token, playerName);
                    }
                }
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
    }
}