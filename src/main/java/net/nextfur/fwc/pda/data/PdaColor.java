package net.nextfur.fwc.pda.data;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum PdaColor implements StringRepresentable {
    BLUE("blue", 0xFF00E5FF, 0xFF005F73, 0xFF001F2D, "Azul"),
    ORANGE("orange", 0xFFFF8800, 0xFF9E4700, 0xFF2D1400, "Laranja"),
    RED("red", 0xFFFF2A4B, 0xFF8A0B20, 0xFF2D040A, "Vermelho (Staff)");

    private final String name;
    private final int primaryColor;
    private final int accentColor;
    private final int darkBgColor;
    private final String displayName;

    PdaColor(String name, int primaryColor, int accentColor, int darkBgColor, String displayName) {
        this.name = name;
        this.primaryColor = primaryColor;
        this.accentColor = accentColor;
        this.darkBgColor = darkBgColor;
        this.displayName = displayName;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public int getDarkBgColor() {
        return darkBgColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public static final Codec<PdaColor> CODEC = StringRepresentable.fromEnum(PdaColor::values);
    public static final StreamCodec<ByteBuf, PdaColor> STREAM_CODEC = ByteBufCodecs.idMapper(
            id -> PdaColor.values()[id],
            PdaColor::ordinal
    );
}
