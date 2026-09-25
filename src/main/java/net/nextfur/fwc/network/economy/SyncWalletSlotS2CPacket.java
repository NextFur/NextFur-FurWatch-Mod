package net.nextfur.fwc.network.economy;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.client.economy.ClientWalletHolder;

public record SyncWalletSlotS2CPacket(ItemStack wallet) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "sync_wallet_slot");
    public static final Type<SyncWalletSlotS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncWalletSlotS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, packet.wallet),
            buf -> new SyncWalletSlotS2CPacket(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncWalletSlotS2CPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientWalletHolder.setEquippedWallet(packet.wallet()));
    }
}
