package net.nextfur.fwc.util.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class SkyColorSavedData extends SavedData {

    private static final String DATA_NAME = "fw_skycolor";

    private int fogColor = -1;
    private int boxColor = -1;

    public static final SavedData.Factory<SkyColorSavedData> FACTORY = new SavedData.Factory<>(
            SkyColorSavedData::new,
            SkyColorSavedData::load,
            null
    );

    public SkyColorSavedData() {}

    public int getFogColor() {
        return fogColor;
    }

    public int getBoxColor() {
        return boxColor;
    }

    public void setColors(int fogColor, int boxColor) {
        this.fogColor = fogColor;
        this.boxColor = boxColor;
        this.setDirty();
    }

    public static SkyColorSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        SkyColorSavedData data = new SkyColorSavedData();
        data.fogColor = tag.getInt("FogColor");
        data.boxColor = tag.getInt("BoxColor");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("FogColor", fogColor);
        tag.putInt("BoxColor", boxColor);
        return tag;
    }

    public static SkyColorSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }
}