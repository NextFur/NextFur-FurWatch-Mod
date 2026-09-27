package net.nextfur.fwc.network.economy;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.economy.items.WalletItem;
import net.nextfur.fwc.init.FwAttachments;

public record OpenWalletMenuC2SPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "open_wallet_menu");
    public static final Type<OpenWalletMenuC2SPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, OpenWalletMenuC2SPacket> STREAM_CODEC = StreamCodec.unit(new OpenWalletMenuC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenWalletMenuC2SPacket packet, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            ctx.enqueueWork(() -> {
                ItemStack slotWallet = player.getData(FwAttachments.WALLET_SLOT.get());
                ItemStack handWallet = player.getMainHandItem();

                if (!slotWallet.isEmpty()) {
                    WalletItem.openWalletMenu(player, slotWallet, true);
                } else if (handWallet.getItem() instanceof WalletItem) {
                    WalletItem.openWalletMenu(player, handWallet, false);
                } else {
                    player.sendSystemMessage(Component.literal("Você não possui uma carteira equipada no slot dedicado nem na mão principal!")
                            .withStyle(ChatFormatting.RED));
                }
            });
        }
    }
}
