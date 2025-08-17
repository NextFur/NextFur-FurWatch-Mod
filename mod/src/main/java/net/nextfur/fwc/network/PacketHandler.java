package net.nextfur.fwc.network;

import net.nextfur.fwc.FwMain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FwMain.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(
            id++,
            ClientAuthPacket.class,
            ClientAuthPacket::encode,
            ClientAuthPacket::new,
            ClientAuthPacket::handle,
            (packet, context) -> packet.handle(context) 
        );
        FwMain.LOGGER.info("[FURSMP] PacketHandler initialized and ClientAuthPacket registered.");
    }
}