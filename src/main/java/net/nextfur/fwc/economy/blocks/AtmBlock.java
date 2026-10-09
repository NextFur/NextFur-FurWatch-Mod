package net.nextfur.fwc.economy.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.nextfur.fwc.economy.menu.AtmMenu;

public class AtmBlock extends AbstractAtmBlock {
    public static final MapCodec<AtmBlock> CODEC = simpleCodec(AtmBlock::new);

    public AtmBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends AtmBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockPos basePos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new AtmMenu(id, inv, ContainerLevelAccess.create(level, basePos)),
                            Component.translatable("container.fursmp.atm")
                    ),
                    basePos
            );
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static AtmBlock create() {
        return new AtmBlock(createProperties());
    }
}
