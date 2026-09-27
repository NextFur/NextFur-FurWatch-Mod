package net.nextfur.fwc.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.economy.currency.CurrencyUnit;
import net.nextfur.fwc.init.FwModItems;
import net.nextfur.fwc.network.economy.WalletActionC2SPacket;

import java.util.ArrayList;
import java.util.List;

public class CurrencyIconWidget extends AbstractWidget {
    private final CurrencyUnit unit;
    private final ItemStack itemStack;

    public CurrencyIconWidget(int x, int y, CurrencyUnit unit) {
        super(x, y, 18, 18, Component.literal(unit.getLabel()));
        this.unit = unit;
        this.itemStack = getItemForUnit(unit);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));

        long amount;
        if (Screen.hasShiftDown()) {
            amount = unit.getValueInCents() * 64; // Withdraw 64x
        } else {
            amount = unit.getValueInCents(); // Withdraw 1x
        }

        PacketDistributor.sendToServer(new WalletActionC2SPacket(
                WalletActionC2SPacket.ACTION_WITHDRAW_CENTS, amount, ""
        ));
    }

    @Override
    protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();

        // Draw vanilla sunken slot border
        gui.fill(x, y, x + 18, y + 1, 0xFF373737);
        gui.fill(x, y, x + 1, y + 18, 0xFF373737);
        gui.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        gui.fill(x + 17, y + 1, x + 18, y + 18, 0xFFFFFFFF);
        gui.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);

        // Render currency item icon
        gui.renderItem(itemStack, x + 1, y + 1);

        // Slot hover highlight
        if (isHovered()) {
            gui.fill(x + 1, y + 1, x + 17, y + 17, 0x80FFFFFF);

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable(itemStack.getDescriptionId()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            tooltip.add(Component.literal("Valor: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(unit.getLabel()).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                    .append(Component.literal(" (" + unit.getFormattedValue() + ")").withStyle(ChatFormatting.DARK_GRAY)));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("● [Clique Esquerdo] Sacar 1x").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal("● [Shift + Clique] Sacar 1 pack (64x)").withStyle(ChatFormatting.AQUA));

            gui.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    private static ItemStack getItemForUnit(CurrencyUnit u) {
        return switch (u) {
            case COIN_1C -> new ItemStack(FwModItems.COIN_1C.get());
            case COIN_5C -> new ItemStack(FwModItems.COIN_5C.get());
            case COIN_20C -> new ItemStack(FwModItems.COIN_20C.get());
            case COIN_25C -> new ItemStack(FwModItems.COIN_25C.get());
            case COIN_50C -> new ItemStack(FwModItems.COIN_50C.get());
            case COIN_1M -> new ItemStack(FwModItems.COIN_1M.get());
            case COIN_2M -> new ItemStack(FwModItems.COIN_2M.get());
            case BILL_5M -> new ItemStack(FwModItems.BILL_5M.get());
            case BILL_10M -> new ItemStack(FwModItems.BILL_10M.get());
            case BILL_20M -> new ItemStack(FwModItems.BILL_20M.get());
            case BILL_50M -> new ItemStack(FwModItems.BILL_50M.get());
            case BILL_100M -> new ItemStack(FwModItems.BILL_100M.get());
            case BILL_200M -> new ItemStack(FwModItems.BILL_200M.get());
        };
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
