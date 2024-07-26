package net.satisfy.camping.handlers;

import net.satisfy.camping.config.TravelersBackpackConfig;
import net.satisfy.camping.items.TravelersBackpackItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EntityItemHandler
{
    public static void registerListeners()
    {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) ->
        {
            if(!(entity instanceof ItemEntity itemEntity) || !TravelersBackpackConfig.getConfig().backpackSettings.invulnerableBackpack) return;

            if(itemEntity.getStack().getItem() instanceof TravelersBackpackItem)
            {
                itemEntity.setCovetedItem();
                entity.setInvulnerable(true);
            }
        });

        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) ->
        {
            if(!(entity instanceof ItemEntity itemEntity) || !TravelersBackpackConfig.getConfig().backpackSettings.voidProtection) return;

            //Void protection
            if(itemEntity.getStack().getItem() instanceof TravelersBackpackItem)
            {
                if(world.isClient) return;

                BlockPos entityPos = itemEntity.getBlockPos();
                Vec3d entityPosCentered = entityPos.toCenterPos();
                double y = entityPosCentered.y;

                if(y < world.getBottomY())
                {
                    ItemEntity protectedItemEntity = new ItemEntity(world, entityPosCentered.x, y, entityPosCentered.z, itemEntity.getStack().copy());

                    protectedItemEntity.setNoGravity(true);
                    protectedItemEntity.setToDefaultPickupDelay();

                    y = world.getBottomY();

                    for(double i = y; i < world.getHeight(); i++)
                    {
                        if(world.getBlockState(BlockPos.ofFloored(new Vec3d(entityPosCentered.x, i, entityPosCentered.z))).isReplaceable())
                        {
                            y = i;
                            break;
                        }
                    }

                    protectedItemEntity.setPosition(entityPosCentered.x, y, entityPosCentered.z);
                    protectedItemEntity.setVelocity(0, 0, 0);

                    world.spawnEntity(protectedItemEntity);
                }
            }
        });
    }
}