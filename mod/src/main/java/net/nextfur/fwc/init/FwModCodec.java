package net.nextfur.fwc.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.blocks.NiixPlushieBlock;
import net.nextfur.fwc.blocks.PoposaPlushieBlock;

import java.util.function.Supplier;

public class FwModCodec {
    public static final DeferredRegister<MapCodec<? extends Block>> REGISTRAR = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, FwMain.MODID);

    public static final Supplier<MapCodec<NiixPlushieBlock>> PLUSHIE_CODEC = REGISTRAR.register("niix_plushie", () -> BlockBehaviour.simpleCodec(NiixPlushieBlock::new));
    public static final Supplier<MapCodec<PoposaPlushieBlock>> POPOSA_CODEC = REGISTRAR.register("poposa_plushie", () -> BlockBehaviour.simpleCodec(PoposaPlushieBlock::new));
}
