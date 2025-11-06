package net.nextfur.fwc.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.init.FwModBlocks;
import net.nextfur.fwc.util.tags.IMineableWithPickaxe;
import net.nextfur.fwc.util.tags.INeedsStoneTool;
import net.nextfur.fwc.util.tags.INeedsIronTool;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, FwMain.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (DeferredHolder<Block, ? extends Block> entry : FwModBlocks.BLOCKS.getEntries()) {
            Block block = entry.get();

            if (block instanceof IMineableWithPickaxe) {
                this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
            }
            if (block instanceof INeedsStoneTool) {
                this.tag(BlockTags.NEEDS_STONE_TOOL).add(block);
            }
            if (block instanceof INeedsIronTool) {
                this.tag(BlockTags.NEEDS_IRON_TOOL).add(block);
            }
        }
    }
}