package net.satisfy.camping.init;

import net.satisfy.camping.TravelersBackpack;
import net.satisfy.camping.datagen.loot.LootItemHasColorCondition;
import net.satisfy.camping.datagen.loot.LootItemHasSleepingBagColorCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModLootConditions
{
    public static LootConditionType HAS_COLOR;
    public static LootConditionType HAS_SLEEPING_BAG_COLOR;

    public static void registerLootConditions()
    {
        HAS_COLOR = Registry.register(Registries.LOOT_CONDITION_TYPE, new Identifier(TravelersBackpack.MODID, "has_color"), new LootConditionType(new LootItemHasColorCondition.ConditionSerializer()));
        HAS_SLEEPING_BAG_COLOR = Registry.register(Registries.LOOT_CONDITION_TYPE, new Identifier(TravelersBackpack.MODID, "has_sleeping_bag_color"), new LootConditionType(new LootItemHasSleepingBagColorCondition.ConditionSerializer()));
    }
}
