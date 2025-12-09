package net.nextfur.fwc.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.blocks.PlushieBlock;
import net.nextfur.fwc.blocks.SimpleBlock;
import net.nextfur.fwc.blocks.SimpleDirectionalBlock;

public class FwModBlocks {
        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FwMain.MODID);

        // Plushies
        public static final DeferredBlock<Block> NIIX_PLUSHIE = BLOCKS.register("niix_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> POPOSA_PLUSHIE = BLOCKS.register("poposa_plushie", () -> PlushieBlock.of(SoundType.WOOL, 2.0f, 10.0f));
        public static final DeferredBlock<Block> KIVIX_PLUSHIE = BLOCKS.register("kivix_plushie", () -> PlushieBlock.of(SoundType.WOOL, 2.0f, 10.0f));
        public static final DeferredBlock<Block> DEEPPEDRA_PLUSHIE = BLOCKS.register("deeppedra_plushie", () -> PlushieBlock.of(SoundType.DEEPSLATE, 2.0f, 10.0f));

        // Directional Blocks
        public static final DeferredBlock<Block> ANIMATED_SERVER_RACK = BLOCKS.register("animated_server_rack", () -> SimpleDirectionalBlock.of(MapColor.METAL, SoundType.METAL, 1.0f, 4.0f));

        // Steel Plating (Normal)
        public static final DeferredBlock<Block> BLACK_STEEL_PLATING = BLOCKS.register("black_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> GRAY_STEEL_PLATING = BLOCKS.register("gray_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> LIGHT_GRAY_STEEL_PLATING = BLOCKS.register("light_gray_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> BLUE_STEEL_PLATING = BLOCKS.register("blue_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> GREEN_STEEL_PLATING = BLOCKS.register("green_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> RED_STEEL_PLATING = BLOCKS.register("red_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> YELLOW_STEEL_PLATING = BLOCKS.register("yellow_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.STONE, 3.0f, 6.0f));

        // Steel Plating (Polished)
        public static final DeferredBlock<Block> POLISHED_BLACK_STEEL_PLATING = BLOCKS.register("polished_black_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.METAL, 3.0f, 6.0f));
        public static final DeferredBlock<Block> POLISHED_BLUE_STEEL_PLATING = BLOCKS.register("polished_blue_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.METAL, 3.0f, 6.0f));
        public static final DeferredBlock<Block> POLISHED_RED_STEEL_PLATING = BLOCKS.register("polished_red_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.METAL, 3.0f, 6.0f));
        public static final DeferredBlock<Block> POLISHED_GREEN_STEEL_PLATING = BLOCKS.register("polished_green_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.METAL, 3.0f, 6.0f));
        public static final DeferredBlock<Block> POLISHED_LIGHT_GRAY_STEEL_PLATING = BLOCKS.register("polished_light_gray_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.METAL, 3.0f, 6.0f));
        public static final DeferredBlock<Block> POLISHED_YELLOW_STEEL_PLATING = BLOCKS.register("polished_yellow_steel_plating", () -> SimpleBlock.of(MapColor.METAL, SoundType.METAL, 3.0f, 6.0f));

        // Stone & Construction
        public static final DeferredBlock<Block> SANDED_STONE_BLOCK = BLOCKS.register("sanded_stone_block", () -> SimpleBlock.of(MapColor.STONE, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> SANDED_DEEPSLATE_BLOCK = BLOCKS.register("sanded_deepslate_block", () -> SimpleBlock.of(MapColor.STONE, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> LIGHT_STONE_BRICKS = BLOCKS.register("light_stone_bricks", () -> SimpleBlock.of(MapColor.STONE, SoundType.STONE, 3.0f, 6.0f));

        // Tiles
        public static final DeferredBlock<Block> WHITE_TILES_BLOCK = BLOCKS.register("white_tiles_block", () -> SimpleBlock.of(MapColor.SNOW, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> RED_TILES_BLOCK = BLOCKS.register("red_tiles_block", () -> SimpleBlock.of(MapColor.STONE, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> WHITE_DIRTY_TILES_BLOCK = BLOCKS.register("white_dirty_tiles_block", () -> SimpleBlock.of(MapColor.STONE, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> SMALLER_WHITE_TILES_BLOCK = BLOCKS.register("smaller_white_tiles_block", () -> SimpleBlock.of(MapColor.SNOW, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> SMALLER_WHITE_TILES_DIRTY_BLOCK = BLOCKS.register("smaller_white_tiles_dirty_block", () -> SimpleBlock.of(MapColor.SNOW, SoundType.STONE, 3.0f, 6.0f));
        public static final DeferredBlock<Block> SMALLER_WHITE_TILES_REALLY_DIRTY_BLOCK = BLOCKS.register("smaller_white_tiles_really_dirty_block", () -> SimpleBlock.of(MapColor.SNOW, SoundType.STONE, 3.0f, 6.0f));
}