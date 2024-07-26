package net.satisfy.camping.items;

import net.satisfy.camping.blockentity.TravelersBackpackBlockEntity;
import net.satisfy.camping.client.screen.tooltip.BackpackTooltipData;
import net.satisfy.camping.common.BackpackAbilities;
import net.satisfy.camping.common.ServerActions;
import net.satisfy.camping.component.ComponentUtils;
import net.satisfy.camping.config.TravelersBackpackConfig;
import net.satisfy.camping.init.ModItems;
import net.satisfy.camping.inventory.ITravelersBackpackInventory;
import net.satisfy.camping.inventory.Tiers;
import net.satisfy.camping.inventory.TravelersBackpackInventory;
import net.satisfy.camping.util.BackpackUtils;
import net.satisfy.camping.util.Reference;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class TravelersBackpackItem extends BlockItem
{
    public TravelersBackpackItem(Block block)
    {
        super(block, new Settings().fireproof().maxCount(1));
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context)
    {
        if(stack.hasNbt())
        {
            if(stack.getNbt().contains(ITravelersBackpackInventory.TIER))
            {
                tooltip.add(Text.translatable("tier.camping." + Tiers.of(stack.getNbt().getInt(ITravelersBackpackInventory.TIER)).getName()));
            }

            if(!BackpackUtils.isCtrlPressed())
            {
                tooltip.add(Text.translatable("item.camping.inventory_tooltip").formatted(Formatting.BLUE));
            }
        }

        if(TravelersBackpackConfig.getConfig().client.obtainTips)
        {
            if(stack.getItem() == ModItems.BAT_TRAVELERS_BACKPACK)
            {
                tooltip.add(Text.translatable("obtain.camping.bat").formatted(Formatting.BLUE));
            }

            if(stack.getItem() == ModItems.VILLAGER_TRAVELERS_BACKPACK)
            {
                tooltip.add(Text.translatable("obtain.camping.villager").formatted(Formatting.BLUE));
            }

            if(stack.getItem() == ModItems.IRON_GOLEM_TRAVELERS_BACKPACK)
            {
                tooltip.add(Text.translatable("obtain.camping.iron_golem").formatted(Formatting.BLUE));
            }
        }

        if(BackpackAbilities.isOnList(BackpackAbilities.ALL_ABILITIES_LIST, stack))
        {
            if(BackpackUtils.isShiftPressed())
            {
                tooltip.add(Text.translatable("ability.camping." + this.getTranslationKey(stack).replaceAll("block.camping.", "")).formatted(Formatting.BLUE));

                if(BackpackAbilities.isOnList(BackpackAbilities.BLOCK_ABILITIES_LIST, stack) && BackpackAbilities.isOnList(BackpackAbilities.ITEM_ABILITIES_LIST, stack))
                {
                    tooltip.add(Text.translatable("ability.camping.item_and_block"));
                }
                else if(BackpackAbilities.isOnList(BackpackAbilities.BLOCK_ABILITIES_LIST, stack) && !BackpackAbilities.isOnList(BackpackAbilities.ITEM_ABILITIES_LIST, stack))
                {
                    tooltip.add(Text.translatable("ability.camping.block"));
                }
                else if(BackpackAbilities.isOnList(BackpackAbilities.ITEM_ABILITIES_LIST, stack) && !BackpackAbilities.isOnList(BackpackAbilities.BLOCK_ABILITIES_LIST, stack))
                {
                    tooltip.add(Text.translatable("ability.camping.item"));
                }
            }
            else
            {
                tooltip.add(Text.translatable("ability.camping.hold_shift").formatted(Formatting.BLUE));
            }
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context)
    {
        ActionResult actionresult = this.place(new ItemPlacementContext(context));
        return !actionresult.isAccepted() ? this.use(context.getWorld(), context.getPlayer(), context.getHand()).getResult() : actionresult;
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {
        if (!this.getBlock().isEnabled(context.getWorld().getEnabledFeatures())) {
            return ActionResult.FAIL;
        }

        if (!context.canPlace() || (context.getHand() == Hand.MAIN_HAND && context.getPlayer() != null && !context.getPlayer().isSneaking())) {
            return ActionResult.FAIL;
        }
        ItemPlacementContext itemPlacementContext = this.getPlacementContext(context);
        if (itemPlacementContext == null) {
            return ActionResult.FAIL;
        }
        BlockState blockState = this.getPlacementState(itemPlacementContext);
        if (blockState == null) {
            return ActionResult.FAIL;
        }
        if (!this.place(itemPlacementContext, blockState)) {
            return ActionResult.FAIL;
        }

        BlockPos blockPos = itemPlacementContext.getBlockPos();
        World world = itemPlacementContext.getWorld();
        PlayerEntity playerEntity = itemPlacementContext.getPlayer();
        ItemStack itemStack = itemPlacementContext.getStack();
        BlockState blockState2 = world.getBlockState(blockPos);

        if (blockState2.isOf(blockState.getBlock())) {

            this.postPlacement(blockPos, world, playerEntity, itemStack, blockState2);
            blockState2.getBlock().onPlaced(world, blockPos, blockState2, playerEntity, itemStack);

            if (playerEntity instanceof ServerPlayerEntity serverPlayer) {
                Criteria.PLACED_BLOCK.trigger(serverPlayer, blockPos, itemStack);
            }
        }

        BlockSoundGroup blockSoundGroup = blockState2.getSoundGroup();
        world.playSound(playerEntity, blockPos, this.getPlaceSound(blockState2), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
        world.emitGameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Emitter.of(playerEntity, blockState2));

        if (playerEntity == null || !playerEntity.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }

        return ActionResult.success(world.isClient);
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state)
    {
        return writeNbtToBlockEntity(world, player, pos, stack);
    }

    public static boolean writeNbtToBlockEntity(World world, @Nullable PlayerEntity player, BlockPos pos, ItemStack stack)
    {
        MinecraftServer minecraftServer = world.getServer();
        if(minecraftServer == null)
        {
            return false;
        }
        NbtCompound nbtCompound = stack.getNbt();

        if(nbtCompound != null && world.getBlockEntity(pos) instanceof TravelersBackpackBlockEntity blockEntity)
        {
            if(!(world.isClient || !blockEntity.copyItemDataRequiresOperator() || player != null && player.isCreativeLevelTwoOp()))
            {
                return false;
            }

            NbtCompound nbtCompound2 = blockEntity.createNbt();
            NbtCompound nbtCompound3 = nbtCompound2.copy();
            nbtCompound2.copyFrom(nbtCompound);

            if(!nbtCompound2.equals(nbtCompound3))
            {
                if(stack.hasCustomName())
                {
                    blockEntity.setCustomName(stack.getName());
                }

                blockEntity.readNbt(nbtCompound2);
                blockEntity.markDirty();
                return true;
            }
        }
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand)
    {
        ItemStack itemstack = user.getStackInHand(hand);

        if(hand == Hand.OFF_HAND || user.isSneaking())
        {
            return TypedActionResult.fail(itemstack);
        }

        if(!TravelersBackpackConfig.getConfig().backpackSettings.allowOnlyEquippedBackpack)
        {
            if(!world.isClient)
            {
                TravelersBackpackInventory.openHandledScreen(user, user.getMainHandStack(), Reference.ITEM_SCREEN_ID);
            }
        }
        else
        {
            if(!ComponentUtils.isWearingBackpack(user))
            {
                ServerActions.equipBackpack(user);
                user.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            }
        }
        return TypedActionResult.success(itemstack, world.isClient);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack)
    {
        return Optional.of(new BackpackTooltipData(stack));
    }

    @Override
    public boolean canBeNested()
    {
        return false;
    }
}