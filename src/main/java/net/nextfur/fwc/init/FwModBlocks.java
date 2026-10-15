package net.nextfur.fwc.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.blocks.PlushieBlock;
import net.nextfur.fwc.blocks.SimpleBlock;
import net.nextfur.fwc.blocks.SimpleDirectionalBlock;
import net.nextfur.fwc.economy.blocks.CurrencyLayerBlock;
import net.nextfur.fwc.economy.currency.CurrencyUnit;

import java.util.EnumMap;
import java.util.Map;

public class FwModBlocks {
        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FwMain.MODID);

        // Economy Currency Layer Blocks (Stackable 1-8 layers like snow)
        public static final Map<CurrencyUnit, DeferredBlock<CurrencyLayerBlock>> CURRENCY_LAYER_BLOCKS = new EnumMap<>(CurrencyUnit.class);

        static {
            for (CurrencyUnit unit : CurrencyUnit.values()) {
                boolean isCoin = unit.getType() == CurrencyUnit.CurrencyType.COIN;
                CURRENCY_LAYER_BLOCKS.put(unit, BLOCKS.register(
                        unit.getId() + "_layer",
                        () -> new CurrencyLayerBlock(unit, BlockBehaviour.Properties.of()
                                .mapColor(isCoin ? MapColor.COLOR_YELLOW : MapColor.COLOR_GREEN)
                                .sound(isCoin ? SoundType.METAL : SoundType.SNOW)
                                .strength(isCoin ? 0.5f : 0.2f, isCoin ? 1.0f : 0.5f)
                                .noOcclusion())
                ));
            }
        }

        // Plushies
        public static final DeferredBlock<Block> NIIX_PLUSHIE = BLOCKS.register("niix_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> POPOSA_PLUSHIE = BLOCKS.register("poposa_plushie", () -> PlushieBlock.of(SoundType.WOOL, 2.0f, 10.0f));
        public static final DeferredBlock<Block> KIVIX_PLUSHIE = BLOCKS.register("kivix_plushie", () -> PlushieBlock.of(SoundType.WOOL, 2.0f, 10.0f));
        public static final DeferredBlock<Block> KITSU_PLUSHIE = BLOCKS.register("kitsu_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> DEEPPEDRA_PLUSHIE = BLOCKS.register("deeppedra_plushie", () -> PlushieBlock.of(SoundType.DEEPSLATE, 2.0f, 10.0f));
        public static final DeferredBlock<Block> SORA_PLUSHIE = BLOCKS.register("sora_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> TYLER_PLUSHIE = BLOCKS.register("tyler_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> PANDINHO_PLUSHIE = BLOCKS.register("pandinho_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> SIRMOD_PLUSHIE = BLOCKS.register("sirmod_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> MINDY_PLUSHIE = BLOCKS.register("mindy_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> MARI_PLUSHIE = BLOCKS.register("mari_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> CHARLES_PLUSHIE = BLOCKS.register("charles_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> GUI_PLUSHIE = BLOCKS.register("gui_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> FLAAY_PLUSHIE = BLOCKS.register("flaay_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> AMORAS_PLUSHIE = BLOCKS.register("amoras_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> ALEX_PLUSHIE = BLOCKS.register("alex_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> NINGUEM_PLUSHIE = BLOCKS.register("ninguem_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));
        public static final DeferredBlock<Block> JUAN_PLUSHIE = BLOCKS.register("juan_plushie", () -> PlushieBlock.of(SoundType.SLIME_BLOCK, 2.0f, 10.0f));

        // Directional Blocks
        public static final DeferredBlock<Block> ANIMATED_SERVER_RACK = BLOCKS.register("animated_server_rack", () -> SimpleDirectionalBlock.of(MapColor.METAL, SoundType.METAL, 1.0f, 4.0f));

        // Computers
        public static final DeferredBlock<Block> COMPUTER = BLOCKS.register("computer", net.nextfur.fwc.blocks.ComputerBlock::create);
        public static final DeferredBlock<Block> DECORATIVE_COMPUTER = BLOCKS.register("decorative_computer", net.nextfur.fwc.blocks.DecorativeComputerBlock::create);

        // ATMs
        public static final DeferredBlock<Block> ATM = BLOCKS.register("atm", net.nextfur.fwc.economy.blocks.AtmBlock::create);
        public static final DeferredBlock<Block> DECORATIVE_ATM = BLOCKS.register("decorative_atm", net.nextfur.fwc.economy.blocks.DecorativeAtmBlock::create);

        // Lucky Block
        public static final DeferredBlock<Block> LUCKY_BLOCK = BLOCKS.register("lucky_block", net.nextfur.fwc.economy.blocks.LuckyBlock::create);

        // Signal Blocker
        public static final DeferredBlock<Block> SIGNAL_BLOCKER = BLOCKS.register("signal_blocker", net.nextfur.fwc.blocks.SignalBlockerBlock::create);

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