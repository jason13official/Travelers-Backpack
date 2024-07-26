package net.satisfy.camping.handlers;

import net.satisfy.camping.blocks.SleepingBagBlock;
import net.satisfy.camping.config.TravelersBackpackConfig;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;

public class SleepHandler
{
    public static void registerListener()
    {
        EntitySleepEvents.ALLOW_SETTING_SPAWN.register((player, sleepingPos) ->
                !(!player.getWorld().isClient && player.getWorld().getBlockState(sleepingPos).getBlock() instanceof SleepingBagBlock && !TravelersBackpackConfig.getConfig().backpackSettings.enableSleepingBagSpawnPoint));
    }
}