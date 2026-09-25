package net.nextfur.fwc.network.economy;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.client.gui.WalletScreen;
import net.nextfur.fwc.economy.data.WalletData;

public record WalletSyncS2CPacket(WalletData walletData) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "wallet_sync");
    public static final Type<WalletSyncS2CPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, WalletSyncS2CPacket> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> WalletData.STREAM_CODEC.encode(buf, val.walletData),
            buf -> new WalletSyncS2CPacket(WalletData.STREAM_CODEC.decode(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WalletSyncS2CPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof WalletScreen screen) {
                screen.updateWalletData(packet.walletData());
            }
        });
    }
}
