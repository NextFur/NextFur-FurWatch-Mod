package net.nextfur.fwc.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;

import net.nextfur.fwc.blocks.AnimatedServerRackBlock; 
import net.nextfur.fwc.blocks.BlackSteelPlatingBlock; 
import net.nextfur.fwc.blocks.SandedStoneBlock;

public class FwModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(FwMain.MODID);

    public static final DeferredBlock<Block> ANIMATED_SERVER_RACK = BLOCKS.register("animated_server_rack",
        () -> new AnimatedServerRackBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.ANVIL)
                .strength(1.0f)
                .noOcclusion()));

    public static final DeferredBlock<Block> BLACK_STEEL_PLATING = BLOCKS.register("black_steel_plating",
        () -> new BlackSteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

    public static final DeferredBlock<Block> SANDED_STONE_BLOCK = BLOCKS.register("sanded_stone_block",
        () -> new SandedStoneBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

    public static final DeferredBlock<Block> SANDED_DEEPSLATE_BLOCK = BLOCKS.register("sanded_deepslate_block",
        () -> new SandedStoneBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));
    //
}