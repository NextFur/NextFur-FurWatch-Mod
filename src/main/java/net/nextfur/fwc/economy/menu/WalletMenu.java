package net.nextfur.fwc.economy.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nextfur.fwc.economy.data.WalletData;
import net.nextfur.fwc.economy.items.CurrencyItem;
import net.nextfur.fwc.economy.items.SignedCheckItem;
import net.nextfur.fwc.init.FwAttachments;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.init.FwModMenus;

public class WalletMenu extends AbstractContainerMenu {
    private final Container depositContainer = new SimpleContainer(1);
    private final Player player;
    private final boolean isEquippedSlot;
    private ItemStack walletStack;

    // Client-side constructor
    public WalletMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        super(FwModMenus.WALLET_MENU.get(), containerId);
        this.player = playerInventory.player;
        this.isEquippedSlot = data.readBoolean();
        this.walletStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(data);

        initSlots(playerInventory);
    }

    // Server-side constructor
    public WalletMenu(int containerId, Inventory playerInventory, ItemStack walletStack, boolean isEquippedSlot) {
        super(FwModMenus.WALLET_MENU.get(), containerId);
        this.player = playerInventory.player;
        this.walletStack = walletStack;
        this.isEquippedSlot = isEquippedSlot;

        initSlots(playerInventory);
    }

    private void initSlots(Inventory playerInventory) {
        // Deposit slot at index 0 (x=24, y=42)
        this.addSlot(new Slot(depositContainer, 0, 24, 42) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof CurrencyItem || stack.getItem() instanceof SignedCheckItem;
            }
        });

        // Player main inventory (3 rows x 9 columns)
        int startY = 142;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, startY + row * 18));
            }
        }

        // Player hotbar (1 row x 9 columns)
        int hotbarY = startY + 58;
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    public ItemStack getWalletStack() {
        if (isEquippedSlot && !player.level().isClientSide) {
            return player.getData(FwAttachments.WALLET_SLOT.get());
        }
        return walletStack;
    }

    public void setWalletStack(ItemStack stack) {
        this.walletStack = stack;
        if (isEquippedSlot && !player.level().isClientSide) {
            player.setData(FwAttachments.WALLET_SLOT.get(), stack);
        }
    }

    public WalletData getWalletData() {
        ItemStack stack = getWalletStack();
        return stack.get(FwDataComponents.WALLET_DATA.get());
    }

    public Container getDepositContainer() {
        return depositContainer;
    }

    public boolean isEquippedSlot() {
        return isEquippedSlot;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.depositContainer);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index == 0) {
                // Moving from deposit slot to player inventory
                if (!this.moveItemStackTo(slotStack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to deposit slot
                if (slotStack.getItem() instanceof CurrencyItem || slotStack.getItem() instanceof SignedCheckItem) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        if (isEquippedSlot) {
            return !player.getData(FwAttachments.WALLET_SLOT.get()).isEmpty();
        } else {
            return player.getMainHandItem() == walletStack || player.getOffhandItem() == walletStack;
        }
    }
}
