package net.nextfur.fwc.economy.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nextfur.fwc.economy.items.CurrencyItem;
import net.nextfur.fwc.economy.items.CurrencyLayerBlockItem;
import net.nextfur.fwc.economy.items.SignedCheckItem;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.init.FwModBlocks;
import net.nextfur.fwc.init.FwModMenus;
import net.nextfur.fwc.pda.data.PdaData;
import net.nextfur.fwc.pda.items.PdaItem;

public class AtmMenu extends AbstractContainerMenu {
    private final Container atmContainer = new SimpleContainer(2);
    private final ContainerLevelAccess access;
    private final Player player;

    // Client-side constructor
    public AtmMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    // Server-side constructor
    public AtmMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(FwModMenus.ATM_MENU.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;

        initSlots(playerInventory);
    }

    private void initSlots(Inventory playerInventory) {
        // Slot 0: PDA Card Slot (x=24, y=47)
        this.addSlot(new Slot(atmContainer, 0, 24, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof PdaItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Slot 1: Money / Deposit Slot (x=24, y=87)
        this.addSlot(new Slot(atmContainer, 1, 24, 87) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof CurrencyItem
                        || stack.getItem() instanceof SignedCheckItem
                        || stack.getItem() instanceof CurrencyLayerBlockItem;
            }
        });

        // Player main inventory (3 rows x 9 columns) at y=140
        int startY = 140;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, startY + row * 18));
            }
        }

        // Player hotbar (1 row x 9 columns) at y=198
        int hotbarY = 198;
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    public Container getAtmContainer() {
        return atmContainer;
    }

    public ItemStack getPdaStack() {
        return atmContainer.getItem(0);
    }

    public ItemStack getMoneyStack() {
        return atmContainer.getItem(1);
    }

    public PdaData getPdaData() {
        ItemStack pdaStack = getPdaStack();
        if (!pdaStack.isEmpty() && pdaStack.has(FwDataComponents.PDA_DATA.get())) {
            return pdaStack.get(FwDataComponents.PDA_DATA.get());
        }
        return null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            if (index == 0 || index == 1) {
                // Moving from ATM container to player inventory (slots 2..37)
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to ATM
                if (slotStack.getItem() instanceof PdaItem) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotStack.getItem() instanceof CurrencyItem
                        || slotStack.getItem() instanceof SignedCheckItem
                        || slotStack.getItem() instanceof CurrencyLayerBlockItem) {
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) {
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
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.atmContainer);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, FwModBlocks.ATM.get());
    }
}
