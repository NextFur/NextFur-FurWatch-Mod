package net.nextfur.fwc.economy.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.nextfur.fwc.economy.currency.CurrencyUnit;

import java.util.List;

public class CurrencyItem extends Item {
    private final CurrencyUnit unit;

    public CurrencyItem(CurrencyUnit unit, Properties properties) {
        super(properties);
        this.unit = unit;
    }

    public CurrencyUnit getUnit() {
        return unit;
    }

    public long getValueInCents() {
        return unit.getValueInCents();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ChatFormatting valColor = unit.getType() == CurrencyUnit.CurrencyType.COIN ? ChatFormatting.GOLD : ChatFormatting.GREEN;
        tooltipComponents.add(Component.literal("Valor: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(unit.getLabel()).withStyle(valColor, ChatFormatting.BOLD))
                .append(Component.literal(" (" + unit.getFormattedValue() + ")").withStyle(ChatFormatting.DARK_GRAY)));

        String typeDesc = unit.getType() == CurrencyUnit.CurrencyType.COIN ? "Moeda Oficial" : "Cédula Oficial";
        tooltipComponents.add(Component.literal(typeDesc + " do FurSMP").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
