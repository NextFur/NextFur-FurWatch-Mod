package net.nextfur.fwc.client.gui;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.client.gui.browser.BrowserManager;
import net.nextfur.fwc.client.gui.browser.MCEFWrapper;
import net.nextfur.fwc.network.computer.UpdateComputerUrlC2SPacket;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

public class ComputerScreen extends Screen {
    @Nullable
    private final BlockPos pos;
    private String currentUrl;
    private final boolean isCreative;

    private int viewportX;
    private int viewportY;
    private int viewportWidth;
    private int viewportHeight;

    @Nullable
    private EditBox urlEditBox;
    @Nullable
    private MCEFWrapper browser;

    private String statusNotification = "";
    private long statusNotificationTime = 0;
    private long openTimestamp = 0;

    public ComputerScreen(@Nullable BlockPos pos, String initialUrl, boolean isCreative) {
        super(Component.literal("Computador FurWatch"));
        this.pos = pos;
        this.currentUrl = (initialUrl == null || initialUrl.trim().isEmpty()) ? "https://fursmp.com" : initialUrl.trim();
        this.isCreative = isCreative;
    }

    @Override
    protected void init() {
        super.init();
        this.openTimestamp = System.currentTimeMillis();

        viewportX = 8;
        viewportY = 32;
        viewportWidth = Math.max(width - 16, 50);
        viewportHeight = Math.max(height - 48, 50);

        int currentBtnX = 8;
        int topBarY = 5;

        // Navigation Buttons
        // Back
        addRenderableWidget(Button.builder(Component.literal("◀"), btn -> {
            if (browser != null) browser.goBack();
        }).bounds(currentBtnX, topBarY, 20, 20).build());
        currentBtnX += 24;

        // Forward
        addRenderableWidget(Button.builder(Component.literal("▶"), btn -> {
            if (browser != null) browser.goForward();
        }).bounds(currentBtnX, topBarY, 20, 20).build());
        currentBtnX += 24;

        // Reload
        addRenderableWidget(Button.builder(Component.literal("⟳"), btn -> {
            if (browser != null) browser.reload();
        }).bounds(currentBtnX, topBarY, 20, 20).build());
        currentBtnX += 24;

        // Home
        addRenderableWidget(Button.builder(Component.literal("⌂"), btn -> {
            if (browser != null) browser.loadURL(currentUrl);
            if (urlEditBox != null) urlEditBox.setValue(currentUrl);
        }).bounds(currentBtnX, topBarY, 20, 20).build());
        currentBtnX += 28;

        // Close button at top right
        int closeBtnX = width - 26;
        addRenderableWidget(Button.builder(Component.literal("✕"), btn -> onClose())
                .bounds(closeBtnX, topBarY, 20, 20).build());

        // URL Input and Save button (ONLY VISIBLE AND CREATED IF IN CREATIVE MODE)
        if (isCreative) {
            int saveBtnWidth = 54;
            int saveBtnX = closeBtnX - saveBtnWidth - 4;
            int labelWidth = 60;
            int inputX = currentBtnX + labelWidth;
            int inputWidth = Math.max(saveBtnX - inputX - 4, 80);

            urlEditBox = new EditBox(font, inputX, topBarY + 1, inputWidth, 18, Component.literal("URL"));
            urlEditBox.setMaxLength(2048);
            urlEditBox.setValue(currentUrl);
            addRenderableWidget(urlEditBox);

            addRenderableWidget(Button.builder(Component.literal("💾 Salvar"), btn -> saveUrl())
                    .bounds(saveBtnX, topBarY, saveBtnWidth, 20).build());
        }

        // Initialize Browser if MCEF is available & initialized
        if (BrowserManager.isMcefInitialized()) {
            if (browser == null) {
                browser = new MCEFWrapper(currentUrl, getPixelWidth(), getPixelHeight());
            } else {
                browser.resize(getPixelWidth(), getPixelHeight());
            }
        }
    }

    private void saveUrl() {
        if (!isCreative || urlEditBox == null) return;
        String newUrl = urlEditBox.getValue().trim();
        if (newUrl.isEmpty()) return;

        if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://") && !newUrl.startsWith("about:")) {
            newUrl = "https://" + newUrl;
            urlEditBox.setValue(newUrl);
        }

        this.currentUrl = newUrl;
        if (pos != null) {
            PacketDistributor.sendToServer(new UpdateComputerUrlC2SPacket(pos, newUrl));
        }
        if (browser != null) {
            browser.loadURL(newUrl);
        }
        this.statusNotification = "§a✔ Salvo!";
        this.statusNotificationTime = System.currentTimeMillis();
    }

    private int toPixelX(double mouseX) {
        double scale = minecraft != null ? minecraft.getWindow().getGuiScale() : 1.0;
        return (int) ((mouseX - viewportX) * scale);
    }

    private int toPixelY(double mouseY) {
        double scale = minecraft != null ? minecraft.getWindow().getGuiScale() : 1.0;
        return (int) ((mouseY - viewportY) * scale);
    }

    private int getPixelWidth() {
        double scale = minecraft != null ? minecraft.getWindow().getGuiScale() : 1.0;
        return (int) (viewportWidth * scale);
    }

    private int getPixelHeight() {
        double scale = minecraft != null ? minecraft.getWindow().getGuiScale() : 1.0;
        return (int) (viewportHeight * scale);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String savedInput = (urlEditBox != null) ? urlEditBox.getValue() : null;
        super.resize(minecraft, width, height);
        if (urlEditBox != null && savedInput != null) {
            urlEditBox.setValue(savedInput);
        }
        if (browser != null) {
            browser.resize(getPixelWidth(), getPixelHeight());
        }
    }

    @Override
    public void renderBackground(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        // Leave empty so super.render() does not draw a full-screen solid box over custom layers
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        // 1. Fill base screen background (solid opaque avoids Minecraft 1.21's world blur shader)
        gui.fill(0, 0, width, height, 0xFF0E1114);

        // 2. Viewport Frame (Vintage Retro Beige Monitor Bezel)
        gui.fill(viewportX - 2, viewportY - 2, viewportX + viewportWidth + 2, viewportY + viewportHeight + 2, 0xFFDCD3BE);
        gui.renderOutline(viewportX - 1, viewportY - 1, viewportWidth + 2, viewportHeight + 2, 0xFF22201C);

        // 3. Viewport Inner Black Surface
        gui.fill(viewportX, viewportY, viewportX + viewportWidth, viewportY + viewportHeight, 0xFF000000);

        // 4. Top Toolbar background & header
        gui.fill(0, 0, width, 30, 0xFF1C2024);
        gui.fill(0, 29, width, 30, 0xFF363B40);

        // 5. Bottom Status Bar
        gui.fill(0, height - 16, width, height, 0xFF121417);
        gui.drawString(font, "§8URL: §7" + truncateUrl(currentUrl, 65), 8, height - 12, 0xFFAAAAAA, false);

        if (!statusNotification.isEmpty() && System.currentTimeMillis() - statusNotificationTime < 3000) {
            gui.drawString(font, statusNotification, width - font.width(statusNotification) - 10, height - 12, 0xFF55FF55, false);
        }

        // 6. Render widgets (Buttons and EditBox)
        super.render(gui, mouseX, mouseY, partialTick);

        // 7. Flush GuiGraphics before raw OpenGL/texture draw
        gui.flush();

        // 8. Render Browser or Fallback CRT Screen inside the viewport
        if (browser != null && browser.isReady() && browser.hasValidTexture()) {
            browser.render(viewportX, viewportY, viewportWidth, viewportHeight);
        } else {
            renderFallbackScreen(gui, mouseX, mouseY);
            gui.flush();
        }
    }

    private void renderFallbackScreen(GuiGraphics gui, int mouseX, int mouseY) {
        int vx = viewportX;
        int vy = viewportY;
        int vw = viewportWidth;
        int vh = viewportHeight;

        // Dark Phosphor Terminal background (Solid opaque black/green)
        gui.fill(vx, vy, vx + vw, vy + vh, 0xFF060D06);
        gui.renderOutline(vx + 2, vy + 2, vw - 4, vh - 4, 0xFF163816);

        int centerX = vx + vw / 2;
        int startY = vy + 30;

        gui.drawCenteredString(font, "§a╔══════════════════════════════════════════════════════════════╗", centerX, startY, 0xFF39FF14);
        gui.drawCenteredString(font, "§a║                  ABERTURA OS - VERSÃO 3.6.2.1                ║", centerX, startY + 12, 0xFF39FF14);
        gui.drawCenteredString(font, "§a║                      ORACLE 0TERMINAL                        ║", centerX, startY + 24, 0xFF39FF14);
        gui.drawCenteredString(font, "§a╚══════════════════════════════════════════════════════════════╝", centerX, startY + 36, 0xFF39FF14);

        if (!BrowserManager.isMcefAvailable()) {
            gui.drawCenteredString(font, "§6[AVISO] MCEF não instalado.", centerX, startY + 105, 0xFFFFAA00);
            gui.drawCenteredString(font, "§7Esta funcionalidade não estará disponível.", centerX, startY + 120, 0xFFAAAAAA);
        } else if (!BrowserManager.isMcefInitialized()) {
            gui.drawCenteredString(font, "§e[MCEF] Inicializando Chromium... Aguarde alguns segundos.", centerX, startY + 105, 0xFFFFFF55);
            gui.drawCenteredString(font, "§7O motor de navegação está baixando/carregando os binários necessários.", centerX, startY + 120, 0xFFAAAAAA);
        } else {
            gui.drawCenteredString(font, "§e[MCEF] Carregando página web...", centerX, startY + 105, 0xFFFFFF55);
            gui.drawCenteredString(font, "§7Conectando ao servidor e gerando a imagem de renderização.", centerX, startY + 120, 0xFFAAAAAA);
        }

        // Clickable external browser prompt
        int btnW = 220;
        int btnH = 22;
        int btnX = centerX - btnW / 2;
        int btnY = startY + 150;
        boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;

        gui.fill(btnX, btnY, btnX + btnW, btnY + btnH, hovered ? 0xFF1B5E20 : 0xFF0D3310);
        gui.renderOutline(btnX, btnY, btnW, btnH, hovered ? 0xFF39FF14 : 0xFF22CC11);
        gui.drawCenteredString(font, "§a🌐 Abrir no Navegador Externo", centerX, btnY + 7, hovered ? 0xFFFFFFFF : 0xFF39FF14);
    }

    private boolean isInsideViewport(double x, double y) {
        return x >= viewportX && x <= viewportX + viewportWidth && y >= viewportY && y <= viewportY + viewportHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Ignore clicks during the first 250ms of opening to prevent block interaction click bleed-through
        if (System.currentTimeMillis() - openTimestamp < 250) {
            return true;
        }

        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (browser != null && browser.isReady() && browser.hasValidTexture() && isInsideViewport(mouseX, mouseY)) {
            browser.sendMousePress(toPixelX(mouseX), toPixelY(mouseY), button);
            return true;
        }

        // If fallback screen, check if external link button was clicked
        if (browser == null || !browser.isReady() || !browser.hasValidTexture()) {
            int centerX = viewportX + viewportWidth / 2;
            int startY = viewportY + 30;
            int btnW = 220;
            int btnH = 22;
            int btnX = centerX - btnW / 2;
            int btnY = startY + 150;
            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                openExternalUrl();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (System.currentTimeMillis() - openTimestamp < 250) {
            return true;
        }

        if (browser != null && browser.isReady() && browser.hasValidTexture() && isInsideViewport(mouseX, mouseY)) {
            browser.sendMouseRelease(toPixelX(mouseX), toPixelY(mouseY), button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (browser != null && browser.isReady() && browser.hasValidTexture() && isInsideViewport(mouseX, mouseY)) {
            browser.sendMouseMove(toPixelX(mouseX), toPixelY(mouseY));
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (browser != null && browser.isReady() && browser.hasValidTexture() && isInsideViewport(mouseX, mouseY)) {
            browser.sendMouseWheel(toPixelX(mouseX), toPixelY(mouseY), scrollY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (urlEditBox != null && urlEditBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                saveUrl();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }

        if (browser != null && browser.isReady() && browser.hasValidTexture()) {
            browser.sendKeyPress(keyCode, scanCode, modifiers);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (urlEditBox != null && urlEditBox.isFocused()) {
            return super.keyReleased(keyCode, scanCode, modifiers);
        }

        if (browser != null && browser.isReady() && browser.hasValidTexture()) {
            browser.sendKeyRelease(keyCode, scanCode, modifiers);
            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (urlEditBox != null && urlEditBox.isFocused()) {
            return super.charTyped(codePoint, modifiers);
        }

        if (codePoint != (char) 0 && browser != null && browser.isReady() && browser.hasValidTexture()) {
            browser.sendKeyTyped(codePoint, modifiers);
            return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    private void openExternalUrl() {
        try {
            Util.getPlatform().openUri(new java.net.URI(currentUrl));
        } catch (Exception e) {
            try {
                Util.getPlatform().openUri(new java.net.URI("https://" + currentUrl));
            } catch (Exception ignored) {}
        }
    }

    private String truncateUrl(String url, int maxLen) {
        if (url == null) return "";
        if (url.length() <= maxLen) return url;
        return url.substring(0, maxLen - 3) + "...";
    }

    @Override
    public void onClose() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
