package net.nextfur.fwc.pda.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.nextfur.fwc.init.FwDataComponents;
import net.nextfur.fwc.pda.app.PdaApp;
import net.nextfur.fwc.pda.app.PdaAppRegistry;
import net.nextfur.fwc.pda.app.impl.*;
import net.nextfur.fwc.pda.data.PdaData;
import net.nextfur.fwc.pda.items.PdaItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PdaScreen extends Screen {
    public static boolean showCrackedOverlay = false;
    public static boolean showVignetteOverlay = true;
    public static boolean flashlightActive = false;

    private static final int FRAME_WIDTH = 360;
    private static final int FRAME_HEIGHT = 218;

    private final ItemStack pdaStack;
    private final boolean isMainHand;
    private PdaData pdaData;

    private int leftPos;
    private int topPos;

    private PdaApp activeApp = null;
    private final List<AbstractWidget> appWidgets = new ArrayList<>();

    public record PdaTab(Component label, @Nullable PdaApp app, boolean isClose) {}

    public PdaScreen(ItemStack stack, boolean isMainHand) {
        super(Component.literal("FurWatch CyberTab PDA"));
        this.pdaStack = stack;
        this.isMainHand = isMainHand;
        this.pdaData = stack.get(FwDataComponents.PDA_DATA.get());
        if (this.pdaData == null && stack.getItem() instanceof PdaItem pdaItem) {
            this.pdaData = PdaData.createNew(pdaItem.getColorVariant());
        }
    }

    public PdaData getPdaData() {
        return pdaData;
    }

    public boolean isMainHand() {
        return isMainHand;
    }

    public int getScreenX() {
        return leftPos + 35;
    }

    public int getScreenY() {
        return topPos + 24;
    }

    public int getScreenWidth() {
        return 274;
    }

    public int getScreenHeight() {
        return 168;
    }

    public int getContentX() {
        return getScreenX() + 4;
    }

    public int getContentY() {
        return getScreenY() + 33;
    }

    public int getContentWidth() {
        return getScreenWidth() - 8;
    }

    public int getContentHeight() {
        return getScreenHeight() - 37;
    }

    public void updatePdaData(PdaData newData) {
        this.pdaData = newData;
        if (activeApp != null) {
            activeApp.onDataUpdated(newData);
        }
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - FRAME_WIDTH) / 2;
        this.topPos = (this.height - FRAME_HEIGHT) / 2;

        clearWidgets();
        clearAppWidgets();

        // Default to Messages app if none currently selected
        if (activeApp == null) {
            PdaApp defaultApp = PdaAppRegistry.get(MessagesApp.ID);
            if (defaultApp == null && !PdaAppRegistry.getApps().isEmpty()) {
                defaultApp = PdaAppRegistry.getApps().get(0);
            }
            if (defaultApp != null) {
                openApp(defaultApp);
            }
        } else {
            activeApp.init(this, getContentX(), getContentY(), getContentWidth(), getContentHeight());
        }
    }

    public void addAppWidget(AbstractWidget widget) {
        this.appWidgets.add(widget);
        this.addRenderableWidget(widget);
    }

    public void openApp(PdaApp app) {
        if (this.activeApp != null) {
            this.activeApp.onClose();
        }
        this.activeApp = app;

        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.35f, 1.8f);
        }

        clearAppWidgets();
        app.init(this, getContentX(), getContentY(), getContentWidth(), getContentHeight());
    }

    private void clearAppWidgets() {
        for (AbstractWidget widget : appWidgets) {
            this.removeWidget(widget);
        }
        appWidgets.clear();
    }

    public List<PdaTab> getTabs() {
        List<PdaTab> tabs = new ArrayList<>();
        PdaApp msg = PdaAppRegistry.get(MessagesApp.ID);
        PdaApp notes = PdaAppRegistry.get(NotepadApp.ID);
        PdaApp bank = PdaAppRegistry.get(ShieldBankApp.ID);
        PdaApp video = PdaAppRegistry.get(VideoPlayerApp.ID);
        PdaApp settings = PdaAppRegistry.get(SettingsApp.ID);

        if (msg != null) tabs.add(new PdaTab(Component.literal("MENSAGENS"), msg, false));
        if (notes != null) tabs.add(new PdaTab(Component.literal("ANOTAÇÕES"), notes, false));
        if (bank != null) tabs.add(new PdaTab(Component.literal("SHIELD BANK"), bank, false));
        if (video != null) tabs.add(new PdaTab(Component.literal("VIDEOPLAYER"), video, false));
        if (settings != null) tabs.add(new PdaTab(Component.literal("CONFIGURAÇÕES"), settings, false));

        // Dynamically registered apps
        for (PdaApp app : PdaAppRegistry.getApps()) {
            if (!app.getId().equals(MessagesApp.ID) &&
                    !app.getId().equals(NotepadApp.ID) &&
                    !app.getId().equals(ShieldBankApp.ID) &&
                    !app.getId().equals(VideoPlayerApp.ID) &&
                    !app.getId().equals(SettingsApp.ID)) {
                tabs.add(new PdaTab(app.getDisplayName(), app, false));
            }
        }

        tabs.add(new PdaTab(Component.literal("✕ SAIR"), null, true));
        return tabs;
    }

    @Override
    public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        // Dark transparent gradient without Minecraft's blur shader
        gui.fillGradient(0, 0, this.width, this.height, 0x88000000, 0xAA000000);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);

        // 1. Draw rugged hardware casing (0.png)
        PdaTheme.drawHardwareFrame(gui, leftPos, topPos, FRAME_WIDTH, FRAME_HEIGHT);

        int screenX = getScreenX();
        int screenY = getScreenY();
        int screenWidth = getScreenWidth();
        int screenHeight = getScreenHeight();

        // 2. Base CRT screen background
        gui.fill(screenX, screenY, screenX + screenWidth, screenY + screenHeight, PdaTheme.BG_SCREEN);

        // 3. Render Top Bar (User info box + Flashlight toggle box + Sci-Fi tabs)
        renderTopBar(gui, mouseX, mouseY, screenX, screenY);

        // 4. Render Workspace Panel frame
        int workspaceX = screenX + 2;
        int workspaceY = screenY + 31;
        int workspaceWidth = screenWidth - 4;
        int workspaceHeight = screenHeight - 33;
        int accentColor = (activeApp != null) ? activeApp.getThemeColor() : 0xFF00E5FF;
        PdaTheme.drawWorkspacePanel(gui, workspaceX, workspaceY, workspaceWidth, workspaceHeight, accentColor);

        // 5. Render Active App contents
        if (activeApp != null) {
            activeApp.render(gui, mouseX, mouseY, partialTick, getContentX(), getContentY(), getContentWidth(), getContentHeight());
        }

        // 6. Render widgets (inputs, buttons)
        for (net.minecraft.client.gui.components.Renderable renderable : this.renderables) {
            renderable.render(gui, mouseX, mouseY, partialTick);
        }

        // 7. Render Screen Glass Overlays on top of the screen contents
        if (showVignetteOverlay) {
            PdaTheme.drawScreenVignette(gui, screenX, screenY, screenWidth, screenHeight);
        }
        PdaTheme.drawScreenSmudge(gui, screenX, screenY, screenWidth, screenHeight);
        if (showCrackedOverlay) {
            PdaTheme.drawScreenCracks(gui, screenX, screenY, screenWidth, screenHeight);
        }
    }

    private void renderTopBar(GuiGraphics gui, int mouseX, int mouseY, int screenX, int screenY) {
        Font font = this.font;

        // User box
        int userBoxX = screenX + 2;
        int userBoxY = screenY + 2;
        int userBoxW = 86;
        int userBoxH = 13;

        gui.fill(userBoxX, userBoxY, userBoxX + userBoxW, userBoxY + userBoxH, 0x660A141D);
        gui.renderOutline(userBoxX, userBoxY, userBoxW, userBoxH, 0x5500A896);

        String userPrefix = "USUÁRIO: ";
        int prefixW = font.width(userPrefix);
        gui.drawString(font, userPrefix, userBoxX + 3, userBoxY + 3, 0xFF7CA0B5, false);

        String ownerName = (pdaData != null && pdaData.hasOwner()) ? pdaData.ownerName() : "NENHUM";
        int nameColor = (pdaData != null && pdaData.hasOwner()) ? 0xFF00FF88 : 0xFFFFAA00;
        String truncated = font.plainSubstrByWidth(ownerName, userBoxW - prefixW - 5);
        gui.drawString(font, truncated, userBoxX + 3 + prefixW, userBoxY + 3, nameColor, false);

        // Flashlight box
        int flashX = screenX + 2;
        int flashY = screenY + 16;
        int flashW = 86;
        int flashH = 13;
        boolean flashHovered = mouseX >= flashX && mouseX <= flashX + flashW && mouseY >= flashY && mouseY <= flashY + flashH;

        gui.fill(flashX, flashY, flashX + flashW, flashY + flashH, flashlightActive ? 0x99003542 : (flashHovered ? 0x880E1C29 : 0x660A141D));
        gui.renderOutline(flashX, flashY, flashW, flashH, flashlightActive ? 0xFF00FF88 : (flashHovered ? 0xFF00E5FF : 0x5500A896));

        int boxSize = 7;
        int checkX = flashX + 4;
        int checkY = flashY + 3;
        gui.fill(checkX, checkY, checkX + boxSize, checkY + boxSize, flashlightActive ? 0xFF00FF88 : 0x44000000);
        gui.renderOutline(checkX, checkY, boxSize, boxSize, flashlightActive ? 0xFFFFFFFF : 0xFF7CA0B5);
        if (flashlightActive) {
            gui.fill(checkX + 2, checkY + 2, checkX + boxSize - 2, checkY + boxSize - 2, 0xFF003311);
        }

        String flashText = flashlightActive ? "LANTERNA [ON]" : "LANTERNA";
        int flashTextColor = flashlightActive ? 0xFF00FF88 : (flashHovered ? 0xFFE0FFFF : 0xFF7CA0B5);
        gui.drawString(font, flashText, checkX + boxSize + 4, flashY + 3, flashTextColor, false);

        // Top Sci-Fi Tabs
        List<PdaTab> tabs = getTabs();
        int tabsAreaX = screenX + 90;
        int tabsAreaW = 182;
        int tabsPerRow = Math.max(3, (tabs.size() + 1) / 2);
        int tabW = (tabsAreaW - (tabsPerRow - 1) * 2) / tabsPerRow;
        int tabH = 13;

        for (int i = 0; i < tabs.size(); i++) {
            PdaTab tab = tabs.get(i);
            int row = i / tabsPerRow;
            int col = i % tabsPerRow;
            int tx = tabsAreaX + col * (tabW + 2);
            int ty = (screenY + 2) + row * 14;

            boolean isActive = (tab.app != null && tab.app == activeApp);
            boolean isHovered = mouseX >= tx && mouseX <= tx + tabW && mouseY >= ty && mouseY <= ty + tabH;
            int tabAccent = tab.isClose ? 0xFFFF4444 : (tab.app != null ? tab.app.getThemeColor() : 0xFF00E5FF);

            PdaTheme.drawSciFiTab(gui, font, tab.label, tx, ty, tabW, tabH, isActive, isHovered, tabAccent);
        }
    }

    private void toggleFlashlight() {
        flashlightActive = !flashlightActive;
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, flashlightActive ? 1.4f : 0.8f);
            if (flashlightActive) {
                minecraft.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 6000, 0, false, false, true));
            } else {
                minecraft.player.removeEffect(MobEffects.NIGHT_VISION);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int screenX = getScreenX();
            int screenY = getScreenY();

            // Check Flashlight click
            int flashX = screenX + 2;
            int flashY = screenY + 16;
            int flashW = 86;
            int flashH = 13;
            if (mouseX >= flashX && mouseX <= flashX + flashW && mouseY >= flashY && mouseY <= flashY + flashH) {
                toggleFlashlight();
                return true;
            }

            // Check Tabs click
            List<PdaTab> tabs = getTabs();
            int tabsAreaX = screenX + 90;
            int tabsAreaW = 182;
            int tabsPerRow = Math.max(3, (tabs.size() + 1) / 2);
            int tabW = (tabsAreaW - (tabsPerRow - 1) * 2) / tabsPerRow;
            int tabH = 13;

            for (int i = 0; i < tabs.size(); i++) {
                PdaTab tab = tabs.get(i);
                int row = i / tabsPerRow;
                int col = i % tabsPerRow;
                int tx = tabsAreaX + col * (tabW + 2);
                int ty = (screenY + 2) + row * 14;

                if (mouseX >= tx && mouseX <= tx + tabW && mouseY >= ty && mouseY <= ty + tabH) {
                    if (tab.isClose) {
                        onClose();
                    } else if (tab.app != null) {
                        openApp(tab.app);
                    }
                    return true;
                }
            }
        }

        // Delegate to active app first, then widgets
        if (activeApp != null && activeApp.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (activeApp != null && activeApp.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (activeApp != null && activeApp.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (activeApp != null && activeApp.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (activeApp != null && activeApp.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
        if (activeApp != null) {
            activeApp.tick();
        }
    }

    @Override
    public void onClose() {
        if (activeApp != null) {
            activeApp.onClose();
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
