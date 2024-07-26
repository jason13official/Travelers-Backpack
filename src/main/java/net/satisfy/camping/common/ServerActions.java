package net.satisfy.camping.common;

import net.satisfy.camping.blockentity.TravelersBackpackBlockEntity;
import net.satisfy.camping.blocks.TravelersBackpackBlock;
import net.satisfy.camping.component.ComponentUtils;
import net.satisfy.camping.fluids.EffectFluidRegistry;
import net.satisfy.camping.init.ModBlocks;
import net.satisfy.camping.init.ModItems;
import net.satisfy.camping.inventory.ITravelersBackpackInventory;
import net.satisfy.camping.inventory.TravelersBackpackInventory;
import net.satisfy.camping.inventory.screen.TravelersBackpackBlockEntityScreenHandler;
import net.satisfy.camping.inventory.screen.TravelersBackpackItemScreenHandler;
import net.satisfy.camping.inventory.sorter.InventorySorter;
import net.satisfy.camping.items.HoseItem;
import net.satisfy.camping.util.Reference;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ServerActions
{
    public static void swapTool(PlayerEntity player, double scrollDelta)
    {
        if(ComponentUtils.isWearingBackpack(player))
        {
            TravelersBackpackInventory inventory = ComponentUtils.getBackpackInv(player);
            Inventory inv = inventory.getToolSlotsInventory();
            ItemStack heldItem = player.getMainHandStack();

            int toolSlots = inv.size();

            if(inv.isEmpty()) return;

            int firstSlot = 0;
            int lastSlot = firstSlot + (toolSlots - 1);

            int j = 0;

            for(int i = firstSlot; i <= lastSlot; i++)
            {
                if(!inv.getStack(i).isEmpty())
                {
                    j++;
                }
            }

            ItemStack[] tools = new ItemStack[j];

            if(scrollDelta < 0)
            {
                int slot = 0;

                for(int i = firstSlot; i <= firstSlot + j - 1; i++)
                {
                    tools[slot] = inv.getStack(i).copy();
                    slot++;
                }

                ItemStack tempStack = tools[tools.length - 1];

                for(int i = tools.length - 1; i >= 0; i--)
                {
                    if(i - 1 < 0)
                    {
                        tools[0] = heldItem;
                        player.setStackInHand(Hand.MAIN_HAND, tempStack);
                    }
                    else
                    {
                        tools[i] = tools[i - 1];
                    }
                }

                slot = 0;

                for(int i = firstSlot; i <= firstSlot + j - 1; i++)
                {
                    inv.setStack(i, tools[slot]);
                    slot++;
                }
            }
            else if(scrollDelta > 0)
            {
                int slot = 0;

                for(int i = firstSlot; i <= firstSlot + j - 1; i++)
                {
                    tools[slot] = inv.getStack(i).copy();
                    slot++;
                }

                ItemStack tempStack = tools[0];

                for(int i = 0; i <= tools.length - 1; i++)
                {
                    if(i + 1 > tools.length - 1)
                    {
                        tools[tools.length - 1] = heldItem;
                        player.setStackInHand(Hand.MAIN_HAND, tempStack);
                    }
                    else
                    {
                        tools[i] = tools[i + 1];
                    }
                }

                slot = 0;

                for(int i = firstSlot; i <= firstSlot + j - 1; i++)
                {
                    inv.setStack(i, tools[slot]);
                    slot++;
                }
            }
            inventory.markDataDirty(ITravelersBackpackInventory.TOOLS_DATA);
        }
    }

    public static void equipBackpack(PlayerEntity player)
    {
        World world = player.getWorld();

        if(!world.isClient)
        {
            if(!ComponentUtils.getComponent(player).hasWearable())
            {
                if(player.currentScreenHandler instanceof TravelersBackpackItemScreenHandler) ((ServerPlayerEntity)player).closeHandledScreen();

                ItemStack stack = player.getMainHandStack().copy();

                ComponentUtils.getComponent(player).setWearable(stack);
                ComponentUtils.getComponent(player).setContents(stack);
                player.getMainHandStack().decrement(1);
                world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 1.0F, (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F);

                //Sync
                ComponentUtils.sync(player);
            }
            ((ServerPlayerEntity)player).closeHandledScreen();
        }
    }

    public static void unequipBackpack(PlayerEntity player)
    {
        World world = player.getWorld();

        if(!world.isClient)
        {
            if(player.currentScreenHandler instanceof TravelersBackpackItemScreenHandler) player.currentScreenHandler.onClosed(player);

            ItemStack wearable = ComponentUtils.getComponent(player).getWearable().copy();

            if(!player.getInventory().insertStack(wearable))
            {
                player.sendMessage(Text.translatable(Reference.NO_SPACE), false);
                ((ServerPlayerEntity)player).closeHandledScreen();

                return;
            }

            if(ComponentUtils.getComponent(player).hasWearable())
            {
                ComponentUtils.getComponent(player).removeWearable();
                world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 1.05F, (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F);

                //Sync
                ComponentUtils.sync(player);
            }
            ((ServerPlayerEntity)player).closeHandledScreen();
        }
    }

    public static void switchAbilitySlider(PlayerEntity player, boolean sliderValue)
    {
        TravelersBackpackInventory inv = ComponentUtils.getBackpackInv(player);
        inv.setAbility(sliderValue);
        inv.markDataDirty(ITravelersBackpackInventory.ABILITY_DATA, ITravelersBackpackInventory.TANKS_DATA);

        if(inv.getItemStack().getItem() == ModItems.CHICKEN_TRAVELERS_BACKPACK && inv.getLastTime() <= 0)
        {
            BackpackAbilities.ABILITIES.chickenAbility(player, true);
        }

        if(BackpackAbilities.isOnList(BackpackAbilities.ITEM_ABILITIES_REMOVAL_LIST, inv.getItemStack()) && !sliderValue)
        {
            BackpackAbilities.ABILITIES.abilityRemoval(inv.getItemStack(), player);
        }
    }

    public static void switchAbilitySliderBlockEntity(PlayerEntity player, BlockPos pos, boolean sliderValue)
    {
        if(player.getWorld().getBlockEntity(pos) instanceof TravelersBackpackBlockEntity blockEntity)
        {
            blockEntity.setAbility(sliderValue);
            blockEntity.markDirty();

            blockEntity.getWorld().updateNeighbors(pos, blockEntity.getCachedState().getBlock());

            if(blockEntity.getCachedState().getBlock() == ModBlocks.SPONGE_TRAVELERS_BACKPACK)
            {
                ((TravelersBackpackBlock)blockEntity.getCachedState().getBlock()).update(blockEntity.getWorld(), pos);
            }
        }
    }

    public static void sortBackpack(PlayerEntity player, byte screenID, byte button, boolean shiftPressed)
    {
        if(screenID == Reference.BLOCK_ENTITY_SCREEN_ID && player.currentScreenHandler instanceof TravelersBackpackBlockEntityScreenHandler screenHandler)
        {
            if(player.getWorld().getBlockEntity(screenHandler.inventory.getPosition()) instanceof TravelersBackpackBlockEntity blockEntity)
            {
                InventorySorter.selectSort(blockEntity, player, button, shiftPressed);
            }
        }

        else if(screenID == Reference.ITEM_SCREEN_ID)
        {
            if(player.currentScreenHandler instanceof TravelersBackpackItemScreenHandler screenHandler)
            {
                InventorySorter.selectSort(screenHandler.inventory, player, button, shiftPressed);
            }
        }

        else if(screenID == Reference.WEARABLE_SCREEN_ID)
        {
            InventorySorter.selectSort(ComponentUtils.getBackpackInv(player), player, button, shiftPressed);
        }
    }

    public static void toggleSleepingBag(PlayerEntity player, BlockPos pos)
    {
        World world = player.getWorld();

        if(world.getBlockEntity(pos) instanceof TravelersBackpackBlockEntity blockEntity)
        {
            if(!blockEntity.isSleepingBagDeployed())
            {
                if(blockEntity.deploySleepingBag(world, pos))
                {
                    if(!world.isClient)
                    {
                        ((ServerPlayerEntity)player).closeHandledScreen();
                    }
                }
                else
                {
                    if(!world.isClient) player.sendMessage(Text.translatable(Reference.DEPLOY), false);
                }
            }
            else
            {
                blockEntity.removeSleepingBag(world);
            }
            if(!world.isClient)
            {
                ((ServerPlayerEntity)player).closeHandledScreen();
            }
        }
    }

    public static void emptyTank(double tankType, PlayerEntity player, World world, byte screenID)
    {
        ITravelersBackpackInventory inv = null;

        if(screenID == Reference.WEARABLE_SCREEN_ID) inv = ComponentUtils.getBackpackInv(player);
        if(screenID == Reference.ITEM_SCREEN_ID) inv = ((TravelersBackpackItemScreenHandler)player.currentScreenHandler).inventory;
        if(screenID == Reference.BLOCK_ENTITY_SCREEN_ID) inv = ((TravelersBackpackBlockEntityScreenHandler)player.currentScreenHandler).inventory;

        if(inv == null) return;

        SingleVariantStorage<FluidVariant> tank = tankType == 1D ? inv.getLeftTank() : inv.getRightTank();
        if(!world.isClient)
        {
            world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        tank.variant = FluidVariant.blank();
        tank.amount = 0;

        if(screenID == Reference.BLOCK_ENTITY_SCREEN_ID) inv.markDirty();
        else inv.markDataDirty(ITravelersBackpackInventory.TANKS_DATA);
    }

    public static boolean setFluidEffect(World world, PlayerEntity player, SingleVariantStorage<FluidVariant> tank)
    {
        boolean done = false;

        if(EffectFluidRegistry.hasExecutableEffects(tank, world, player))
        {
            done = EffectFluidRegistry.executeEffects(tank, player, world);
        }
        return done;
    }

    public static void switchHoseMode(PlayerEntity player, double scrollDelta)
    {
        ItemStack hose = player.getMainHandStack();

        if(hose.getItem() instanceof HoseItem)
        {
            if(hose.getNbt() != null)
            {
                int mode = HoseItem.getHoseMode(hose);

                if(scrollDelta > 0)
                {
                    mode = mode + 1;

                    if(mode == 4)
                    {
                        mode = 1;
                    }
                }

                else if(scrollDelta < 0)
                {
                    mode = mode - 1;

                    if(mode == 0)
                    {
                        mode = 3;
                    }
                }
                hose.getNbt().putInt("Mode", mode);
            }
        }
    }

    public static void toggleHoseTank(PlayerEntity player)
    {
        ItemStack hose = player.getMainHandStack();

        if(hose.getItem() instanceof HoseItem)
        {
            if(hose.getNbt() != null)
            {
                int tank = HoseItem.getHoseTank(hose);

                if(tank == 1)
                {
                    tank = 2;
                }
                else
                {
                    tank = 1;
                }

                hose.getNbt().putInt("Tank", tank);
            }
        }
    }
}