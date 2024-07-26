package net.satisfy.camping.fluids.effects;

import net.satisfy.camping.fluids.EffectFluid;
import net.satisfy.camping.init.ModFluids;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class MilkEffect extends EffectFluid
{
    public MilkEffect()
    {
        super("camping:milk", ModFluids.MILK_STILL.getStill(), FluidConstants.BUCKET);
    }

    @Override
    public void affectDrinker(StorageView<FluidVariant> variant, World world, Entity entity)
    {
        if(entity instanceof PlayerEntity player)
        {
            player.clearStatusEffects();
        }
    }

    @Override
    public boolean canExecuteEffect(StorageView<FluidVariant> variant, World world, Entity entity)
    {
        return variant.getAmount() >= amountRequired;
    }
}