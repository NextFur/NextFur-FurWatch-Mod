package net.nextfur.fwc.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nextfur.fwc.blocks.SignalBlockerBlock;
import net.nextfur.fwc.init.FwModBlockEntities;
import net.nextfur.fwc.init.FwModEffects;

import java.util.List;

public class SignalBlockerBlockEntity extends BlockEntity {
    public static final int MODE_ALWAYS_ON = 0;
    public static final int MODE_INVERTED = 1; // Disabled by redstone interference (default)
    public static final int MODE_DIRECT = 2;   // Enabled by redstone

    public static final int DEFAULT_RADIUS = 16;
    public static final int MIN_RADIUS = 1;
    public static final int MAX_RADIUS = 128;

    private int radius = DEFAULT_RADIUS;
    private int redstoneMode = MODE_INVERTED;
    private boolean manualEnabled = true;
    private int tickCounter = 0;

    public SignalBlockerBlockEntity(BlockPos pos, BlockState blockState) {
        super(FwModBlockEntities.SIGNAL_BLOCKER.get(), pos, blockState);
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
        setChanged();
    }

    public int getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(int redstoneMode) {
        this.redstoneMode = Math.max(0, Math.min(2, redstoneMode));
        updateBlockState();
        setChanged();
    }

    public boolean isManualEnabled() {
        return manualEnabled;
    }

    public void setManualEnabled(boolean manualEnabled) {
        this.manualEnabled = manualEnabled;
        updateBlockState();
        setChanged();
    }

    public boolean isPowered() {
        if (level != null) {
            return level.hasNeighborSignal(worldPosition);
        }
        return false;
    }

    public boolean isActive() {
        if (!manualEnabled) {
            return false;
        }

        boolean powered = isPowered();

        return switch (redstoneMode) {
            case MODE_ALWAYS_ON -> true;
            case MODE_INVERTED -> !powered; // Redstone interference disables the blocker
            case MODE_DIRECT -> powered;   // Redstone enables the blocker
            default -> true;
        };
    }

    public void setPowered(boolean powered) {
        if (level != null && !level.isClientSide()) {
            updateBlockState();
            setChanged();
        }
    }

    public void updateBlockState() {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            if (state.hasProperty(SignalBlockerBlock.ENABLED)) {
                boolean active = isActive();
                if (state.getValue(SignalBlockerBlock.ENABLED) != active) {
                    level.setBlock(worldPosition, state.setValue(SignalBlockerBlock.ENABLED, active), 3);
                    setChanged();
                }
            }
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SignalBlockerBlockEntity blocker) {
        if (level.isClientSide()) {
            return;
        }

        blocker.tickCounter++;
        if (blocker.tickCounter >= 20) { // Every 1 second
            blocker.tickCounter = 0;

            boolean active = blocker.isActive();
            if (state.hasProperty(SignalBlockerBlock.ENABLED) && state.getValue(SignalBlockerBlock.ENABLED) != active) {
                level.setBlock(pos, state.setValue(SignalBlockerBlock.ENABLED, active), 3);
            }

            if (active) {
                blocker.applySignalBlockedEffect(level, pos);
            }
        }
    }

    private void applySignalBlockedEffect(Level level, BlockPos pos) {
        double r = this.radius;
        AABB aabb = new AABB(pos).inflate(r);
        List<Player> players = level.getEntitiesOfClass(Player.class, aabb);
        double rSq = r * r;
        Vec3 center = pos.getCenter();

        for (Player player : players) {
            if (player.distanceToSqr(center) <= rSq) {
                // Apply beacon effect: 100 ticks (5s) duration, re-applied every 1s (20 ticks)
                player.addEffect(new MobEffectInstance(
                        FwModEffects.SIGNAL_BLOCKED,
                        100,
                        0,
                        true,
                        true,
                        true
                ));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Radius", this.radius);
        tag.putInt("RedstoneMode", this.redstoneMode);
        tag.putBoolean("ManualEnabled", this.manualEnabled);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Radius")) {
            this.radius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, tag.getInt("Radius")));
        }
        if (tag.contains("RedstoneMode")) {
            this.redstoneMode = Math.max(0, Math.min(2, tag.getInt("RedstoneMode")));
        }
        if (tag.contains("ManualEnabled")) {
            this.manualEnabled = tag.getBoolean("ManualEnabled");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Radius", this.radius);
        tag.putInt("RedstoneMode", this.redstoneMode);
        tag.putBoolean("ManualEnabled", this.manualEnabled);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
