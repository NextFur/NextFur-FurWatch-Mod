package net.nextfur.fwc.init;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.nextfur.fwc.FwMain;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class FwModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FwMain.MODID);

    public static final DeferredItem<Item> SCRAP_METAL = ITEMS.register(
        "scrap_metal",
        () -> new Item(new Item.Properties())
    );

    public static final DeferredItem<Item> MOON_GRAVITY_POTION = ITEMS.register(
        "moon_gravity_potion",
        () -> new net.minecraft.world.item.PotionItem(new Item.Properties())
    ); 
    
    public static final DeferredItem<Item> MOON_GRAVITY_SPLASH_POTION = ITEMS.register(
        "moon_gravity_splash_potion",
        () -> new net.minecraft.world.item.SplashPotionItem(new Item.Properties())
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