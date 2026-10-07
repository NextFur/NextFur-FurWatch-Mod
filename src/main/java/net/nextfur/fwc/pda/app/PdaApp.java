package net.nextfur.fwc.pda.app;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.pda.client.PdaScreen;
import net.nextfur.fwc.pda.data.PdaData;

import javax.annotation.Nullable;

public abstract class PdaApp {
    private final ResourceLocation id;
    private final Component displayName;
    private final Component description;
    private final int themeColor;
    @Nullable
    private final ResourceLocation iconTexture;

    public PdaApp(ResourceLocation id, Component displayName, Component description, int themeColor, @Nullable ResourceLocation iconTexture) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.themeColor = themeColor;
        this.iconTexture = iconTexture;
    }

    public ResourceLocation getId() {
        return id;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public Component getDescription() {
        return description;
    }

    public int getThemeColor() {
        return themeColor;
    }

    @Nullable
    public ResourceLocation getIconTexture() {
        return iconTexture;
    }

    public void renderIcon(GuiGraphics gui, int x, int y, int size, boolean hovered) {
        if (iconTexture != null) {
            gui.blit(iconTexture, x, y, 0, 0, size, size, size, size);
        } else {
            renderProceduralIcon(gui, x, y, size, hovered);
        }
    }

    protected void renderProceduralIcon(GuiGraphics gui, int x, int y, int size, boolean hovered) {
        int color = hovered ? 0xFFFFFFFF : (0xFF000000 | this.themeColor);
        gui.fill(x + 2, y + 2, x + size - 2, y + size - 2, 0x33000000 | (this.themeColor & 0x00FFFFFF));
        gui.renderOutline(x + 1, y + 1, size - 2, size - 2, color);
    }

    public abstract void init(PdaScreen screen, int contentX, int contentY, int contentWidth, int contentHeight);

    public abstract void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int contentX, int contentY, int contentWidth, int contentHeight);

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    public void tick() {
    }

    public void onClose() {
    }

    public void onDataUpdated(PdaData data) {
    }
}
