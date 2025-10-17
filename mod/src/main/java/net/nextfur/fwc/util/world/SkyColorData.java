package net.nextfur.fwc.util.world;

public class SkyColorData {
    private static int currentFogColor = -1; // -1 = default
    private static int currentBoxColor = -1;

    public static int getCurrentFogColor() {
        return currentFogColor;
    }

    public static int getCurrentBoxColor() {
        return currentBoxColor;
    }

    public static void setCurrentFogColor(int color) {
        currentFogColor = color;
    }

    public static void setCurrentBoxColor(int color) {
        currentBoxColor = color;
    }
}