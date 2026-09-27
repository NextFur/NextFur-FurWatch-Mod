package net.nextfur.fwc.economy.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.nextfur.fwc.economy.blocks.CurrencyLayerBlock;
import net.nextfur.fwc.economy.currency.CurrencyUnit;
import net.nextfur.fwc.economy.data.EconomyFormatHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CurrencyLayerBlockItem extends BlockItem {
    private final CurrencyUnit unit;

    public CurrencyLayerBlockItem(CurrencyLayerBlock block, Properties properties) {
        super(block, properties);
        this.unit = block.getUnit();
    }

    public CurrencyUnit getUnit() {
        return unit;
    }

    public long getValueInCents() {
        return unit.getValueInCents() * 9L;
    }

    @Nullable
    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        BlockPos blockpos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.is(this.getBlock())) {
            int i = blockstate.getValue(CurrencyLayerBlock.LAYERS);
            if (i < 8) {
                return context;
            }
        }
        return super.updatePlacementContext(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Camada de Dinheiro (" + unit.getLabel() + ")").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("Contém: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("9x " + unit.getLabel()).withStyle(ChatFormatting.WHITE)));
        tooltipComponents.add(Component.literal("Valor Total: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(EconomyFormatHelper.formatFull(getValueInCents())).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)));
        tooltipComponents.add(Component.literal("Empilhável no chão em até 8 camadas (como neve).").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
        tooltipComponents.add(Component.literal("Pode ser convertida em 9 " + (unit.getType() == CurrencyUnit.CurrencyType.COIN ? "moedas" : "cédulas") + " na bancada.")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        tooltipComponents.add(Component.literal("Shift + Botão Direito (mão vazia) remove 1 camada.").withStyle(ChatFormatting.DARK_GRAY));

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
