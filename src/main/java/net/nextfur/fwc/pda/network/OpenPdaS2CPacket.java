package net.nextfur.fwc.pda.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.pda.client.PdaClientHelper;

public record OpenPdaS2CPacket(ItemStack stack, boolean isMainHand) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "open_pda");
    public static final Type<OpenPdaS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPdaS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, OpenPdaS2CPacket::stack,
            ByteBufCodecs.BOOL, OpenPdaS2CPacket::isMainHand,
            OpenPdaS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenPdaS2CPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> PdaClientHelper.openPdaScreen(packet.stack(), packet.isMainHand()));
    }
}
