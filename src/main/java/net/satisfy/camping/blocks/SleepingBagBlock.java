package net.satisfy.camping.blocks;

import net.satisfy.camping.blockentity.TravelersBackpackBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class SleepingBagBlock extends BedBlock
{
    public static final EnumProperty<BedPart> PART;
    public static final BooleanProperty OCCUPIED;
    public static final BooleanProperty CAN_DROP;

    private static final VoxelShape SLEEPING_BAG_SHAPE;
    private static final VoxelShape SLEEPING_BAG_SHAPE_NORTH;
    private static final VoxelShape SLEEPING_BAG_SHAPE_EAST;
    private static final VoxelShape SLEEPING_BAG_SHAPE_SOUTH;
    private static final VoxelShape SLEEPING_BAG_SHAPE_WEST;
    public SleepingBagBlock(DyeColor color, Settings settings)
    {
        super(color, settings.solid());
        this.setDefaultState(this.stateManager.getDefaultState().with(PART, BedPart.FOOT).with(OCCUPIED, false).with(CAN_DROP, true));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(PART)) {
            case FOOT:
                return SLEEPING_BAG_SHAPE;
            case HEAD:
                switch (state.get(FACING)) {
                    case EAST:
                        return SLEEPING_BAG_SHAPE_EAST;
                    case SOUTH:
                        return SLEEPING_BAG_SHAPE_SOUTH;
                    case WEST:
                        return SLEEPING_BAG_SHAPE_WEST;
                    default:
                        return SLEEPING_BAG_SHAPE_NORTH;
                }
        }
        return SLEEPING_BAG_SHAPE;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit)
    {
        if(world.isClient)
        {
            return ActionResult.CONSUME;
        }
        else
        {
            if(state.get(PART) != BedPart.HEAD)
            {
                pos = pos.offset(state.get(FACING));
                state = world.getBlockState(pos);
                if(!state.isOf(this))
                {
                    return ActionResult.CONSUME;
                }
            }

            if(!isBedWorking(world))
            {
                world.removeBlock(pos, false);
                BlockPos blockPos = pos.offset((state.get(FACING)).getOpposite());
                if(world.getBlockState(blockPos).isOf(this))
                {
                    world.removeBlock(blockPos, false);
                }

                //world.createExplosion(null, DamageSource.badRespawnPoint(), null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, 5.0F, true, Explosion.DestructionType.DESTROY);
                return ActionResult.SUCCESS;
            }
            else if(state.get(OCCUPIED))
            {
                if(!this.isFree(world, pos))
                {
                    player.sendMessage(Text.translatable("block.minecraft.bed.occupied"), true);
                }
                return ActionResult.SUCCESS;
            }
            else
            {
                if(player instanceof ServerPlayerEntity)
                {
                    player.trySleep(pos).ifLeft((sleepFailureReason) ->
                    {
                        if(sleepFailureReason != null)
                        {
                            player.sendMessage(sleepFailureReason.getMessage(), true);
                        }
                    });
                }
                return ActionResult.SUCCESS;
            }
        }
    }

    private boolean isFree(World world, BlockPos pos)
    {
        List<VillagerEntity> list = world.getEntitiesByClass(VillagerEntity.class, new Box(pos), LivingEntity::isSleeping);
        if(list.isEmpty())
        {
            return false;
        }
        else
        {
            (list.get(0)).wakeUp();
            return true;
        }
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance)
    {
        super.onLandedUpon(world, state, pos, entity, fallDistance * 0.75F);
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity)
    {
        if(entity.bypassesLandingEffects())
        {
            super.onEntityLand(world, entity);
        }
        else
        {
            this.bounceEntity(entity);
        }

    }

    private void bounceEntity(Entity entity)
    {
        Vec3d vec3d = entity.getVelocity();
        if(vec3d.y < 0.0D)
        {
            double d = entity instanceof LivingEntity ? 1.0D : 0.8D;
            entity.setVelocity(vec3d.x, -vec3d.y * 0.6600000262260437D * d, vec3d.z);
        }
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos)
    {
        if(direction == getDirectionTowardsOtherPart(state.get(PART), state.get(FACING)))
        {
            return neighborState.isOf(this) && neighborState.get(PART) != state.get(PART) ? state.with(OCCUPIED, neighborState.get(OCCUPIED)) : Blocks.AIR.getDefaultState();
        }
        else
        {
            return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
        }
    }

    private static Direction getDirectionTowardsOtherPart(BedPart part, Direction direction)
    {
        return part == BedPart.FOOT ? direction : direction.getOpposite();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player)
    {
        boolean isFoot = state.get(PART) == BedPart.FOOT;

        BlockPos backpackPos = isFoot ? pos.offset(state.get(FACING).getOpposite()) : pos.offset(state.get(FACING).getOpposite(), 2);

        if(world.getBlockEntity(backpackPos) instanceof TravelersBackpackBlockEntity blockEntity)
        {
            blockEntity.setSleepingBagDeployed(false);
        }
        super.onBreak(world, pos, state, player);
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx)
    {
        Direction direction = ctx.getHorizontalPlayerFacing();
        BlockPos blockPos = ctx.getBlockPos();
        BlockPos blockPos2 = blockPos.offset(direction);
        return ctx.getWorld().getBlockState(blockPos2).canReplace(ctx) ? this.getDefaultState().with(FACING, direction) : null;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, OCCUPIED, CAN_DROP);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack)
    {
        super.onPlaced(world, pos, state, placer, itemStack);
        if(!world.isClient)
        {
            BlockPos blockPos = pos.offset(state.get(FACING));
            world.setBlockState(blockPos, state.with(PART, BedPart.HEAD), 3);
            world.updateNeighbors(pos, Blocks.AIR);
            state.updateNeighbors(world, pos, 3);
        }

    }

    @Override
    @Environment(EnvType.CLIENT)
    public long getRenderingSeed(BlockState state, BlockPos pos)
    {
        BlockPos blockPos = pos.offset(state.get(FACING), state.get(PART) == BedPart.HEAD ? 0 : 1);
        return MathHelper.hashCode(blockPos.getX(), pos.getY(), blockPos.getZ());
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder)
    {
        if(!state.get(CAN_DROP))
        {
            return List.of();
        }
        return super.getDroppedStacks(state, builder);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state)
    {
        return null;
    }

    static {
        PART = Properties.BED_PART;
        OCCUPIED = Properties.OCCUPIED;
        CAN_DROP = Properties.CONDITIONAL;
        SLEEPING_BAG_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
        SLEEPING_BAG_SHAPE_NORTH = Stream.of(
                Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
                Block.createCuboidShape(0.0D, 2.0D, 0.0D, 16.0D, 2.5D, 8.0D)
        ).reduce((v1, v2) -> VoxelShapes.combine(v1, v2, BooleanBiFunction.OR)).get();

        SLEEPING_BAG_SHAPE_EAST = Stream.of(
                Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
                Block.createCuboidShape(8.0D, 2.0D, 0.0D, 16.0D, 2.5D, 16.0D)
        ).reduce((v1, v2) -> VoxelShapes.combine(v1, v2, BooleanBiFunction.OR)).get();

        SLEEPING_BAG_SHAPE_SOUTH = Stream.of(
                Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
                Block.createCuboidShape(0.0D, 2.0D, 8.0D, 16.0D, 2.5D, 16.0D)
        ).reduce((v1, v2) -> VoxelShapes.combine(v1, v2, BooleanBiFunction.OR)).get();

        SLEEPING_BAG_SHAPE_WEST = Stream.of(
                Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
                Block.createCuboidShape(0.0D, 2.0D, 0.0D, 8.0D, 2.5D, 16.0D)
        ).reduce((v1, v2) -> VoxelShapes.combine(v1, v2, BooleanBiFunction.OR)).get();
    }
}