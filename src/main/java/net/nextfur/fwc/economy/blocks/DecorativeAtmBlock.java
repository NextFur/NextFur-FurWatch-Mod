package net.nextfur.fwc.economy.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DecorativeAtmBlock extends AbstractAtmBlock {
    public static final MapCodec<DecorativeAtmBlock> CODEC = simpleCodec(DecorativeAtmBlock::new);

    public DecorativeAtmBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DecorativeAtmBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.6f, 1.0f, false);
            player.displayClientMessage(
                    Component.literal("§8[§bATM§8] §cFora de serviço. -w-\"")
                            .withStyle(ChatFormatting.RED),
                    true
            );
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static DecorativeAtmBlock create() {
        return new DecorativeAtmBlock(createProperties());
    }
}
