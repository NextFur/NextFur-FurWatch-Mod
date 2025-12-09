package net.nextfur.fwc.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;

public class PlushieBlock extends Block {
    public static final MapCodec<PlushieBlock> CODEC = simpleCodec(PlushieBlock::new);

    public PlushieBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public static PlushieBlock of(SoundType soundType, float destroyTime, float explosionResist) {
        return new PlushieBlock(Properties.of()
                .destroyTime(destroyTime)
                .explosionResistance(explosionResist)
                .sound(soundType)
                .noOcclusion()
                .noCollission()
        );
    }
}
