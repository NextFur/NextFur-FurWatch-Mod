package net.nextfur.fwc.init;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.nextfur.fwc.FwMain;

import java.util.function.Supplier;

public class FwAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FwMain.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ItemStack>> WALLET_SLOT =
            ATTACHMENT_TYPES.register("wallet_slot", () -> AttachmentType.builder(() -> ItemStack.EMPTY)
                    .serialize(ItemStack.OPTIONAL_CODEC)
                    .build());
}
