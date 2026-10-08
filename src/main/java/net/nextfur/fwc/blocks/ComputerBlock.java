package net.nextfur.fwc.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import net.nextfur.fwc.blocks.entity.ComputerBlockEntity;
import net.nextfur.fwc.network.computer.OpenComputerScreenS2CPacket;

import javax.annotation.Nullable;

public class ComputerBlock extends BaseEntityBlock {
    public static final MapCodec<ComputerBlock> CODEC = simpleCodec(ComputerBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(1.0, 0.0, 0.0, 15.0, 4.0, 15.0),
            Block.box(2.0, 4.0, 4.0, 14.0, 14.0, 14.0)
    );
    protected static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 4.0, 16.0),
            Block.box(2.0, 4.0, 2.0, 14.0, 14.0, 12.0)
    );
    protected static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(0.0, 0.0, 1.0, 15.0, 4.0, 15.0),
            Block.box(4.0, 4.0, 2.0, 14.0, 14.0, 14.0)
    );
    protected static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 16.0, 4.0, 15.0),
            Block.box(2.0, 4.0, 2.0, 12.0, 14.0, 14.0)
    );

    public ComputerBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        return switch (dir) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ComputerBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            String url = ComputerBlockEntity.DEFAULT_URL;
            if (level.getBlockEntity(pos) instanceof ComputerBlockEntity be) {
                url = be.getUrl();
            }
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new OpenComputerScreenS2CPacket(pos, url));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static ComputerBlock create() {
        return new ComputerBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_GRAY)
                .sound(SoundType.METAL)
                .strength(2.0f, 6.0f)
                .noOcclusion());
    }
}
