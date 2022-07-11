package com.tiviacz.travelersbackpack.common;

import com.tiviacz.travelersbackpack.init.ModBlocks;
import com.tiviacz.travelersbackpack.init.ModItems;
import com.tiviacz.travelersbackpack.inventory.ITravelersBackpackInventory;
import com.tiviacz.travelersbackpack.inventory.TravelersBackpackInventory;
import com.tiviacz.travelersbackpack.tileentity.TravelersBackpackTileEntity;
import com.tiviacz.travelersbackpack.util.BackpackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;

public class BackpackAbilities
{
    public static final BackpackAbilities ABILITIES = new BackpackAbilities();

    /**
     * Called in TravelersBackpackTileEntity#Tick and ForgeEventHandler#playerTick method to enable abilities
     */
    public void abilityTick(@Nullable ItemStack stack, @Nullable PlayerEntity player, @Nullable TravelersBackpackTileEntity tile)
    {
        if(tile == null) //WEARABLE ABILITIES
        {
            if(stack.getItem() == ModItems.BAT_TRAVELERS_BACKPACK.get())
            {
                batAbility(player);
            }

            if(stack.getItem() == ModItems.CHICKEN_TRAVELERS_BACKPACK.get())
            {
                chickenAbility(player, false);
            }

            if(stack.getItem() == ModItems.EMERALD_TRAVELERS_BACKPACK.get())
            {
                emeraldAbility(player, null);
            }

            if(stack.getItem() == ModItems.CACTUS_TRAVELERS_BACKPACK.get())
            {
                cactusAbility(player, null);
            }

            if(stack.getItem() == ModItems.DRAGON_TRAVELERS_BACKPACK.get())
            {
                dragonAbility(player);
            }

            if(stack.getItem() == ModItems.BLAZE_TRAVELERS_BACKPACK.get())
            {
                blazeAbility(player);
            }

            if(stack.getItem() == ModItems.MAGMA_CUBE_TRAVELERS_BACKPACK.get())
            {
                magmaCubeAbility(player);
            }

            if(stack.getItem() == ModItems.WITHER_TRAVELERS_BACKPACK.get())
            {
                witherAbility(player);
            }

            if(stack.getItem() == ModItems.OCELOT_TRAVELERS_BACKPACK.get())
            {
                ocelotAbility(player);
            }

            if(stack.getItem() == ModItems.SQUID_TRAVELERS_BACKPACK.get())
            {
                squidAbility(player);
            }
        }
        else //TILE ABILITIES
        {
            Item item = tile.getItemStack().getItem();

            if(item == ModItems.CACTUS_TRAVELERS_BACKPACK.get())
            {
                cactusAbility(null, tile);
            }
        }
    }

    /**
     * Called in TravelersBackpackBlock#animateTick method to enable visual only abilities for TravelersBackpackTileEntity
     */

    public void animateTick(@Nullable TravelersBackpackTileEntity tile, BlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if(tile != null && tile.getAbilityValue())
        {
            Block block = stateIn.getBlock();

            if(block == ModBlocks.EMERALD_TRAVELERS_BACKPACK.get())
            {
                emeraldAbility(null, tile);
            }

            if(block == ModBlocks.BOOKSHELF_TRAVELERS_BACKPACK.get())
            {
                bookshelfAbility(null, tile);
            }

            if(block == ModBlocks.SPONGE_TRAVELERS_BACKPACK.get())
            {
                spongeAbility(null, tile);
            }
        }
    }

    public void emeraldAbility(@Nullable PlayerEntity player, @Nullable TravelersBackpackTileEntity tile)
    {
        World world = player == null ? tile.getLevel() : player.level;

        if(player == null || world.random.nextInt(10) == 1)
        {
            float f = world.random.nextFloat() * (float) Math.PI * 2.0F;
            float f1 = world.random.nextFloat() * 0.5F + 0.5F;
            float f2 = MathHelper.sin(f) * 0.5F * f1;
            float f3 = MathHelper.cos(f) * 0.5F * f1;
            world.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    player == null ? tile.getBlockPos().getX() + f2 + 0.5F : player.position().x + f2,
                    player == null ? tile.getBlockPos().getY() + world.random.nextFloat() : player.getBoundingBox().minY + world.random.nextFloat() + 0.5F,
                    player == null ? tile.getBlockPos().getZ() + f3 + 0.5F : player.position().z + f3, (double)(float)Math.pow(2.0D, (world.random.nextInt(169) - 12) / 12.0D) / 24.0D, -1.0D, 0.0D);
        }
    }

    public void bookshelfAbility(@Nullable PlayerEntity player, @Nullable TravelersBackpackTileEntity tile)
    {
        BlockPos enchanting = BackpackUtils.findBlock3D(tile.getLevel(), tile.getBlockPos().getX(), tile.getBlockPos().getY(), tile.getBlockPos().getZ(), Blocks.ENCHANTING_TABLE, 2, 2);

        if(enchanting != null)
        {
            if(!tile.getLevel().isEmptyBlock(new BlockPos((enchanting.getX() - tile.getBlockPos().getX()) / 2 + tile.getBlockPos().getX(), enchanting.getY(), (enchanting.getZ() - tile.getBlockPos().getZ()) / 2 + tile.getBlockPos().getZ())))
            {
                return;
            }

            for(int o = 0; o < 4; o++)
            {
                tile.getLevel().addParticle(ParticleTypes.ENCHANT, enchanting.getX() + 0.5D, enchanting.getY() + 2.0D, enchanting.getZ() + 0.5D,
                        ((tile.getBlockPos().getX() - enchanting.getX()) + tile.getLevel().random.nextFloat()) - 0.5D,
                        ((tile.getBlockPos().getY() - enchanting.getY()) - tile.getLevel().random.nextFloat() - 1.0F),
                        ((tile.getBlockPos().getZ() - enchanting.getZ()) + tile.getLevel().random.nextFloat()) - 0.5D);
            }
        }
    }

    public void spongeAbility(@Nullable PlayerEntity player, @Nullable TravelersBackpackTileEntity tile)
    {
        if(!tile.getLeftTank().isEmpty() && !tile.getRightTank().isEmpty())
        {
            if(tile.getLeftTank().getFluid().getFluid().isSame(Fluids.WATER) && tile.getRightTank().getFluid().getFluid().isSame(Fluids.WATER))
            {
                if(tile.getLeftTank().getFluidAmount() == tile.getLeftTank().getCapacity() && tile.getRightTank().getFluidAmount() == tile.getRightTank().getCapacity())
                {
                    float f = tile.getLevel().random.nextFloat() * (float) Math.PI * 2.0F;
                    float f1 = tile.getLevel().random.nextFloat() * 0.5F + 0.5F;
                    float f2 = MathHelper.sin(f) * 0.5F * f1;
                    float f3 = MathHelper.cos(f) * 0.5F * f1;
                    tile.getLevel().addParticle(ParticleTypes.SPLASH,
                            tile.getBlockPos().getX() + f2 + 0.5F,
                            tile.getBlockPos().getY() + tile.getLevel().random.nextFloat(),
                            tile.getBlockPos().getZ() + f3 + 0.5F, (double)(float)Math.pow(2.0D, (tile.getLevel().random.nextInt(169) - 12) / 12.0D) / 24.0D, -1.0D, 0.0D);
                }
            }
        }
    }

    public void chickenAbility(@Nullable PlayerEntity player, boolean firstSwitch)
    {
        TravelersBackpackInventory inv = BackpackUtils.getCurrentInventory(player);

        if(firstSwitch)
        {
            if(inv.getLastTime() <= 0)
            {
                inv.setLastTime((200 + 10 * player.level.random.nextInt(10)) * 20);
                inv.markLastTimeDirty();
            }
        }

        if(inv.getLastTime() <= 0)
        {
            player.level.playSound(player, player.blockPosition(), SoundEvents.CHICKEN_EGG, SoundCategory.AMBIENT, 1.0F, (player.level.random.nextFloat() - player.level.random.nextFloat()) * 0.3F + 1.0F);
            player.spawnAtLocation(Items.EGG);

            inv.setLastTime((200 + 10 * player.level.random.nextInt(10)) * 20);
            inv.markLastTimeDirty();
        }
    }

    public void cactusAbility(@Nullable PlayerEntity player, @Nullable TravelersBackpackTileEntity tile)
    {
        ITravelersBackpackInventory inv = player == null ? tile : BackpackUtils.getCurrentInventory(player);
        FluidTank leftTank = inv.getLeftTank();
        FluidTank rightTank = inv.getRightTank();

        int drops = 0;

        if(player != null && player.isInWater())
        {
            drops += 2;
        }

        if(isUnderRain(tile == null ? player.blockPosition() : tile.getBlockPos(), tile == null ? player.level : tile.getLevel()))
        {
            drops += 1;
        }

        FluidStack water = new FluidStack(Fluids.WATER, drops);

        if(inv.getLastTime() <= 0 && drops > 0)
        {
            inv.setLastTime(5);

            if(leftTank.isEmpty() || leftTank.getFluid().isFluidEqual(water))
            {
                leftTank.fill(water, IFluidHandler.FluidAction.EXECUTE);
            }

            if(rightTank.isEmpty() || rightTank.getFluid().isFluidEqual(water))
            {
                rightTank.fill(water, IFluidHandler.FluidAction.EXECUTE);
            }

            inv.markTankDirty();
        }
    }

    public static boolean creeperAbility(LivingDeathEvent event)
    {
        if(event.getEntity() instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity)event.getEntity();

            TravelersBackpackInventory inv = BackpackUtils.getCurrentInventory(player);

            if(player.isDeadOrDying() && inv != null && inv.getItemStack().getItem() == ModItems.CREEPER_TRAVELERS_BACKPACK.get() && inv.getAbilityValue() && inv.getLastTime() <= 0)
            {
                player.setHealth(1.0F);
                player.removeAllEffects();
                player.addEffect(new EffectInstance(Effects.REGENERATION, 450, 1));
                player.addEffect(new EffectInstance(Effects.ABSORPTION, 100, 1));
                player.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 400, 0));
                player.level.explode(player, DamageSource.playerAttack(player), null, player.getRandomX(0.5F), player.getY(), player.getRandomZ(0.5F), 3.0F, false, Explosion.Mode.NONE);
                player.level.playSound(null, player.blockPosition(), SoundEvents.CREEPER_PRIMED, SoundCategory.AMBIENT, 1.2F, 0.5F);

                inv.setLastTime((200 + 10 * player.level.random.nextInt(10)) * 50);
                inv.markLastTimeDirty();
                event.setCanceled(true);
                return true;
            }
        }
        return false;
    }

    public void dragonAbility(@Nullable PlayerEntity player)
    {
        magmaCubeAbility(player);
        squidAbility(player);

        player.addEffect(new EffectInstance(Effects.REGENERATION, 210, 0, false, false, true));
        player.addEffect(new EffectInstance(Effects.DAMAGE_BOOST, 210, 0, false, false, true));
    }

    public void blazeAbility(@Nullable PlayerEntity player)
    {
        if(player.fallDistance >= 3.0F)
        {
            for(int i = 0; i < 4; ++i)
            {
                player.level.addParticle(ParticleTypes.LARGE_SMOKE, player.getRandomX(0.5D), player.getRandomY(), player.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
            }

            player.fallDistance = 0.0F;
        }
    }

    public void magmaCubeAbility(@Nullable PlayerEntity player)
    {
        player.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 210, 0, false, false, true));
    }

    public void witherAbility(@Nullable PlayerEntity player)
    {
        if(player.getEffect(Effects.WITHER) != null)
        {
            player.removeEffect(Effects.WITHER);
        }
    }

    public void batAbility(@Nullable PlayerEntity player)
    {
        player.addEffect(new EffectInstance(Effects.NIGHT_VISION, 210, 0, false, false, true));
    }

    private static final EntityPredicate OCELOT_ABILITY_PREDICATE = (new EntityPredicate()).range(6.0D);

    public void ocelotAbility(@Nullable PlayerEntity player)
    {
        if(player.level.getNearestEntity(MobEntity.class, OCELOT_ABILITY_PREDICATE, player, player.getX(), player.getY(), player.getZ(), player.getBoundingBox().inflate(6.0D, 2.0D, 6.0D)) != null)
        {
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 20, 0, false, false, true));
        }
    }

    public void squidAbility(@Nullable PlayerEntity player)
    {
        if(player.isInWater())
        {
            player.addEffect(new EffectInstance(Effects.WATER_BREATHING, 210, 0, false, false, true));
            batAbility(player);
        }
    }

    private boolean isUnderRain(BlockPos pos, World world)
    {
        return world.canSeeSky(pos) && world.isRaining();
    }

    public static boolean isOnList(Item[] list, ItemStack stackToCheck)
    {
        return Arrays.stream(list).anyMatch(s -> s == stackToCheck.getItem());
    }

    public static final Item[] ALL_ABILITIES_LIST = {

            ModItems.EMERALD_TRAVELERS_BACKPACK.get(), //#TODO niy
            ModItems.REDSTONE_TRAVELERS_BACKPACK.get(),

            ModItems.BOOKSHELF_TRAVELERS_BACKPACK.get(),
            //ModItems.END_TRAVELERS_BACKPACK.get(),
            //ModItems.NETHER_TRAVELERS_BACKPACK.get(),
            //ModItems.SANDSTONE_TRAVELERS_BACKPACK.get(),
            //ModItems.SNOW_TRAVELERS_BACKPACK.get(),
            ModItems.SPONGE_TRAVELERS_BACKPACK.get(),

            //ModItems.CAKE_TRAVELERS_BACKPACK.get(),

            ModItems.CACTUS_TRAVELERS_BACKPACK.get(),
           // ModItems.HAY_TRAVELERS_BACKPACK.get(),
           // ModItems.MELON_TRAVELERS_BACKPACK.get(),
           // ModItems.PUMPKIN_TRAVELERS_BACKPACK.get(),

            ModItems.CREEPER_TRAVELERS_BACKPACK.get(),
            ModItems.DRAGON_TRAVELERS_BACKPACK.get(),
           // ModItems.ENDERMAN_TRAVELERS_BACKPACK.get(),
            ModItems.BLAZE_TRAVELERS_BACKPACK.get(),
           // ModItems.GHAST_TRAVELERS_BACKPACK.get(),
            ModItems.MAGMA_CUBE_TRAVELERS_BACKPACK.get(),
          //  ModItems.SKELETON_TRAVELERS_BACKPACK.get(),
          //  ModItems.SPIDER_TRAVELERS_BACKPACK.get(),
            ModItems.WITHER_TRAVELERS_BACKPACK.get(),

            ModItems.BAT_TRAVELERS_BACKPACK.get(),
           // ModItems.BEE_TRAVELERS_BACKPACK.get(),
           // ModItems.WOLF_TRAVELERS_BACKPACK.get(),
           // ModItems.FOX_TRAVELERS_BACKPACK.get(),
            ModItems.OCELOT_TRAVELERS_BACKPACK.get(),
           // ModItems.HORSE_TRAVELERS_BACKPACK.get(),
           // ModItems.COW_TRAVELERS_BACKPACK.get(),
          //  ModItems.PIG_TRAVELERS_BACKPACK.get(),
          //  ModItems.SHEEP_TRAVELERS_BACKPACK.get(),
            ModItems.CHICKEN_TRAVELERS_BACKPACK.get(),
            ModItems.SQUID_TRAVELERS_BACKPACK.get(),
          //  ModItems.IRON_GOLEM_TRAVELERS_BACKPACK.get()
        };

    public static final Item[] ITEM_ABILITIES_LIST = {

            ModItems.EMERALD_TRAVELERS_BACKPACK.get(),

            //ModItems.END_TRAVELERS_BACKPACK.get(),
            //ModItems.NETHER_TRAVELERS_BACKPACK.get(),
            //ModItems.SANDSTONE_TRAVELERS_BACKPACK.get(),
            //ModItems.SNOW_TRAVELERS_BACKPACK.get(),

            //ModItems.CAKE_TRAVELERS_BACKPACK.get(),

            ModItems.CACTUS_TRAVELERS_BACKPACK.get(),
            //ModItems.HAY_TRAVELERS_BACKPACK.get(),
            //ModItems.MELON_TRAVELERS_BACKPACK.get(),
            //ModItems.PUMPKIN_TRAVELERS_BACKPACK.get(),

            ModItems.CREEPER_TRAVELERS_BACKPACK.get(),
            ModItems.DRAGON_TRAVELERS_BACKPACK.get(),
            //ModItems.ENDERMAN_TRAVELERS_BACKPACK.get(),
            ModItems.BLAZE_TRAVELERS_BACKPACK.get(),
            //ModItems.GHAST_TRAVELERS_BACKPACK.get(),
            ModItems.MAGMA_CUBE_TRAVELERS_BACKPACK.get(),
            //ModItems.SKELETON_TRAVELERS_BACKPACK.get(),
            //ModItems.SPIDER_TRAVELERS_BACKPACK.get(),
            ModItems.WITHER_TRAVELERS_BACKPACK.get(),

            ModItems.BAT_TRAVELERS_BACKPACK.get(),
           // ModItems.BEE_TRAVELERS_BACKPACK.get(),
           // ModItems.WOLF_TRAVELERS_BACKPACK.get(),
            //ModItems.FOX_TRAVELERS_BACKPACK.get(),
            ModItems.OCELOT_TRAVELERS_BACKPACK.get(),
            //ModItems.HORSE_TRAVELERS_BACKPACK.get(),
            //ModItems.COW_TRAVELERS_BACKPACK.get(),
            //ModItems.PIG_TRAVELERS_BACKPACK.get(),
            //ModItems.SHEEP_TRAVELERS_BACKPACK.get(),
            ModItems.CHICKEN_TRAVELERS_BACKPACK.get(),
            ModItems.SQUID_TRAVELERS_BACKPACK.get(),
            //ModItems.IRON_GOLEM_TRAVELERS_BACKPACK.get()
    };

    public static final Item[] ITEM_TIMER_ABILITIES_LIST = {
            ModItems.CREEPER_TRAVELERS_BACKPACK.get(),

            ModItems.CHICKEN_TRAVELERS_BACKPACK.get()
    };

    public static final Item[] BLOCK_ABILITIES_LIST = {

            ModItems.EMERALD_TRAVELERS_BACKPACK.get(),
            ModItems.REDSTONE_TRAVELERS_BACKPACK.get(),

            ModItems.BOOKSHELF_TRAVELERS_BACKPACK.get(),
            //ModItems.END_TRAVELERS_BACKPACK.get(),
            //ModItems.NETHER_TRAVELERS_BACKPACK.get(),
            //ModItems.SANDSTONE_TRAVELERS_BACKPACK.get(),
            //ModItems.SNOW_TRAVELERS_BACKPACK.get(),
            ModItems.SPONGE_TRAVELERS_BACKPACK.get(),

            //ModItems.CAKE_TRAVELERS_BACKPACK.get(),

            ModItems.CACTUS_TRAVELERS_BACKPACK.get(),
            //ModItems.HAY_TRAVELERS_BACKPACK.get(),
            //ModItems.MELON_TRAVELERS_BACKPACK.get(),
           // ModItems.PUMPKIN_TRAVELERS_BACKPACK.get(),

           // ModItems.ENDERMAN_TRAVELERS_BACKPACK.get(),
           // ModItems.GHAST_TRAVELERS_BACKPACK.get(),
           // ModItems.SKELETON_TRAVELERS_BACKPACK.get(),
           // ModItems.SPIDER_TRAVELERS_BACKPACK.get(),

           // ModItems.BEE_TRAVELERS_BACKPACK.get(),
          //  ModItems.WOLF_TRAVELERS_BACKPACK.get(),
          //  ModItems.FOX_TRAVELERS_BACKPACK.get(),
          //  ModItems.HORSE_TRAVELERS_BACKPACK.get(),
          //  ModItems.COW_TRAVELERS_BACKPACK.get(),
           // ModItems.PIG_TRAVELERS_BACKPACK.get(),
           // ModItems.SHEEP_TRAVELERS_BACKPACK.get(),
           // ModItems.IRON_GOLEM_TRAVELERS_BACKPACK.get()
    };
}