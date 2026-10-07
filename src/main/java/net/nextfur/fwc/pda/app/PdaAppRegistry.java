package net.nextfur.fwc.pda.app;

import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.pda.app.impl.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for all PDA applications.
 * Supports static and dynamic app registration.
 */
public class PdaAppRegistry {
    private static final Map<ResourceLocation, PdaApp> APPS = new LinkedHashMap<>();
    private static boolean initialized = false;

    public static synchronized void initDefaults() {
        if (initialized) return;
        initialized = true;

        // Register the 5 main default apps in standard priority order
        register(new MessagesApp());
        register(new NotepadApp());
        register(new ShieldBankApp());
        register(new VideoPlayerApp());
        register(new SettingsApp());
    }

    /**
     * Registers a new PDA app dynamically.
     */
    public static synchronized void register(PdaApp app) {
        APPS.put(app.getId(), app);
    }

    public static PdaApp get(ResourceLocation id) {
        initDefaults();
        return APPS.get(id);
    }

    public static List<PdaApp> getApps() {
        initDefaults();
        return Collections.unmodifiableList(new ArrayList<>(APPS.values()));
    }
}
