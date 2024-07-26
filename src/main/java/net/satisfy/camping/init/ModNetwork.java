package net.satisfy.camping.init;

import net.satisfy.camping.TravelersBackpack;
import net.satisfy.camping.common.ServerActions;
import net.satisfy.camping.component.ComponentUtils;
import net.satisfy.camping.config.TravelersBackpackConfig;
import net.satisfy.camping.config.TravelersBackpackConfigData;
import net.satisfy.camping.inventory.SettingsManager;
import net.satisfy.camping.inventory.TravelersBackpackInventory;
import net.satisfy.camping.inventory.screen.TravelersBackpackBlockEntityScreenHandler;
import net.satisfy.camping.inventory.screen.TravelersBackpackItemScreenHandler;
import net.satisfy.camping.inventory.sorter.SlotManager;
import net.satisfy.camping.util.Reference;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ModNetwork
{
    public static final Identifier EQUIP_BACKPACK_ID = new Identifier(TravelersBackpack.MODID, "equip_backpack");
    public static final Identifier DEPLOY_SLEEPING_BAG_ID = new Identifier(TravelersBackpack.MODID, "deploy_sleeping_bag");
    public static final Identifier SPECIAL_ACTION_ID = new Identifier(TravelersBackpack.MODID, "special_action");
    public static final Identifier ABILITY_SLIDER_ID = new Identifier(TravelersBackpack.MODID, "ability_slider");
    public static final Identifier SORTER_ID = new Identifier(TravelersBackpack.MODID, "sorter");
    public static final Identifier SLOT_ID = new Identifier(TravelersBackpack.MODID, "slot");
    public static final Identifier MEMORY_ID = new Identifier(TravelersBackpack.MODID, "memory");
    public static final Identifier SETTINGS_ID = new Identifier(TravelersBackpack.MODID, "settings");
    public static final Identifier UPDATE_CONFIG_ID = new Identifier(TravelersBackpack.MODID,"update_config");
    public static final Identifier SYNC_BACKPACK_ID = new Identifier(TravelersBackpack.MODID, "sync_backpack");
    public static final Identifier SEND_MESSAGE_ID = new Identifier(TravelersBackpack.MODID, "send_message");

    public static void initClient()
    {
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_CONFIG_ID, (client, handler, buf, sender) ->
        {
            NbtCompound tag = buf.readNbt();
            client.execute(() ->
            {
                TravelersBackpack.LOGGER.info("Syncing config from server to client...");
                AutoConfig.getConfigHolder(TravelersBackpackConfigData.class).setConfig(TravelersBackpackConfig.readFromNbt(tag));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SEND_MESSAGE_ID, (client, handler, buf, sender) ->
        {
            boolean drop = buf.readBoolean();
            BlockPos pos = buf.readBlockPos();

            if(TravelersBackpackConfig.getConfig().client.sendBackpackCoordinatesMessage)
            {
                if(MinecraftClient.getInstance().player != null)
                {
                    MinecraftClient.getInstance().player.sendMessage(Text.translatable(drop ? "information.camping.backpack_drop" : "information.camping.backpack_coords", pos.getX(), pos.getY(), pos.getZ()));
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ModNetwork.SYNC_BACKPACK_ID, (client, handler, buf, sender) ->
        {
            int entityId = buf.readInt();
            NbtCompound compound = buf.readNbt();

            client.execute(() ->
            {
                if(client.world != null)
                {
                    Entity entity = client.world.getEntityById(entityId);

                    if(entity instanceof PlayerEntity player)
                    {
                        ComponentUtils.getComponent(player).setWearable(ItemStack.fromNbt(compound));
                        ComponentUtils.getComponent(player).setContents(ItemStack.fromNbt(compound));
                    }
                }
            });
        });
    }

    public static void initServer()
    {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
        {
            //Load default config from file
            TravelersBackpack.LOGGER.info("Loading config from file...");
            AutoConfig.getConfigHolder(TravelersBackpackConfigData.class).load();

            //Sync config from server to client if present
            PacketByteBuf data = PacketByteBufs.create();
            data.writeNbt(TravelersBackpackConfig.writeToNbt());
            ServerPlayNetworking.send(handler.player, UPDATE_CONFIG_ID, data);

            //Packets to sync backpack component to client on login (Cardinal Components autosync somehow doesn't sync properly)

            //Sync to target client
            PacketByteBuf buf2 = PacketByteBufs.create();
            buf2.writeInt(handler.getPlayer().getId());
            buf2.writeNbt(ComponentUtils.getWearingBackpack(handler.getPlayer()).writeNbt(new NbtCompound()));
            sender.sendPacket(ModNetwork.SYNC_BACKPACK_ID, buf2);

            //Sync backpacks of all players in radius of 64 blocks
            for(ServerPlayerEntity serverPlayer : PlayerLookup.around(handler.getPlayer().getServerWorld(), handler.getPlayer().getPos(), 64))
            {
                PacketByteBuf buf3 = PacketByteBufs.create();
                buf3.writeInt(serverPlayer.getId());
                buf3.writeNbt(ComponentUtils.getWearingBackpack(serverPlayer).writeNbt(new NbtCompound()));

                sender.sendPacket(ModNetwork.SYNC_BACKPACK_ID, buf3);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(EQUIP_BACKPACK_ID, (server, player, handler, buf, response) ->
        {
            boolean equip = buf.readBoolean();

            server.execute(() -> {
                if(player != null) //&& !TravelersBackpack.enableCurios())
                {
                    if(equip)
                    {
                        if(!ComponentUtils.isWearingBackpack(player))
                        {
                            ServerActions.equipBackpack(player);
                        }
                        else
                        {
                            player.onHandledScreenClosed();
                            player.sendMessage(Text.translatable(Reference.OTHER_BACKPACK), false);
                        }
                    }
                    else
                    {
                        if(ComponentUtils.isWearingBackpack(player))
                        {
                            ServerActions.unequipBackpack(player);
                        }
                        else
                        {
                            player.onHandledScreenClosed();
                            player.sendMessage(Text.translatable(Reference.NO_BACKPACK), false);
                        }
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(DEPLOY_SLEEPING_BAG_ID, (server, player, handler, buf, response) ->
        {
            BlockPos pos = buf.readBlockPos();

            server.execute(() -> {
                if(player != null)
                {
                    ServerActions.toggleSleepingBag(player, pos);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SPECIAL_ACTION_ID, (server, player, handler, buf, response) ->
        {
            byte screenID = buf.readByte();
            byte typeOfAction = buf.readByte();
            double scrollDelta = buf.readDouble();

            server.execute(() -> {
                if(player != null)
                {
                    if(typeOfAction == Reference.SWAP_TOOL)
                    {
                        ServerActions.swapTool(player, scrollDelta);
                    }

                    else if(typeOfAction == Reference.SWITCH_HOSE_MODE)
                    {
                        ServerActions.switchHoseMode(player, scrollDelta);
                    }

                    else if(typeOfAction == Reference.TOGGLE_HOSE_TANK)
                    {
                        ServerActions.toggleHoseTank(player);
                    }

                    else if(typeOfAction == Reference.EMPTY_TANK)
                    {
                        ServerActions.emptyTank(scrollDelta, player, player.getServerWorld(), screenID);
                    }

                    else if(typeOfAction == Reference.OPEN_SCREEN)
                    {
                        if(ComponentUtils.isWearingBackpack(player))
                        {
                            TravelersBackpackInventory.openHandledScreen(player, ComponentUtils.getWearingBackpack(player), Reference.WEARABLE_SCREEN_ID);
                        }
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ABILITY_SLIDER_ID, (server, player, handler, buf, response) ->
        {
            byte screenID = buf.readByte();
            boolean sliderValue = buf.readBoolean();

            server.execute(() -> {
                if(player != null)
                {
                    if(screenID == Reference.WEARABLE_SCREEN_ID && ComponentUtils.isWearingBackpack(player))
                    {
                        ServerActions.switchAbilitySlider(player, sliderValue);
                    }
                    else if(screenID == Reference.BLOCK_ENTITY_SCREEN_ID && player.currentScreenHandler instanceof TravelersBackpackBlockEntityScreenHandler)
                    {
                        ServerActions.switchAbilitySliderBlockEntity(player, ((TravelersBackpackBlockEntityScreenHandler)player.currentScreenHandler).inventory.getPosition(), sliderValue);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SORTER_ID, (server, player, handler, buf, response) ->
        {
            byte screenID = buf.readByte();
            byte button = buf.readByte();
            boolean shiftPressed = buf.readBoolean();

            server.execute(() -> {
                if(player != null)
                {
                    ServerActions.sortBackpack(player, screenID, button, shiftPressed);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SLOT_ID, (server, player, handler, buf, response) ->
        {
            final byte screenID = buf.readByte();
            final boolean isActive = buf.readBoolean();
            final int[] selectedSlots = buf.readIntArray();

            server.execute(() -> {
                if(player != null)
                {
                    if(screenID == Reference.WEARABLE_SCREEN_ID)
                    {
                        SlotManager manager = ComponentUtils.getBackpackInv(player).getSlotManager();
                        manager.setSelectorActive(SlotManager.UNSORTABLE, isActive);
                        manager.setUnsortableSlots(selectedSlots, true);
                        manager.setSelectorActive(SlotManager.UNSORTABLE, !isActive);
                    }
                    if(screenID == Reference.ITEM_SCREEN_ID)
                    {
                        SlotManager manager = ((TravelersBackpackItemScreenHandler)player.currentScreenHandler).inventory.getSlotManager();
                        manager.setSelectorActive(SlotManager.UNSORTABLE, isActive);
                        manager.setUnsortableSlots(selectedSlots, true);
                        manager.setSelectorActive(SlotManager.UNSORTABLE, !isActive);
                    }
                    if(screenID == Reference.BLOCK_ENTITY_SCREEN_ID)
                    {
                        SlotManager manager = ((TravelersBackpackBlockEntityScreenHandler)player.currentScreenHandler).inventory.getSlotManager();
                        manager.setSelectorActive(SlotManager.UNSORTABLE, isActive);
                        manager.setUnsortableSlots(selectedSlots, true);
                        manager.setSelectorActive(SlotManager.UNSORTABLE, !isActive);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(MEMORY_ID, (server, player, handler, buf, response) ->
        {
            final byte screenID = buf.readByte();
            final boolean isActive = buf.readBoolean();
            final int[] selectedSlots = buf.readIntArray();
            final ItemStack[] stacks = new ItemStack[selectedSlots.length];

            for(int i = 0; i < selectedSlots.length; i++)
            {
                stacks[i] = buf.readItemStack();
            }

            server.execute(() -> {
                if(player != null)
                {
                    if(screenID == Reference.WEARABLE_SCREEN_ID)
                    {
                        SlotManager manager = ComponentUtils.getBackpackInv(player).getSlotManager();
                        manager.setSelectorActive(SlotManager.MEMORY, isActive);
                        manager.setMemorySlots(selectedSlots, stacks, true);
                        manager.setSelectorActive(SlotManager.MEMORY, !isActive);
                    }
                    if(screenID == Reference.ITEM_SCREEN_ID)
                    {
                        SlotManager manager = ((TravelersBackpackItemScreenHandler)player.currentScreenHandler).inventory.getSlotManager();
                        manager.setSelectorActive(SlotManager.MEMORY, isActive);
                        manager.setMemorySlots(selectedSlots, stacks, true);
                        manager.setSelectorActive(SlotManager.MEMORY, !isActive);
                    }
                    if(screenID == Reference.BLOCK_ENTITY_SCREEN_ID)
                    {
                        SlotManager manager = ((TravelersBackpackBlockEntityScreenHandler)player.currentScreenHandler).inventory.getSlotManager();
                        manager.setSelectorActive(SlotManager.MEMORY, isActive);
                        manager.setMemorySlots(selectedSlots, stacks, true);
                        manager.setSelectorActive(SlotManager.MEMORY, !isActive);
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SETTINGS_ID, (server, player, handler, buf, response) ->
        {
            final byte screenID = buf.readByte();
            final byte dataArray = buf.readByte();
            final int place = buf.readInt();
            final byte value = buf.readByte();
            server.execute(() ->
            {
                if(player != null)
                {
                    if(screenID == Reference.WEARABLE_SCREEN_ID)
                    {
                        SettingsManager manager = ComponentUtils.getBackpackInv(player).getSettingsManager();
                        manager.set(dataArray, place, value);
                    }
                    if(screenID == Reference.ITEM_SCREEN_ID)
                    {
                        SettingsManager manager = ((TravelersBackpackItemScreenHandler)player.currentScreenHandler).inventory.getSettingsManager();
                        manager.set(dataArray, place, value);
                    }
                    if(screenID == Reference.BLOCK_ENTITY_SCREEN_ID)
                    {
                        SettingsManager manager = ((TravelersBackpackBlockEntityScreenHandler)player.currentScreenHandler).inventory.getSettingsManager();
                        manager.set(dataArray, place, value);
                    }
                }
            });
        });
    }
}