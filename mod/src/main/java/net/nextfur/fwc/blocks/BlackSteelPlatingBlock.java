package net.nextfur.fwc.blocks;

import net.nextfur.fwc.util.tags.IMineableWithPickaxe;
import net.nextfur.fwc.util.tags.INeedsStoneTool;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlackSteelPlatingBlock extends Block implements IMineableWithPickaxe, INeedsStoneTool {
    public BlackSteelPlatingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return simpleCodec(BlackSteelPlatingBlock::new);
    }
}