package net.nextfur.fwc.util.client;

public class SkyColorState {
    private static int fogcolor = -1; // -1 = default
    private static int boxcolor = -1;

    public static void setFogColor(int newColor) {
        fogcolor = newColor;
    }
    public static void setBoxcolor(int newColor) {
        boxcolor = newColor;
    }

    public static int getFogColor() {
        return fogcolor;
    }

    public static int getBoxColor() {
        return boxcolor;
    }
}