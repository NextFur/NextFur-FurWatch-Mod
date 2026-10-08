package net.nextfur.fwc.client.gui.browser;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;

import java.lang.reflect.Method;

public class MCEFWrapper {
    private Object browser; // com.cinemamod.mcef.MCEFBrowser
    private Object renderer; // com.cinemamod.mcef.MCEFRenderer

    private Method mLoadURL;
    private Method mGetURL;
    private Method mGoBack;
    private Method mGoForward;
    private Method mReload;
    private Method mResize;
    private Method mGetRenderer;
    private Method mSendMousePress;
    private Method mSendMouseRelease;
    private Method mSendMouseMove;
    private Method mSendMouseWheel;
    private Method mSendKeyPress;
    private Method mSendKeyRelease;
    private Method mSendKeyTyped;
    private Method mSetFocus;
    private Method mClose;

    private Method mGetTextureID;

    public MCEFWrapper(String url, int width, int height) {
        try {
            Class<?> mcefClass = Class.forName("com.cinemamod.mcef.MCEF");
            Method isInitMethod = mcefClass.getMethod("isInitialized");
            boolean isInit = (boolean) isInitMethod.invoke(null);
            if (!isInit) {
                return;
            }

            int w = Math.max(width, 100);
            int h = Math.max(height, 100);

            try {
                Method createBrowser4 = mcefClass.getMethod("createBrowser", String.class, boolean.class, int.class, int.class);
                this.browser = createBrowser4.invoke(null, url, true, w, h);
            } catch (NoSuchMethodException e) {
                Method createBrowser2 = mcefClass.getMethod("createBrowser", String.class, boolean.class);
                this.browser = createBrowser2.invoke(null, url, true);
            }

            if (this.browser != null) {
                resolveMethods(this.browser.getClass());
                if (mResize != null) {
                    try {
                        mResize.invoke(this.browser, w, h);
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable t) {
            this.browser = null;
        }
    }

    private void resolveMethods(Class<?> clazz) {
        for (Method m : clazz.getMethods()) {
            String name = m.getName();
            Class<?>[] params = m.getParameterTypes();
            if (name.equals("loadURL") && params.length == 1 && params[0] == String.class) {
                mLoadURL = m;
            } else if (name.equals("getURL") && params.length == 0) {
                mGetURL = m;
            } else if (name.equals("goBack") && params.length == 0) {
                mGoBack = m;
            } else if (name.equals("goForward") && params.length == 0) {
                mGoForward = m;
            } else if (name.equals("reload") && params.length == 0) {
                mReload = m;
            } else if (name.equals("resize") && params.length == 2 && params[0] == int.class && params[1] == int.class) {
                mResize = m;
            } else if (name.equals("getRenderer") && params.length == 0) {
                mGetRenderer = m;
            } else if (name.equals("sendMousePress") && params.length == 3) {
                mSendMousePress = m;
            } else if (name.equals("sendMouseRelease") && params.length == 3) {
                mSendMouseRelease = m;
            } else if (name.equals("sendMouseMove") && params.length == 2) {
                mSendMouseMove = m;
            } else if (name.equals("sendMouseWheel")) {
                mSendMouseWheel = m;
            } else if (name.equals("sendKeyPress")) {
                mSendKeyPress = m;
            } else if (name.equals("sendKeyRelease")) {
                mSendKeyRelease = m;
            } else if (name.equals("sendKeyTyped")) {
                mSendKeyTyped = m;
            } else if (name.equals("setFocus") && params.length == 1 && params[0] == boolean.class) {
                mSetFocus = m;
            } else if (name.equals("close") && params.length == 0) {
                mClose = m;
            }
        }
    }

    public boolean isReady() {
        return browser != null;
    }

    public boolean hasValidTexture() {
        return browser != null && getTextureID() > 0;
    }

    public void loadURL(String url) {
        if (browser != null && mLoadURL != null) {
            try {
                mLoadURL.invoke(browser, url);
            } catch (Throwable ignored) {}
        }
    }

    public String getURL() {
        if (browser != null && mGetURL != null) {
            try {
                Object res = mGetURL.invoke(browser);
                return res != null ? res.toString() : "";
            } catch (Throwable ignored) {}
        }
        return "";
    }

    public void goBack() {
        if (browser != null && mGoBack != null) {
            try {
                mGoBack.invoke(browser);
            } catch (Throwable ignored) {}
        }
    }

    public void goForward() {
        if (browser != null && mGoForward != null) {
            try {
                mGoForward.invoke(browser);
            } catch (Throwable ignored) {}
        }
    }

    public void reload() {
        if (browser != null && mReload != null) {
            try {
                mReload.invoke(browser);
            } catch (Throwable ignored) {}
        }
    }

    public void resize(int width, int height) {
        if (browser != null && mResize != null && width > 50 && height > 50) {
            try {
                mResize.invoke(browser, width, height);
            } catch (Throwable ignored) {}
        }
    }

    public void render(int x, int y, int width, int height) {
        if (browser == null) return;
        int textureId = getTextureID();
        if (textureId <= 0) return;

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, textureId);

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buffer = t.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex(x, y + height, 0).setUv(0.0f, 1.0f).setColor(255, 255, 255, 255);
        buffer.addVertex(x + width, y + height, 0).setUv(1.0f, 1.0f).setColor(255, 255, 255, 255);
        buffer.addVertex(x + width, y, 0).setUv(1.0f, 0.0f).setColor(255, 255, 255, 255);
        buffer.addVertex(x, y, 0).setUv(0.0f, 0.0f).setColor(255, 255, 255, 255);

        MeshData mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }

        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.enableDepthTest();
    }

    private int getTextureID() {
        if (browser == null) return -1;
        try {
            if (renderer == null && mGetRenderer != null) {
                renderer = mGetRenderer.invoke(browser);
                if (renderer != null) {
                    mGetTextureID = renderer.getClass().getMethod("getTextureID");
                }
            }
            if (renderer != null && mGetTextureID != null) {
                return (int) mGetTextureID.invoke(renderer);
            }
        } catch (Throwable ignored) {}
        return -1;
    }

    public void sendMousePress(int x, int y, int button) {
        if (browser != null) {
            if (mSendMousePress != null) {
                try {
                    mSendMousePress.invoke(browser, x, y, button);
                } catch (Throwable ignored) {}
            }
            setFocus(true);
        }
    }

    public void sendMouseRelease(int x, int y, int button) {
        if (browser != null) {
            if (mSendMouseRelease != null) {
                try {
                    mSendMouseRelease.invoke(browser, x, y, button);
                } catch (Throwable ignored) {}
            }
            setFocus(true);
        }
    }

    public void sendMouseMove(int x, int y) {
        if (browser != null && mSendMouseMove != null) {
            try {
                mSendMouseMove.invoke(browser, x, y);
            } catch (Throwable ignored) {}
        }
    }

    public void sendMouseWheel(int x, int y, double scrollY) {
        if (browser != null && mSendMouseWheel != null) {
            try {
                if (mSendMouseWheel.getParameterCount() == 4) {
                    mSendMouseWheel.invoke(browser, x, y, scrollY, 0);
                } else if (mSendMouseWheel.getParameterCount() == 3) {
                    mSendMouseWheel.invoke(browser, x, y, scrollY);
                }
            } catch (Throwable ignored) {}
        }
    }

    public void sendKeyPress(int keyCode, int scanCode, int modifiers) {
        if (browser != null) {
            if (mSendKeyPress != null) {
                try {
                    if (mSendKeyPress.getParameterTypes()[1] == long.class) {
                        mSendKeyPress.invoke(browser, keyCode, (long) scanCode, modifiers);
                    } else {
                        mSendKeyPress.invoke(browser, keyCode, scanCode, modifiers);
                    }
                } catch (Throwable ignored) {}
            }
            setFocus(true);
        }
    }

    public void sendKeyRelease(int keyCode, int scanCode, int modifiers) {
        if (browser != null) {
            if (mSendKeyRelease != null) {
                try {
                    if (mSendKeyRelease.getParameterTypes()[1] == long.class) {
                        mSendKeyRelease.invoke(browser, keyCode, (long) scanCode, modifiers);
                    } else {
                        mSendKeyRelease.invoke(browser, keyCode, scanCode, modifiers);
                    }
                } catch (Throwable ignored) {}
            }
            setFocus(true);
        }
    }

    public void sendKeyTyped(char codePoint, int modifiers) {
        if (browser != null) {
            if (mSendKeyTyped != null) {
                try {
                    mSendKeyTyped.invoke(browser, codePoint, modifiers);
                } catch (Throwable ignored) {}
            }
            setFocus(true);
        }
    }

    public void setFocus(boolean focus) {
        if (browser != null && mSetFocus != null) {
            try {
                mSetFocus.invoke(browser, focus);
            } catch (Throwable ignored) {}
        }
    }

    public void close() {
        if (browser != null) {
            if (mClose != null) {
                try {
                    mClose.invoke(browser);
                } catch (Throwable ignored) {}
            }
            browser = null;
            renderer = null;
        }
    }
}
