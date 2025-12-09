package net.nextfur.fwc.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

public class SimpleDirectionalBlock extends Block {
    public static final MapCodec<SimpleDirectionalBlock> CODEC = simpleCodec(SimpleDirectionalBlock::new);

    public SimpleDirectionalBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
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

    public static SimpleDirectionalBlock of(MapColor color, SoundType sound, float strength, float resistance) {
        return new SimpleDirectionalBlock(BlockBehaviour.Properties.of()
                .mapColor(color)
                .sound(sound)
                .strength(strength)
                .explosionResistance(resistance)
        );
    }
}
