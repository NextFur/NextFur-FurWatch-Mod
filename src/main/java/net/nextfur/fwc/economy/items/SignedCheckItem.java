package net.nextfur.fwc.economy.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.nextfur.fwc.economy.data.CheckData;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import net.nextfur.fwc.init.FwDataComponents;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SignedCheckItem extends Item {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public SignedCheckItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CheckData data = stack.get(FwDataComponents.CHECK_DATA.get());
        if (data != null) {
            tooltipComponents.add(Component.literal("Cheque Bancário Oficial").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            tooltipComponents.add(Component.literal("Valor: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(EconomyFormatHelper.formatFull(data.amountCents())).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
            tooltipComponents.add(Component.literal("Emitente: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.issuerName()).withStyle(ChatFormatting.WHITE)));
            tooltipComponents.add(Component.literal("Favorecido: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(data.payee().isBlank() ? "Portador" : data.payee()).withStyle(ChatFormatting.WHITE)));

            String dateStr = DATE_FORMAT.format(new Date(data.timestamp()));
            tooltipComponents.add(Component.literal("Data: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(dateStr).withStyle(ChatFormatting.DARK_GRAY)));

            String shortSerial = data.checkId().toString().substring(0, 8);
            tooltipComponents.add(Component.literal("Serial: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("#" + shortSerial).withStyle(ChatFormatting.DARK_GRAY)));

            if (data.deposited()) {
                tooltipComponents.add(Component.literal("● COMPENSADO / JÁ DEPOSITADO").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            } else {
                tooltipComponents.add(Component.literal("● VÁLIDO PARA DEPÓSITO").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            }
        } else {
            tooltipComponents.add(Component.literal("Cheque sem dados válidos").withStyle(ChatFormatting.RED));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
