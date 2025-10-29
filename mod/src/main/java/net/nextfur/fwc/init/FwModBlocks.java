package net.nextfur.fwc.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;

import net.nextfur.fwc.blocks.*;
import net.nextfur.fwc.blocks.plushies.KivixPlushieBlock;
import net.nextfur.fwc.blocks.plushies.NiixPlushieBlock;
import net.nextfur.fwc.blocks.plushies.PoposaPlushieBlock;

import java.util.function.Supplier;

public class FwModBlocks {
        public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(FwMain.MODID);

        public static final Supplier<Block> NIIX_PLUSHIE = BLOCKS.register("niix_plushie", rname ->
                new NiixPlushieBlock(BlockBehaviour.Properties.of()
                        .destroyTime(2.0f)
                        .explosionResistance(10.0f)
                        .sound(SoundType.SLIME_BLOCK)
                        .noOcclusion()
                        .noCollission()
                )
        );

        public static final Supplier<Block> POPOSA_PLUSHIE = BLOCKS.register("poposa_plushie", rname ->
                new PoposaPlushieBlock(BlockBehaviour.Properties.of()
                        .destroyTime(2.0f)
                        .explosionResistance(10.0f)
                        .sound(SoundType.WOOL)
                        .noOcclusion()
                        .noCollission()
                )
        );

        public static final Supplier<Block> KIVIX_PLUSHIE = BLOCKS.register("kivix_plushie", rname ->
                new KivixPlushieBlock(BlockBehaviour.Properties.of()
                        .destroyTime(2.0f)
                        .explosionResistance(10.0f)
                        .sound(SoundType.WOOL)
                        .noOcclusion()
                        .noCollission()
                )
        );

        public static final DeferredBlock<Block> ANIMATED_SERVER_RACK = BLOCKS.register("animated_server_rack",
        () -> new AnimatedServerRackBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.ANVIL)
                .strength(1.0f)
                .noOcclusion()));

        public static final DeferredBlock<Block> BLACK_STEEL_PLATING = BLOCKS.register("black_steel_plating",
        () -> new BlackSteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> GRAY_STEEL_PLATING = BLOCKS.register("gray_steel_plating",
        () -> new GraySteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> LIGHT_GRAY_STEEL_PLATING = BLOCKS.register("light_gray_steel_plating",
        () -> new LightGraySteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> BLUE_STEEL_PLATING = BLOCKS.register("blue_steel_plating",
        () -> new BlueSteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> GREEN_STEEL_PLATING = BLOCKS.register("green_steel_plating",
        () -> new GreenSteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> RED_STEEL_PLATING = BLOCKS.register("red_steel_plating",
        () -> new RedSteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> YELLOW_STEEL_PLATING = BLOCKS.register("yellow_steel_plating",
        () -> new YellowSteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> SANDED_STONE_BLOCK = BLOCKS.register("sanded_stone_block",
        () -> new SandedStoneBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> SANDED_DEEPSLATE_BLOCK = BLOCKS.register("sanded_deepslate_block",
        () -> new SandedDeepslateBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> POLISHED_BLACK_STEEL_PLATING = BLOCKS.register("polished_black_steel_plating",
        () -> new PolishedBlackSteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> POLISHED_BLUE_STEEL_PLATING = BLOCKS.register("polished_blue_steel_plating",
        () -> new net.nextfur.fwc.blocks.PolishedBlueSteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> POLISHED_RED_STEEL_PLATING = BLOCKS.register("polished_red_steel_plating",
        () -> new net.nextfur.fwc.blocks.PolishedRedSteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> POLISHED_GREEN_STEEL_PLATING = BLOCKS.register("polished_green_steel_plating",
        () -> new net.nextfur.fwc.blocks.PolishedGreenSteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> POLISHED_LIGHT_GRAY_STEEL_PLATING = BLOCKS.register("polished_light_gray_steel_plating",
        () -> new net.nextfur.fwc.blocks.PolishedLightGraySteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> POLISHED_YELLOW_STEEL_PLATING = BLOCKS.register("polished_yellow_steel_plating",
        () -> new net.nextfur.fwc.blocks.PolishedYellowSteelPlatingBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> LIGHT_STONE_BRICKS = BLOCKS.register("light_stone_bricks",
        () -> new LightStoneBricksBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));
    
        public static final DeferredBlock<Block> WHITE_TILES_BLOCK = BLOCKS.register("white_tiles_block",
        () -> new WhiteTilesBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> SMALLER_WHITE_TILES_BLOCK = BLOCKS.register("smaller_white_tiles_block",
        () -> new SmallerWhiteTiles(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> SMALLER_WHITE_TILES_DIRTY_BLOCK = BLOCKS.register("smaller_white_tiles_dirty_block",
        () -> new SmallerWhiteTilesDirty(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));

        public static final DeferredBlock<Block> SMALLER_WHITE_TILES_REALLY_DIRTY_BLOCK = BLOCKS.register("smaller_white_tiles_really_dirty_block",
        () -> new SmallerWhiteTilesReallyDirty(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));
    
        public static final DeferredBlock<Block> RED_TILES_BLOCK = BLOCKS.register("red_tiles_block",
        () -> new RedTilesBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));
    
        public static final DeferredBlock<Block> WHITE_DIRTY_TILES_BLOCK = BLOCKS.register("white_dirty_tiles_block",
        () -> new WhiteDirtyTilesBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .sound(SoundType.STONE)
                .strength(3.0f, 6.0f) 
                .noOcclusion()));
}