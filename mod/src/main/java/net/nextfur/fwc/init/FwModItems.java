package net.nextfur.fwc.init;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;

public class FwModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(FwMain.MODID);

    public static final DeferredItem<Item> ANIMATED_SERVER_RACK_ITEM = ITEMS.register(
            "animated_server_rack",
            () -> new BlockItem(FwModBlocks.ANIMATED_SERVER_RACK.get(), new Item.Properties())
    );
}