package net.satisfy.camping.component;

import net.satisfy.camping.inventory.TravelersBackpackInventory;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import net.minecraft.item.ItemStack;

public interface ITravelersBackpackComponent extends PlayerComponent<Component>, AutoSyncedComponent
{
    boolean hasWearable();

    ItemStack getWearable();

    void setWearable(ItemStack stack);

    void removeWearable();

    TravelersBackpackInventory getInventory();

    void setContents(ItemStack stack);

    void sync();
}