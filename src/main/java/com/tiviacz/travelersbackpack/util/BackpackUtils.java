package com.tiviacz.travelersbackpack.util;

import com.tiviacz.travelersbackpack.blockentity.TravelersBackpackBlockEntity;
import com.tiviacz.travelersbackpack.common.BackpackManager;
import com.tiviacz.travelersbackpack.component.ComponentUtils;
import com.tiviacz.travelersbackpack.config.TravelersBackpackConfig;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class BackpackUtils
{
    public static void onPlayerDeath(World world, PlayerEntity player, ItemStack stack)
    {
        if(!world.isClient) BackpackManager.addBackpack((ServerPlayerEntity)player, stack);

        if(TravelersBackpackConfig.backpackDeathPlace)
        {
            if(TravelersBackpackConfig.backpackForceDeathPlace)
            {
                if(!forcePlace(world, player, stack))
                {
                    ItemEntity backpackItemEntity = player.dropStack(stack, 1);

                    if(backpackItemEntity != null)
                    {
                        backpackItemEntity.setCovetedItem();
                    }

                    ComponentUtils.getComponent(player).removeWearable();

                    player.sendMessage(Text.translatable("information.travelersbackpack.backpack_drop", player.getBlockPos().getX(), player.getBlockPos().getY(), player.getBlockPos().getZ()), false);
                    LogHelper.info("There's no space for backpack. Dropping backpack item at" + " X: " + player.getBlockPos().getX() + " Y: " + player.getBlockPos().getY() + " Z: " + player.getBlockPos().getZ());
                }
            }
            else if(!tryPlace(world, player, stack))
            {
                //player.entityDropItem(stack, 1); //Not too op though?
                //int offsetY = Math.max(0, -((int) player.getPosY()) + 1) + 1;
                ItemEntity backpackItemEntity = player.dropStack(stack, 1);

                if(backpackItemEntity != null)
                {
                    backpackItemEntity.setCovetedItem();
                }

                ComponentUtils.getComponent(player).removeWearable();

                player.sendMessage(Text.translatable("information.travelersbackpack.backpack_drop", player.getBlockPos().getX(), player.getBlockPos().getY(), player.getBlockPos().getZ()), false);
                LogHelper.info("There's no space for backpack. Dropping backpack item at" + " X: " + player.getBlockPos().getX() + " Y: " + player.getBlockPos().getY() + " Z: " + player.getBlockPos().getZ());
            }
        }
        else
        {
            if(TravelersBackpackConfig.trinketsIntegration) return;

            ItemEntity backpackItemEntity = player.dropStack(stack, 1);

            if(backpackItemEntity != null)
            {
                backpackItemEntity.setCovetedItem();
            }

            player.sendMessage(Text.translatable("information.travelersbackpack.backpack_drop", player.getBlockPos().getX(), player.getBlockPos().getY(), player.getBlockPos().getZ()), false);
            LogHelper.info("There's no space for backpack. Dropping backpack item at" + " X: " + player.getBlockPos().getX() + " Y: " + player.getBlockPos().getY() + " Z: " + player.getBlockPos().getZ());

            ComponentUtils.getComponent(player).removeWearable();
        }
    }

    public static BlockPos findBlock3D(World world, int x, int y, int z, Block block, int hRange, int vRange)
    {
        for(int i = (y - vRange); i <= (y + vRange); i++)
        {
            for(int j = (x - hRange); j <= (x + hRange); j++)
            {
                for(int k = (z - hRange); k <= (z + hRange); k++)
                {
                    if(world.getBlockState(new BlockPos(j, i, k)).getBlock() == block)
                    {
                        return new BlockPos(j, i, k);
                    }
                }
            }
        }
        return null;
    }

    private static boolean forcePlace(World world, PlayerEntity player, ItemStack stack)
    {
        if(stack.getNbt() == null)
        {
            stack.setNbt(new NbtCompound());
        }

        Block block = Block.getBlockFromItem(stack.getItem());
        BlockPos playerPos = player.getBlockPos();
        int y = playerPos.getY();

        if(y <= world.getDimension().minY() || y >= world.getHeight())
        {
            for(int i = 1; i < world.getHeight(); i++)
            {
                BlockPos pos = new BlockPos(playerPos.getX(), i, playerPos.getZ());

                if(world.getBlockState(pos).isAir() || world.getBlockState(pos).getHardness(world, pos) > -1)
                {
                    y = i;
                    break;
                }
            }
        }

        BlockPos targetPos = new BlockPos(playerPos.getX(), y, playerPos.getZ());

        if(world.getBlockState(targetPos).getHardness(world, targetPos) > -1)
        {
            while(world.getBlockEntity(targetPos) != null)
            {
                targetPos = targetPos.up();
            }

            if(!world.setBlockState(targetPos, block.getDefaultState()))
            {
                return false;
            }

            player.sendMessage(Text.translatable("information.travelersbackpack.backpack_coords", targetPos.getX(), targetPos.getY(), targetPos.getZ()), false);
            LogHelper.info("Your backpack has been placed at" + " X: " + targetPos.getX() + " Y: " + targetPos.getY() + " Z: " + targetPos.getZ());

            world.playSound(player, playerPos.getX(), y, playerPos.getZ(), block.getDefaultState().getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 0.5F, 1.0F);
            ((TravelersBackpackBlockEntity)world.getBlockEntity(targetPos)).readAllData(stack.getNbt());

            if(ComponentUtils.isWearingBackpack(player))
            {
                ComponentUtils.getComponent(player).removeWearable();
            }

            return true;
        }
        return false;
    }

    private static boolean tryPlace(World world, @NotNull PlayerEntity player, ItemStack stack)
    {
        int X = (int) player.getX();
        int Z = (int) player.getZ();
        int[] positions = {0,-1,1,-2,2,-3,3,-4,4,-5,5,-6,6};

        for(int Y: positions)
        {
            BlockPos spawn = getNearestEmptyChunkCoordinatesSpiral(player, world, X, Z, new BlockPos(X, (int)player.getY() + Y, Z), 12, true, 1, (byte) 0, false);

            if(spawn != null)
            {
                return placeBackpack(stack, player, world, spawn.getX(), spawn.getY(), spawn.getZ(), Direction.UP);
            }
        }
        return false;
    }

    public static boolean placeBackpack(ItemStack stack, PlayerEntity player, World world, int x, int y, int z, Direction facing)
    {
        if(stack.getNbt() == null)
        {
            stack.setNbt(new NbtCompound());
        }

        Block block = Block.getBlockFromItem(stack.getItem());

        if(y <= world.getDimension().minY() || y >= world.getHeight()) return false;

        BlockPos targetPos = new BlockPos(x, y, z);

        if(world.getBlockState(targetPos).getMaterial().isReplaceable())
        {
            if(!world.getBlockState(targetPos).getMaterial().isSolid())
            {
                if(!world.setBlockState(targetPos, block.getDefaultState()))
                {
                    return false;
                }

                player.sendMessage(Text.translatable("information.travelersbackpack.backpack_coords", targetPos.getX(), targetPos.getY(), targetPos.getZ()), false);
                LogHelper.info("Your backpack has been placed at" + " X: " + targetPos.getX() + " Y: " + targetPos.getY() + " Z: " + targetPos.getZ());

                world.playSound(player, x, y, z, block.getDefaultState().getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 0.5F, 1.0F);
                ((TravelersBackpackBlockEntity)world.getBlockEntity(targetPos)).readAllData(stack.getNbt());

                if(stack.hasCustomName())
                {
                    ((TravelersBackpackBlockEntity)world.getBlockEntity(targetPos)).setCustomName(stack.getName());
                }

                if(ComponentUtils.isWearingBackpack(player))
                {
                    ComponentUtils.getComponent(player).removeWearable();
                }

                return true;
            }
        }
        return false;
    }

    public static String getConvertedTime(int ticks) {

        int i = ticks / 20;
        int minutes = i / 60;
        int seconds = i % 60;

        if (seconds < 10) {
            return minutes + ":" + "0" + seconds;
        }

        return minutes + ":" + seconds;
    }

    public static boolean isShiftPressed()
    {
        return GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    /**
     * Gets you the nearest Empty Chunk Coordinates, free of charge! Looks in two dimensions and finds a block
     * that a: can have stuff placed on it and b: has space above it.
     * This is a spiral search, will begin at close range and move out.
     * @param world  The world object.
     * @param origX  Original X coordinate
     * @param origZ  Original Z coordinate
     * @param pos      Moving X coordinate, should be the same as origX when called.
     * @param pos      Y coordinate, does not move.
     * @param pos      Moving Z coordinate, should be the same as origZ when called.
     * @param radius The radius of the search. If set to high numbers, will create a ton of lag
     * @param except Wether to include the origin of the search as a valid block.
     * @param steps  Number of steps of the recursive recursiveness that recurses through the recursion. It is the first size of the spiral, should be one (1) always at the first call.
     * @param pass   Pass switch for the witchcraft I can't quite explain. Set to 0 always at the beggining.
     * @param type   True = for player, False = for backpack
     * @return The coordinates of the block in the chunk of the world of the game of the server of the owner of the computer, where you can place something above it.
     */
    public static BlockPos getNearestEmptyChunkCoordinatesSpiral(PlayerEntity player, World world, int origX, int origZ, BlockPos pos, int radius, boolean except, int steps, byte pass, boolean type)
    {
        //Spiral search, because Mr Darkona is awesome :)
        //This is so the backpack tries to get placed near the death point first
        //And then goes looking farther away at each step
        //Steps mod 2 == 0 => X++, Z--
        //Steps mod 2 == 1 => X--, Z++

        if(steps >= radius)
        {
            return null;
        }

        int i = pos.getX();
        int j = pos.getZ();

        if(steps % 2 == 0)
        {
            if(pass == 0)
            {
                for(; i <= pos.getX() + steps; i++)
                {
                    BlockPos blockPos = type ? checkCoordsForPlayer(player, world, origX, origZ, pos, except) : checkCoordsForBackpack(player, world, origX, origZ, pos, except);

                    if(blockPos != null)
                    {
                        return blockPos;
                    }
                }
                pass++;
                return getNearestEmptyChunkCoordinatesSpiral(player, world, origX, origZ, new BlockPos(i, pos.getY(), j), radius, except, steps, pass, type);
            }

            if(pass == 1)
            {
                for(; j >= pos.getZ() - steps; j--)
                {
                    BlockPos blockPos = type ? checkCoordsForPlayer(player, world, origX, origZ, pos, except) : checkCoordsForBackpack(player, world, origX, origZ, pos, except);

                    if(blockPos != null)
                    {
                        return blockPos;
                    }
                }
                pass--;
                steps++;
                return getNearestEmptyChunkCoordinatesSpiral(player, world, origX, origZ, new BlockPos(i, pos.getY(), j), radius, except, steps, pass, type);
            }
        }

        if(steps % 2 == 1)
        {
            if(pass == 0)
            {
                for(; i >= pos.getX() - steps; i--)
                {
                    BlockPos blockPos = type ? checkCoordsForPlayer(player, world, origX, origZ, pos, except) : checkCoordsForBackpack(player, world, origX, origZ, pos, except);

                    if(blockPos != null)
                    {
                        return blockPos;
                    }
                }
                pass++;
                return getNearestEmptyChunkCoordinatesSpiral(player, world, origX, origZ, new BlockPos(i, pos.getY(), j), radius, except, steps, pass, type);
            }

            if(pass == 1)
            {
                for(; j <= pos.getZ() + steps; j++)
                {
                    BlockPos blockPos = type ? checkCoordsForPlayer(player, world, origX, origZ, pos, except) : checkCoordsForBackpack(player, world, origX, origZ, pos, except);

                    if(blockPos != null)
                    {
                        return blockPos;
                    }
                }
                pass--;
                steps++;
                return getNearestEmptyChunkCoordinatesSpiral(player, world, origX, origZ, new BlockPos(i, pos.getY(), j), radius, except, steps, pass, type);
            }
        }
        return null;
    }

    private static BlockPos checkCoordsForBackpack(PlayerEntity player, World world, int origX, int origZ, BlockPos pos, boolean except)
    {
        if(except && world.isTopSolid(pos.offset(Direction.DOWN), player) && world.isAir(pos) && !areCoordinatesTheSame(new BlockPos(origX, pos.getY(), origZ), pos))
        {
            return pos;
        }
        if(!except && world.isTopSolid(pos.offset(Direction.DOWN), player) && world.isAir(pos))
        {
            return pos;
        }
        return null;
    }

    private static BlockPos checkCoordsForPlayer(PlayerEntity player, World world, int origX, int origZ, BlockPos pos, boolean except)
    {
        if(except && world.isTopSolid(pos.offset(Direction.DOWN), player) && world.isAir(pos) && world.isAir(pos.offset(Direction.UP)) && !areCoordinatesTheSame2D(origX, origZ, pos.getX(), pos.getZ()))
        {
            return pos;
        }
        if(!except && world.isTopSolid(pos.offset(Direction.DOWN), player) && world.isAir(pos) && world.isAir(pos.offset(Direction.UP)))
        {
            return pos;
        }
        return null;
    }

    private static boolean areCoordinatesTheSame(BlockPos pos1, BlockPos pos2)
    {
        return pos1 == pos2;
    }

    private static boolean areCoordinatesTheSame2D(int X1, int Z1, int X2, int Z2)
    {
        return (X1 == X2 &&  Z1 == Z2);
    }
}