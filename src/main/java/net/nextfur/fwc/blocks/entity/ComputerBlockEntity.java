package net.nextfur.fwc.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nextfur.fwc.init.FwModBlockEntities;

public class ComputerBlockEntity extends BlockEntity {
    public static final String DEFAULT_URL = "https://fursmp.com";
    private String url = DEFAULT_URL;

    public ComputerBlockEntity(BlockPos pos, BlockState blockState) {
        super(FwModBlockEntities.COMPUTER.get(), pos, blockState);
    }

    public String getUrl() {
        return (url == null || url.trim().isEmpty()) ? DEFAULT_URL : url;
    }

    public void setUrl(String url) {
        this.url = (url == null || url.trim().isEmpty()) ? DEFAULT_URL : url;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Url", getUrl());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Url")) {
            this.url = tag.getString("Url");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("Url", getUrl());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
