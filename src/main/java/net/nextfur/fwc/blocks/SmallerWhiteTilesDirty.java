package net.nextfur.fwc.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.nextfur.fwc.util.tags.IMineableWithPickaxe;
import net.nextfur.fwc.util.tags.INeedsStoneTool;

public class SmallerWhiteTilesDirty extends Block implements IMineableWithPickaxe, INeedsStoneTool {

    public SmallerWhiteTilesDirty(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return simpleCodec(SmallerWhiteTilesDirty::new);
    }
}