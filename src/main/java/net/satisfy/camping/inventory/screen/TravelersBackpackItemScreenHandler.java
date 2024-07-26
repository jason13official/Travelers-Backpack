package net.satisfy.camping.inventory.screen;

import net.satisfy.camping.component.ComponentUtils;
import net.satisfy.camping.init.ModScreenHandlerTypes;
import net.satisfy.camping.inventory.ITravelersBackpackInventory;
import net.satisfy.camping.inventory.TravelersBackpackInventory;
import net.satisfy.camping.inventory.screen.slot.DisabledSlot;
import net.satisfy.camping.items.TravelersBackpackItem;
import net.satisfy.camping.util.Reference;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Objects;

public class TravelersBackpackItemScreenHandler extends TravelersBackpackBaseScreenHandler
{
    public TravelersBackpackItemScreenHandler(int windowID, PlayerInventory playerInventory, PacketByteBuf data)
    {
        this(windowID, playerInventory, createInventory(playerInventory, data));
    }

    public TravelersBackpackItemScreenHandler(int windowID, PlayerInventory playerInventory, ITravelersBackpackInventory inventory)
    {
        super(ModScreenHandlerTypes.TRAVELERS_BACKPACK_ITEM, windowID, playerInventory, inventory);
    }

    private static TravelersBackpackInventory createInventory(final PlayerInventory playerInventory, final PacketByteBuf data)
    {
        Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
        Objects.requireNonNull(data, "data cannot be null");

        final ItemStack stack; //= data.readItemStack(); //Get ItemStack from hand or capability to avoid sending a lot of information by packetBuffer
        final byte screenID = data.readByte();

        if(screenID == Reference.ITEM_SCREEN_ID)
        {
            stack = playerInventory.player.getEquippedStack(EquipmentSlot.MAINHAND);
        }
        else
        {
            if(data.writerIndex() == 94)
            {
                final int entityId = data.readInt();
                stack = ComponentUtils.getWearingBackpack((PlayerEntity)playerInventory.player.getWorld().getEntityById(entityId));

                if(stack.getItem() instanceof TravelersBackpackItem)
                {
                    return ComponentUtils.getBackpackInv((PlayerEntity)playerInventory.player.getWorld().getEntityById(entityId));
                }
            }
            else
            {
                stack = ComponentUtils.getWearingBackpack(playerInventory.player);
            }
        }

        if(stack.getItem() instanceof TravelersBackpackItem)
        {
            if(screenID == Reference.WEARABLE_SCREEN_ID)
            {
                return ComponentUtils.getBackpackInv(playerInventory.player);
            }
            else if(screenID == Reference.ITEM_SCREEN_ID)
            {
                return new TravelersBackpackInventory(stack, playerInventory.player, screenID);
            }
        }
        throw new IllegalStateException("ItemStack is not correct! " + stack);
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player)
    {
        if(inventory.getScreenID() == Reference.ITEM_SCREEN_ID && actionType == SlotActionType.SWAP)
        {
            final ItemStack stack = player.getInventory().getStack(button);
            final ItemStack currentItem = player.getInventory().getMainHandStack();

            if(!currentItem.isEmpty() && stack == currentItem)
            {
                return;
            }
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public void addPlayerInventoryAndHotbar(PlayerInventory playerInv, int currentItemIndex)
    {
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(playerInv, x + y * 9 + 9, 44 + x*18, (71 + this.inventory.getYOffset()) + y*18));
            }
        }

        for(int x = 0; x < 9; x++)
        {
            if(x == currentItemIndex && this.inventory.getScreenID() == Reference.ITEM_SCREEN_ID)
            {
                this.addSlot(new DisabledSlot(playerInv, x, 44 + x*18, 129 + this.inventory.getYOffset()));
            }
            else
            {
                this.addSlot(new Slot(playerInv, x, 44 + x*18, 129 + this.inventory.getYOffset()));
            }
        }
    }
}