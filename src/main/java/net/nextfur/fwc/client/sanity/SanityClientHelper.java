package net.nextfur.fwc.client.sanity;

public final class SanityClientHelper {
    private SanityClientHelper() {
    }

    public static void updateSanity(float sanity, boolean triggerSpike) {
        SanityClientHandler.onSanitySynced(sanity, triggerSpike);
    }
}
