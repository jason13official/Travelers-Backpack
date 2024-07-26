package net.satisfy.camping.common.recipes;

import com.google.gson.JsonObject;
import net.satisfy.camping.config.TravelersBackpackConfig;
import net.satisfy.camping.init.ModItems;
import net.satisfy.camping.init.ModRecipeSerializers;
import net.satisfy.camping.inventory.ITravelersBackpackInventory;
import net.satisfy.camping.inventory.SettingsManager;
import net.satisfy.camping.inventory.Tiers;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.SmithingTransformRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

public class BackpackUpgradeRecipe extends SmithingTransformRecipe
{
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;
    final ItemStack result;

    public BackpackUpgradeRecipe(Identifier id, Ingredient template, Ingredient base, Ingredient addition, ItemStack result)
    {
        super(id, template, base, addition, result);

        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager)
    {
        ItemStack itemstack = this.result.copy();
        NbtCompound nbtCompound = inventory.getStack(1).getNbt();

        if(nbtCompound != null)
        {
            nbtCompound = nbtCompound.copy();

            if(inventory.getStack(2).isOf(ModItems.CRAFTING_UPGRADE))
            {
                if(nbtCompound.contains(SettingsManager.CRAFTING_SETTINGS))
                {
                    if(nbtCompound.getByteArray(SettingsManager.CRAFTING_SETTINGS)[0] == (byte)0)
                    {
                        byte[] newArray = new byte[]{(byte)1, (byte)0, (byte)1};
                        nbtCompound.putByteArray(SettingsManager.CRAFTING_SETTINGS, newArray);

                        itemstack.setNbt(nbtCompound);
                        return itemstack;
                    }
                }
                else
                {
                    byte[] newArray = new byte[]{(byte)1, (byte)0, (byte)1};
                    nbtCompound.putByteArray(SettingsManager.CRAFTING_SETTINGS, newArray);

                    itemstack.setNbt(nbtCompound);
                    return itemstack;
                }
            }

            if(nbtCompound.contains(ITravelersBackpackInventory.TIER))
            {
                Tiers.Tier tier = Tiers.of(nbtCompound.getInt(ITravelersBackpackInventory.TIER));

                if(inventory.getStack(2).isOf(Tiers.of(nbtCompound.getInt(ITravelersBackpackInventory.TIER)).getTierUpgradeIngredient()))
                {
                    upgradeInventory(nbtCompound, tier);
                    itemstack.setNbt(nbtCompound.copy());
                    return itemstack;
                }
            }
            else
            {
                if(inventory.getStack(2).isOf(Tiers.LEATHER.getTierUpgradeIngredient()))
                {
                    upgradeInventory(nbtCompound, Tiers.LEATHER);
                    itemstack.setNbt(nbtCompound.copy());
                    return itemstack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public void upgradeInventory(NbtCompound compound, Tiers.Tier tier)
    {
        compound.putInt(ITravelersBackpackInventory.TIER, tier.getNextTier().getOrdinal());

        if(compound.contains(ITravelersBackpackInventory.TOOLS_INVENTORY))
        {
            if(compound.getCompound(ITravelersBackpackInventory.TOOLS_INVENTORY).contains("Size", NbtElement.INT_TYPE))
            {
                compound.getCompound(ITravelersBackpackInventory.TOOLS_INVENTORY).putInt("Size", tier.getNextTier().getToolSlots());
            }
        }

        if(compound.contains(ITravelersBackpackInventory.INVENTORY))
        {
            if(compound.getCompound(ITravelersBackpackInventory.INVENTORY).contains("Size", NbtElement.INT_TYPE))
            {
                compound.getCompound(ITravelersBackpackInventory.INVENTORY).putInt("Size", tier.getNextTier().getStorageSlots());
            }
        }

        if(compound.contains(ITravelersBackpackInventory.LEFT_TANK))
        {
            if(compound.getCompound(ITravelersBackpackInventory.LEFT_TANK).contains("capacity", NbtElement.LONG_TYPE))
            {
                compound.getCompound(ITravelersBackpackInventory.LEFT_TANK).putLong("capacity", tier.getNextTier().getTankCapacity());
            }
        }

        if(compound.contains(ITravelersBackpackInventory.RIGHT_TANK))
        {
            if(compound.getCompound(ITravelersBackpackInventory.RIGHT_TANK).contains("capacity", NbtElement.LONG_TYPE))
            {
                compound.getCompound(ITravelersBackpackInventory.RIGHT_TANK).putLong("capacity", tier.getNextTier().getTankCapacity());
            }
        }
    }

    @Override
    public boolean matches(Inventory inventory, World world)
    {
        ItemStack addition = inventory.getStack(2);
        boolean flag = true;

        if(!TravelersBackpackConfig.getConfig().backpackSettings.enableCraftingUpgrade)
        {
            flag = !addition.isOf(ModItems.CRAFTING_UPGRADE);
        }
        if(!TravelersBackpackConfig.getConfig().backpackSettings.enableTierUpgrades)
        {
            flag = !(addition.isOf(ModItems.IRON_TIER_UPGRADE) || addition.isOf(ModItems.GOLD_TIER_UPGRADE)
                    || addition.isOf(ModItems.DIAMOND_TIER_UPGRADE) || addition.isOf(ModItems.NETHERITE_TIER_UPGRADE));
        }
        return matchesTier(inventory, world) && flag && super.matches(inventory, world);
    }

    public boolean matchesTier(Inventory inventory, World world)
    {
        ItemStack base = inventory.getStack(1);
        ItemStack addition = inventory.getStack(2);

        if(addition.getItem() == ModItems.CRAFTING_UPGRADE)
        {
            return true;
        }

        if(!base.hasNbt() || !base.getNbt().contains(ITravelersBackpackInventory.TIER))
        {
            return addition.isOf(ModItems.IRON_TIER_UPGRADE);
        }

        if(base.getNbt().contains(ITravelersBackpackInventory.TIER))
        {
            int tier = base.getNbt().getInt(ITravelersBackpackInventory.TIER);

            return switch(tier)
            {
                case 0 -> addition.getItem() == ModItems.IRON_TIER_UPGRADE;
                case 1 -> addition.getItem() == ModItems.GOLD_TIER_UPGRADE;
                case 2 -> addition.getItem() == ModItems.DIAMOND_TIER_UPGRADE;
                case 3 -> addition.getItem() == ModItems.NETHERITE_TIER_UPGRADE;
                default -> false;
            };
        }
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.BACKPACK_UPGRADE;
    }

    public static class Serializer implements RecipeSerializer<BackpackUpgradeRecipe>
    {
        @Override
        public BackpackUpgradeRecipe read(Identifier identifier, JsonObject jsonObject) {
            Ingredient ingredient = Ingredient.fromJson(JsonHelper.getElement(jsonObject, "template"));
            Ingredient ingredient2 = Ingredient.fromJson(JsonHelper.getElement(jsonObject, "base"));
            Ingredient ingredient3 = Ingredient.fromJson(JsonHelper.getElement(jsonObject, "addition"));
            ItemStack itemStack = ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));
            return new BackpackUpgradeRecipe(identifier, ingredient, ingredient2, ingredient3, itemStack);
        }

        @Override
        public BackpackUpgradeRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            Ingredient ingredient = Ingredient.fromPacket(packetByteBuf);
            Ingredient ingredient2 = Ingredient.fromPacket(packetByteBuf);
            Ingredient ingredient3 = Ingredient.fromPacket(packetByteBuf);
            ItemStack itemStack = packetByteBuf.readItemStack();
            return new BackpackUpgradeRecipe(identifier, ingredient, ingredient2, ingredient3, itemStack);
        }

        @Override
        public void write(PacketByteBuf packetByteBuf, BackpackUpgradeRecipe upgradeRecipe) {
            upgradeRecipe.template.write(packetByteBuf);
            upgradeRecipe.base.write(packetByteBuf);
            upgradeRecipe.addition.write(packetByteBuf);
            packetByteBuf.writeItemStack(upgradeRecipe.result);
        }
    }
}