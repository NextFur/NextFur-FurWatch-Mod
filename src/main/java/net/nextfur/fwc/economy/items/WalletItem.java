package net.nextfur.fwc.economy.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import net.nextfur.fwc.economy.data.WalletData;
import net.nextfur.fwc.init.FwDataComponents;

import java.util.List;

public class WalletItem extends Item {
    public WalletItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static WalletData getOrCreateWalletData(ItemStack stack, Player player) {
        WalletData data = stack.get(FwDataComponents.WALLET_DATA.get());
        if (data == null) {
            data = WalletData.createNew(player.getUUID(), player.getName().getString());
            stack.set(FwDataComponents.WALLET_DATA.get(), data);
        }
        return data;
    }

    public static void openWalletMenu(ServerPlayer player, ItemStack stack, boolean isEquippedSlot) {
        getOrCreateWalletData(stack, player);
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (id, inv, p) -> new net.nextfur.fwc.economy.menu.WalletMenu(id, inv, stack, isEquippedSlot),
                Component.literal("Carteira FurSMP")
        ), buf -> {
            buf.writeBoolean(isEquippedSlot);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            openWalletMenu(serverPlayer, stack, false);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        WalletData data = stack.get(FwDataComponents.WALLET_DATA.get());
        tooltipComponents.add(Component.literal("Carteira FurSMP").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        if (data != null) {
            tooltipComponents.add(Component.literal("Titular: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.ownerName()).withStyle(ChatFormatting.WHITE)));
            tooltipComponents.add(Component.literal("Saldo: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(EconomyFormatHelper.formatFull(data.balanceCents())).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
            tooltipComponents.add(Component.literal("ID: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("#" + data.walletId().toString().substring(0, 8)).withStyle(ChatFormatting.DARK_GRAY)));
        } else {
            tooltipComponents.add(Component.literal("Titular: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("Não vinculado").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
            tooltipComponents.add(Component.literal("Saldo: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(EconomyFormatHelper.formatFull(0L)).withStyle(ChatFormatting.DARK_GRAY)));
        }

        tooltipComponents.add(Component.literal("Clique com o Botão Direito ou equipe no slot dedicado.")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
