package net.nextfur.fwc.client.gui.browser;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class BrowserManager {
    private static Boolean MCEF_PRESENT = null;
    private static boolean contextMenuHandlerRegistered = false;

    public static boolean isMcefAvailable() {
        if (MCEF_PRESENT == null) {
            try {
                Class.forName("com.cinemamod.mcef.MCEF");
                MCEF_PRESENT = true;
            } catch (Throwable t) {
                MCEF_PRESENT = false;
            }
        }
        return MCEF_PRESENT;
    }

    public static boolean isMcefInitialized() {
        if (!isMcefAvailable()) return false;
        try {
            Class<?> clazz = Class.forName("com.cinemamod.mcef.MCEF");
            Method method = clazz.getMethod("isInitialized");
            boolean init = (boolean) method.invoke(null);
            if (init && !contextMenuHandlerRegistered) {
                setupMcefHooks(clazz);
            }
            return init;
        } catch (Throwable t) {
            return false;
        }
    }

    private static synchronized void setupMcefHooks(Class<?> mcefClass) {
        if (contextMenuHandlerRegistered) return;
        try {
            Method getClientMethod = mcefClass.getMethod("getClient");
            Object mcefClient = getClientMethod.invoke(null);
            if (mcefClient == null) return;

            Class<?> handlerInterface = Class.forName("org.cef.handler.CefContextMenuHandler");
            InvocationHandler invocationHandler = (proxy, method, args) -> {
                String methodName = method.getName();
                if ("onBeforeContextMenu".equals(methodName)) {
                    // Suppress default OSR context menu to prevent "Window handle is required for default OSR context menu" error
                    if (args != null && args.length >= 4 && args[3] != null) {
                        try {
                            Method clearMethod = args[3].getClass().getMethod("clear");
                            clearMethod.invoke(args[3]);
                        } catch (Throwable ignored) {}
                    }
                    return null;
                } else if ("onContextMenuCommand".equals(methodName)) {
                    return false;
                } else if ("onContextMenuDismissed".equals(methodName)) {
                    return null;
                } else if ("toString".equals(methodName)) {
                    return "FWC_CefContextMenuHandler";
                } else if ("hashCode".equals(methodName)) {
                    return System.identityHashCode(proxy);
                } else if ("equals".equals(methodName)) {
                    return proxy == (args != null && args.length > 0 ? args[0] : null);
                }
                return null;
            };

            Object proxyInstance = Proxy.newProxyInstance(
                handlerInterface.getClassLoader(),
                new Class<?>[]{handlerInterface},
                invocationHandler
            );

            Method addContextMenuHandler = mcefClient.getClass().getMethod("addContextMenuHandler", handlerInterface);
            addContextMenuHandler.invoke(mcefClient, proxyInstance);
            contextMenuHandlerRegistered = true;
        } catch (Throwable ignored) {
        }
    }
}

