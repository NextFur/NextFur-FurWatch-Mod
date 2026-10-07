package net.nextfur.fwc.pda.app;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.pda.client.PdaScreen;
import net.nextfur.fwc.pda.data.PdaData;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A dynamically constructed PDA application.
 * Allows developers to build new apps fluently by specifying logos, theme colors,
 * widgets, lifecycle handlers, and renderers.
 */
public class DynamicPdaApp extends PdaApp {
    @FunctionalInterface
    public interface IconRenderer {
        void render(GuiGraphics gui, int x, int y, int size, boolean hovered, int themeColor);
    }

    @FunctionalInterface
    public interface ContentRenderer {
        void render(DynamicPdaApp app, GuiGraphics gui, int mouseX, int mouseY, float partialTick, int x, int y, int width, int height);
    }

    @FunctionalInterface
    public interface InitHandler {
        void onInit(DynamicPdaApp app, PdaScreen screen, int x, int y, int width, int height);
    }

    @FunctionalInterface
    public interface ClickHandler {
        boolean onClick(DynamicPdaApp app, double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    public interface KeyHandler {
        boolean onKey(DynamicPdaApp app, int keyCode, int scanCode, int modifiers);
    }

    private final IconRenderer customIconRenderer;
    private final InitHandler initHandler;
    private final ContentRenderer contentRenderer;
    private final ClickHandler clickHandler;
    private final KeyHandler keyHandler;
    private final Consumer<PdaData> dataUpdateHandler;
    private final Runnable closeHandler;

    private DynamicPdaApp(Builder builder) {
        super(builder.id, builder.displayName, builder.description, builder.themeColor, builder.iconTexture);
        this.customIconRenderer = builder.customIconRenderer;
        this.initHandler = builder.initHandler;
        this.contentRenderer = builder.contentRenderer;
        this.clickHandler = builder.clickHandler;
        this.keyHandler = builder.keyHandler;
        this.dataUpdateHandler = builder.dataUpdateHandler;
        this.closeHandler = builder.closeHandler;
    }

    public static Builder builder(ResourceLocation id, Component displayName) {
        return new Builder(id, displayName);
    }

    @Override
    public void renderIcon(GuiGraphics gui, int x, int y, int size, boolean hovered) {
        if (customIconRenderer != null) {
            customIconRenderer.render(gui, x, y, size, hovered, getThemeColor());
        } else {
            super.renderIcon(gui, x, y, size, hovered);
        }
    }

    @Override
    public void init(PdaScreen screen, int contentX, int contentY, int contentWidth, int contentHeight) {
        if (initHandler != null) {
            initHandler.onInit(this, screen, contentX, contentY, contentWidth, contentHeight);
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick, int contentX, int contentY, int contentWidth, int contentHeight) {
        if (contentRenderer != null) {
            contentRenderer.render(this, gui, mouseX, mouseY, partialTick, contentX, contentY, contentWidth, contentHeight);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (clickHandler != null) {
            return clickHandler.onClick(this, mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyHandler != null) {
            return keyHandler.onKey(this, keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public void onClose() {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    @Override
    public void onDataUpdated(PdaData data) {
        if (dataUpdateHandler != null) {
            dataUpdateHandler.accept(data);
        }
    }

    public static class Builder {
        private final ResourceLocation id;
        private final Component displayName;
        private Component description = Component.empty();
        private int themeColor = 0xFF00D4FF;
        private ResourceLocation iconTexture = null;
        private IconRenderer customIconRenderer = null;
        private InitHandler initHandler = null;
        private ContentRenderer contentRenderer = null;
        private ClickHandler clickHandler = null;
        private KeyHandler keyHandler = null;
        private Consumer<PdaData> dataUpdateHandler = null;
        private Runnable closeHandler = null;

        public Builder(ResourceLocation id, Component displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder themeColor(int color) {
            this.themeColor = color;
            return this;
        }

        public Builder icon(ResourceLocation texture) {
            this.iconTexture = texture;
            return this;
        }

        public Builder iconRenderer(IconRenderer renderer) {
            this.customIconRenderer = renderer;
            return this;
        }

        public Builder onInit(InitHandler handler) {
            this.initHandler = handler;
            return this;
        }

        public Builder onRender(ContentRenderer renderer) {
            this.contentRenderer = renderer;
            return this;
        }

        public Builder onClick(ClickHandler handler) {
            this.clickHandler = handler;
            return this;
        }

        public Builder onKey(KeyHandler handler) {
            this.keyHandler = handler;
            return this;
        }

        public Builder onDataUpdate(Consumer<PdaData> handler) {
            this.dataUpdateHandler = handler;
            return this;
        }

        public Builder onClose(Runnable handler) {
            this.closeHandler = handler;
            return this;
        }

        public DynamicPdaApp build() {
            return new DynamicPdaApp(this);
        }
    }
}
