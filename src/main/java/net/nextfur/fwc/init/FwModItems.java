package net.nextfur.fwc.init;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;

import java.util.List;

public class FwModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FwMain.MODID);

    public static final DeferredItem<Item> SCRAP_METAL = ITEMS.register(
        "scrap_metal",
        () -> new Item(new Item.Properties())
    );

    public static final DeferredItem<Item> LUXMARK_COIN = ITEMS.register(
            "luxmark_coin",
            () -> new Item(new Item.Properties())
    );

    static {
        FwModBlocks.BLOCKS.getEntries().forEach(block -> {
            String blockName = block.getId().getPath();
            if (blockName.contains("plushie")) {
                ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()) {
                    @Override
                    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
                        tooltipComponents.add(Component.translatable("block.fursmp." + blockName + ".description").withStyle(ChatFormatting.GRAY));
                        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
                    }
                });
            } else {
                ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
            }
        });
    }
}