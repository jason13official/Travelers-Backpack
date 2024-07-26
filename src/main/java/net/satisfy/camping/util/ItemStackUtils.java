package net.satisfy.camping.util;

import net.satisfy.camping.inventory.ITravelersBackpackInventory;
import net.satisfy.camping.items.HoseItem;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemStackUtils
{
    public static ItemStack decrStackSize(ITravelersBackpackInventory inventory, int index, int count)
    {
        return Inventories.splitStack(inventory.getFluidSlotsInventory().getStacks(), index, count);
    }

    public static boolean canCombine(ItemStack stack1, ItemStack stack2)
    {
        //Hose patch
        if(stack1.getItem() instanceof HoseItem && stack1.isOf(stack2.getItem())) return true;

        return ItemStack.areItemsEqual(stack1, stack2) && areTagsEqual(stack1, stack2);
    }

    public static boolean areTagsEqual(ItemStack stack1, ItemStack stack2)
    {
        if (stack1.isEmpty() && stack2.isEmpty()) {
            return true;
        } else if (!stack1.isEmpty() && !stack2.isEmpty()) {
            if (stack1.getNbt() == null && stack2.getNbt() != null) {
                return false;
            } else {

                NbtCompound copy1 = stack1.getNbt() == null ? null : stack1.getNbt().copy();
                NbtCompound copy2 = stack2.getNbt() == null ? null : stack2.getNbt().copy();

                if(copy1 != null)
                {
                    if(copy1.contains("Damage"))
                    {
                        copy1.remove("Damage");
                    }
                }

                if(copy2 != null)
                {
                    if(copy2.contains("Damage"))
                    {
                        copy2.remove("Damage");
                    }
                }

                return (stack1.getNbt() == null || copy1.equals(copy2));
            }
        } else {
            return false;
        }
    }
}