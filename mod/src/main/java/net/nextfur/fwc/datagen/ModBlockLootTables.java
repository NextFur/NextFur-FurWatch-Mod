package net.nextfur.fwc.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.nextfur.fwc.init.FwModBlocks; 

import java.util.Set;
import java.util.stream.Collectors;

public class ModBlockLootTables extends BlockLootSubProvider {

    public ModBlockLootTables(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.VANILLA_SET, provider);
    }

    @Override
    protected void generate() {
        // Register Loot Drop
        dropSelf(FwModBlocks.ANIMATED_SERVER_RACK.get());

        dropSelf(FwModBlocks.BLACK_STEEL_PLATING.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return FwModBlocks.BLOCKS.getEntries().stream().map(DeferredHolder::get).collect(Collectors.toList());
    }
}