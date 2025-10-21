package net.nextfur.fwc.network.furguard;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.nextfur.fwc.FwMain;

import java.util.Arrays;
import java.util.List;

public class ModListPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FwMain.MODID, "client_modlist");
    public static final Type<ModListPacket> TYPE = new Type<>(ID);

    private final String username;
    private final List<String> modList;

    public ModListPacket(String username, List<String> modList) {
        this.username = username;
        this.modList = modList;
    }

    public static final StreamCodec<FriendlyByteBuf, ModListPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUtf(packet.username);
                buf.writeVarInt(packet.modList.size());
                for (String mod : packet.modList) {
                    buf.writeUtf(mod);
                }
            },
            buf -> {
                String username = buf.readUtf();
                int size = buf.readVarInt();
                String[] mods = new String[size];
                for (int i = 0; i < size; i++) {
                    mods[i] = buf.readUtf();
                }
                return new ModListPacket(username, Arrays.asList(mods));
            }
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
}
