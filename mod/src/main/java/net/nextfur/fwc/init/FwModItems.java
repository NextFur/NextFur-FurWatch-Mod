package net.nextfur.fwc.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;

public class FwModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FwMain.MODID);

    // Register Items
    public static final DeferredItem<Item> SCRAP_METAL = ITEMS.register(
        "scrap_metal",
        () -> new Item(new Item.Properties())
    );

    //Register Blocks 
    public static final DeferredItem<Item> ANIMATED_SERVER_RACK_ITEM = ITEMS.register(
        "animated_server_rack",
        () -> new BlockItem(FwModBlocks.ANIMATED_SERVER_RACK.get(), new Item.Properties())
    );

    public static final DeferredItem<Item> BLACK_STEEL_PLATING_ITEM = ITEMS.register(
        "black_steel_plating",
        () -> new BlockItem(FwModBlocks.BLACK_STEEL_PLATING.get(), new Item.Properties())
    );

    public static final DeferredItem<Item> SANDED_STONE_BLOCK = ITEMS.register(
        "sanded_stone_block",
        () -> new BlockItem(FwModBlocks.SANDED_STONE_BLOCK.get(), new Item.Properties())
    );
}