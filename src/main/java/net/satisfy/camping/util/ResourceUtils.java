package net.satisfy.camping.util;

import net.satisfy.camping.TravelersBackpack;
import net.satisfy.camping.init.ModItems;
import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ResourceUtils
{
    public static final List<Identifier> TEXTURE_IDENTIFIERS = new ArrayList<>();
    public static final List<Identifier> SLEEPING_BAG_TEXTURE_RESOURCE_LOCATIONS = new ArrayList<>();

    //Store resource locations for backpacks, to avoid tick by tick new resource locations creation.
    //Then get each texture by index from ModItems#BACKPACKS.
    //Any new approach is appreciated D:

    public static Identifier getBackpackTexture(Item item)
    {
        return TEXTURE_IDENTIFIERS.get(ModItems.BACKPACKS.indexOf(item));
    }

    public static Identifier getSleepingBagTexture(int colorId)
    {
        return SLEEPING_BAG_TEXTURE_RESOURCE_LOCATIONS.get(colorId);
    }

    public static Identifier getDefaultSleepingBagTexture()
    {
        return SLEEPING_BAG_TEXTURE_RESOURCE_LOCATIONS.get(14);
    }

    public static void createTextureLocations()
    {
        TEXTURE_IDENTIFIERS.clear();

        for(String name : Reference.BACKPACK_NAMES)
        {
            Identifier res = new Identifier(TravelersBackpack.MODID, "textures/model/" + name.toLowerCase(Locale.ENGLISH) + ".png");
            TEXTURE_IDENTIFIERS.add(res);
        }
    }

    public static void createSleepingBagTextureLocations()
    {
        SLEEPING_BAG_TEXTURE_RESOURCE_LOCATIONS.clear();

        for(DyeColor color : DyeColor.values())
        {
            Identifier id = new Identifier(TravelersBackpack.MODID, "textures/model/bags/" + color.getName().toLowerCase(Locale.ENGLISH) + "_sleeping_bag" + ".png");
            SLEEPING_BAG_TEXTURE_RESOURCE_LOCATIONS.add(id);
        }
    }
}