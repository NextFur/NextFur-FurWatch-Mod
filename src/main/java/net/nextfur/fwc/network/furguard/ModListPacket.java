package net.nextfur.fwc.network.furguard;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.FwMain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModListPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "client_modlist");
    public static final Type<ModListPacket> TYPE = new Type<>(ID);

    private final String username;
    private final List<String> modList;
    private final Map<String, String> modFileHashes;

    public ModListPacket(String username, List<String> modList, Map<String, String> modFileHashes) {
        this.username = username;
        this.modList = modList;
        this.modFileHashes = modFileHashes;
    }

    public static final StreamCodec<FriendlyByteBuf, ModListPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ModListPacket::getUsername,

            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            ModListPacket::getModList,

            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8),
            ModListPacket::getModFileHashes,

            ModListPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public List<String> getModList() {
        return this.modList;
    }

    public String getUsername() {
        return this.username;
    }

    public Map<String, String> getModFileHashes() {
        return this.modFileHashes;
    }
}
