package net.nextfur.fwc.network.nextfur.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.nextfur.fwc.FwMain;

import java.util.function.Consumer;

public class AuthTaskPayload implements ICustomConfigurationTask {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "auth_task");
    public static final Type TYPE = new Type(ID);

    public static final StreamCodec<FriendlyByteBuf, AuthTaskPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {},
            buffer -> new AuthTaskPayload()
    );

    @Override
    public void run(Consumer<CustomPacketPayload> packetSender) {
        packetSender.accept(new AuthRequestPacket());
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
