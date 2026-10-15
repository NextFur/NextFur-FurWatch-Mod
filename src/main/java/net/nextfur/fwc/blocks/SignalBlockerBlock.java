package net.nextfur.fwc.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.blocks.entity.SignalBlockerBlockEntity;
import net.nextfur.fwc.init.FwModBlockEntities;
import net.nextfur.fwc.init.FwModParticles;
import net.nextfur.fwc.network.signalblocker.OpenSignalBlockerScreenS2CPacket;

import javax.annotation.Nullable;

public class SignalBlockerBlock extends BaseEntityBlock {
    public static final MapCodec<SignalBlockerBlock> CODEC = simpleCodec(SignalBlockerBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    public static SignalBlockerBlock create() {
        return new SignalBlockerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GREEN)
                .sound(SoundType.METAL)
                .strength(3.5f, 6.0f)
                .requiresCorrectToolForDrops());
    }

    public SignalBlockerBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ENABLED, true));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean powered = context.getLevel().hasNeighborSignal(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ENABLED, !powered);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SignalBlockerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, FwModBlockEntities.SIGNAL_BLOCKER.get(), SignalBlockerBlockEntity::tick);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, fromPos, isMoving);
        if (!level.isClientSide()) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SignalBlockerBlockEntity blocker) {
                blocker.setPowered(hasSignal);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.isCreative()) {
            if (!level.isClientSide()) {
                player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0f, 0.5f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SignalBlockerBlockEntity blocker) {
                PacketDistributor.sendToPlayer(serverPlayer, new OpenSignalBlockerScreenS2CPacket(
                        pos,
                        blocker.getRadius(),
                        blocker.getRedstoneMode(),
                        blocker.isManualEnabled(),
                        blocker.isActive()
                ));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(ENABLED)) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
            double y = pos.getY() + 1.02;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;

            level.addParticle(FwModParticles.EMP_PARTICLE.get(), x, y, z, 0.0, 0.02, 0.0);

            if (random.nextFloat() < 0.35f) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z,
                        (random.nextDouble() - 0.5) * 0.05, 0.04, (random.nextDouble() - 0.5) * 0.05);
            }
            if (random.nextFloat() < 0.15f) {
                level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.015, 0.0);
            }
        }
    }
}
