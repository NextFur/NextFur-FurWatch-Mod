package net.nextfur.fwc.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.blocks.AnimatedServerRackBlock; 

public class FwModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(FwMain.MODID);

    public static final DeferredBlock<Block> ANIMATED_SERVER_RACK = BLOCKS.register("animated_server_rack",

            () -> new AnimatedServerRackBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.ANVIL)
                    .strength(1.0f)
                    .noOcclusion()));
}