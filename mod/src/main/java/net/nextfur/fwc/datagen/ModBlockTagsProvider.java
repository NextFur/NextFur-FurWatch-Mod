package net.nextfur.fwc.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.init.FwModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(DataGenerator.PackGenerator packGenerator, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(packGenerator, lookupProvider, FwMain.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(BlockTags.NEEDS_STONE_TOOL).add(FwModBlocks.ANIMATED_SERVER_RACK.get());
        
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(FwModBlocks.BLACK_STEEL_PLATING.get());
        this.tag(BlockTags.NEEDS_STONE_TOOL).add(FwModBlocks.BLACK_STEEL_PLATING.get());
    }
}