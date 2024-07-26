package net.satisfy.camping.fluids.effects;

import net.satisfy.camping.fluids.EffectFluid;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class WaterEffect extends EffectFluid
{
    public WaterEffect()
    {
        super("minecraft:water", Fluids.WATER, FluidConstants.BUCKET);
    }

    @Override
    public void affectDrinker(StorageView<FluidVariant> fluidStack, World world, Entity entity)
    {
        if(entity instanceof PlayerEntity player)
        {
            RegistryEntry<Biome> biome = world.getBiome(player.getBlockPos());
            int duration = 7 * 20;

            if(biome.value().getTemperature() >= 2.0F)
            {
                if(player.isOnFire())
                {
                    player.extinguish();
                }
                else
                {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration, 0));
                }
            }
        }
    }

    @Override
    public boolean canExecuteEffect(StorageView<FluidVariant> stack, World world, Entity entity)
    {
        return stack.getAmount() >= amountRequired;
    }
}