package com.tiviacz.travelersbackpack.compat.curios;

import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.blocks.TravelersBackpackBlock;
import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.capability.ITravelersBackpack;
import com.tiviacz.travelersbackpack.inventory.TravelersBackpackContainer;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class TravelersBackpackCurios
{
    public static ICurio createBackpackProvider()
    {
        return new TravelersBackpackCurio(ItemStack.EMPTY);
    }

    public static Optional<ImmutableTriple<String, Integer, ItemStack>> getCurioTravelersBackpack(LivingEntity livingEntity)
    {
        Predicate<ItemStack> backpack = stack -> stack.getItem() instanceof TravelersBackpackItem;
        return CuriosApi.getCuriosHelper().findEquippedCurio(backpack, livingEntity);
    }

    public static ItemStack getCurioTravelersBackpackStack(Player player)
    {
        if(getCurioTravelersBackpack(player).isPresent())
        {
            return getCurioTravelersBackpack(player).get().getRight();
        }
        return ItemStack.EMPTY;
    }

    public static TravelersBackpackContainer getCurioTravelersBackpackInventory(Player player)
    {
        TravelersBackpackContainer curioContainer = CapabilityUtils.getCapability(player).map(ITravelersBackpack::getContainer).orElse(null);

        if(curioContainer.getItemStack() != getCurioTravelersBackpackStack(player))
        {
            curioContainer.setStack(getCurioTravelersBackpackStack(player));
            curioContainer.loadAllData(getCurioTravelersBackpackStack(player).getOrCreateTag());
        }

        return curioContainer;
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean renderCurioLayer(AbstractClientPlayer clientPlayer)
    {
        if(TravelersBackpackCurios.getCurioTravelersBackpack(clientPlayer).isPresent())
        {
            return CuriosApi.getCuriosInventory(clientPlayer).map(curios -> curios.findFirstCurio(stack -> stack.getItem() instanceof TravelersBackpackItem)
                    .map(result -> result.slotContext().visible()).orElse(false)).orElse(false);
        }
        return false;
    }

    public static void rightClickUnequip(Player player, ItemStack stack)
    {
        if(TravelersBackpack.enableCurios())
        {
            int backpackSlot = CuriosApi.getCuriosHelper().findFirstCurio(player, p -> ItemStack.isSameItemSameTags(p, stack)).get().slotContext().index();

            CuriosApi.getCuriosHelper().getCuriosHandler(player).map(iCuriosItemHandler -> iCuriosItemHandler.getStacksHandler(SlotTypePreset.BACK.getIdentifier()))
                    .ifPresent(iCurioStacksHandler -> iCurioStacksHandler.get().getStacks().setStackInSlot(backpackSlot, ItemStack.EMPTY));
        }
    }

    public static boolean rightClickEquip(Player player, ItemStack stack, boolean simulate)
    {
        if(CuriosApi.getCurio(stack).isPresent() && CuriosApi.getCurio(stack).resolve().isPresent())
        {
            ICurio curio = CuriosApi.getCurio(stack).resolve().get();
            LazyOptional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosInventory(player);

            if(curiosHandler.isPresent() && curiosHandler.resolve().isPresent())
            {
                int index = -1;
                boolean isEmptySlot = false;

                ICurioStacksHandler curioHandler = curiosHandler.resolve().get().getCurios().get("back");

                for(int i = 0; i < curioHandler.getSlots(); i++)
                {
                    if(curioHandler.getStacks().getStackInSlot(i).isEmpty())
                    {
                        index = i;
                        isEmptySlot = true;
                    }
                }

                if(!isEmptySlot) return false;
                NonNullList<Boolean> renderStates = curioHandler.getRenders();

                SlotContext slotContext = new SlotContext(curioHandler.getIdentifier(), player, index, false, renderStates.size() > index && renderStates.get(index));

                if(curio.canEquip(slotContext))
                {
                    if(simulate) return true;
                    curioHandler.getStacks().setStackInSlot(index, stack.copy());
                    curio.onEquipFromUse(slotContext);

                    //Sound
                    player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 1.0F, (1.0F + (player.level().random.nextFloat() - player.level().random.nextFloat()) * 0.2F) * 0.7F);

                    //Sync
                    CapabilityUtils.synchronise(player);
                    CapabilityUtils.synchroniseToOthers(player);
                    return true;
                }
            }
        }
        return false;
    }
}