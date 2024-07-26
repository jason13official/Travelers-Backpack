package net.satisfy.camping.fluids.effects;

import net.satisfy.camping.fluids.EffectFluid;
import net.satisfy.camping.init.ModFluids;
import net.satisfy.camping.util.FluidUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.PotionUtil;
import net.minecraft.world.World;

public class PotionEffect extends EffectFluid
{
    public PotionEffect()
    {
        super("camping:potion", ModFluids.POTION_STILL, FluidConstants.BOTTLE);
    }

    @Override
    public void affectDrinker(StorageView<FluidVariant> variant, World world, Entity entity)
    {
        if(!world.isClient && entity instanceof PlayerEntity player)
        {
            for(StatusEffectInstance effectinstance : PotionUtil.getPotionEffects(FluidUtils.getItemStackFromFluidStack(variant.getResource())))
            {
                if(effectinstance.getEffectType().isInstant())
                {
                    effectinstance.getEffectType().applyInstantEffect(player, player, player, effectinstance.getAmplifier(), 1.0D);
                }
                else
                {
                    player.addStatusEffect(new StatusEffectInstance(effectinstance));
                }
            }
        }
    }

    @Override
    public boolean canExecuteEffect(StorageView<FluidVariant> variant, World world, Entity entity)
    {
        return variant.getAmount() >= amountRequired;
    }
}