package net.nextfur.fwc.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;
import net.nextfur.fwc.blocks.entity.ComputerBlockEntity;

public class FwModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FwMain.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ComputerBlockEntity>> COMPUTER =
            BLOCK_ENTITIES.register("computer", () ->
                    BlockEntityType.Builder.of(ComputerBlockEntity::new, FwModBlocks.COMPUTER.get()).build(null)
            );
}
