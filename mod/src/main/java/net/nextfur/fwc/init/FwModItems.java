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

    public static final DeferredItem<Item> POLISHED_BLACK_STEEL_PLATING = ITEMS.register(
        "polished_black_steel_plating",
        () -> new BlockItem(FwModBlocks.POLISHED_BLACK_STEEL_PLATING.get(), new Item.Properties())
    );

    public static final DeferredItem<Item> SANDED_STONE_BLOCK = ITEMS.register(
        "sanded_stone_block",
        () -> new BlockItem(FwModBlocks.SANDED_STONE_BLOCK.get(), new Item.Properties())
    );

    public static final DeferredItem<Item> SANDED_DEEPSLATE_BLOCK = ITEMS.register(
        "sanded_deepslate_block",
        () -> new BlockItem(FwModBlocks.SANDED_DEEPSLATE_BLOCK.get(), new Item.Properties())
    );

    public static final DeferredItem<Item> WHITE_TILES_BLOCK = ITEMS.register(
        "white_tiles_block",
        () -> new BlockItem(FwModBlocks.WHITE_TILES_BLOCK.get(), new Item.Properties())
    );

    public static final DeferredItem<Item> RED_TILES_BLOCK = ITEMS.register(
        "red_tiles_block",
        () -> new BlockItem(FwModBlocks.RED_TILES_BLOCK.get(), new Item.Properties())
    );

    public static final DeferredItem<Item> WHITE_DIRTY_TILES_BLOCK = ITEMS.register(
        "white_dirty_tiles_block",
        () -> new BlockItem(FwModBlocks.WHITE_DIRTY_TILES_BLOCK.get(), new Item.Properties())
    );

    public static final DeferredItem<Item> MOON_GRAVITY_POTION = ITEMS.register(
        "moon_gravity_potion",
        () -> new net.nextfur.fwc.item.MoonGravityPotionItem(new Item.Properties(), net.nextfur.fwc.init.FwModPotions.MOON_GRAVITY_POTION)
    );
}