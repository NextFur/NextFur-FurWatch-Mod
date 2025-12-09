package net.nextfur.fwc.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class SimpleBlock extends Block {
    public static final MapCodec<SimpleBlock> CODEC = simpleCodec(SimpleBlock::new);

    public SimpleBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public static SimpleBlock of(MapColor color, SoundType sound, float strength, float resistance) {
        return new SimpleBlock(BlockBehaviour.Properties.of()
                .mapColor(color)
                .sound(sound)
                .strength(strength)
                .explosionResistance(resistance)
        );
    }
}

