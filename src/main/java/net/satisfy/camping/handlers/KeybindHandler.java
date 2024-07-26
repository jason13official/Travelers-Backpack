package net.satisfy.camping.handlers;

import net.satisfy.camping.component.ComponentUtils;
import net.satisfy.camping.config.TravelersBackpackConfig;
import net.satisfy.camping.init.ModNetwork;
import net.satisfy.camping.inventory.screen.slot.ToolSlot;
import net.satisfy.camping.items.HoseItem;
import net.satisfy.camping.items.TravelersBackpackItem;
import net.satisfy.camping.util.Reference;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class KeybindHandler
{
    private static final String CATEGORY = "key.camping.category";
    public static final KeyBinding OPEN_BACKPACK = new KeyBinding("key.camping.inventory", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_B, CATEGORY);
    public static final KeyBinding SORT_BACKPACK = new KeyBinding("key.camping.sort", InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), CATEGORY);
    public static final KeyBinding ABILITY = new KeyBinding("key.camping.ability", InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), CATEGORY);
    public static final KeyBinding SWITCH_TOOL = new KeyBinding("key.camping.cycle_tool", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_Z, CATEGORY);
    public static final KeyBinding TOGGLE_TANK = new KeyBinding("key.camping.toggle_tank", InputUtil.Type.KEYSYM, InputUtil.GLFW_KEY_N, CATEGORY);

    public static void initKeybinds()
    {
        KeyBindingHelper.registerKeyBinding(OPEN_BACKPACK);
        KeyBindingHelper.registerKeyBinding(SORT_BACKPACK);
        KeyBindingHelper.registerKeyBinding(ABILITY);
        KeyBindingHelper.registerKeyBinding(SWITCH_TOOL);
        KeyBindingHelper.registerKeyBinding(TOGGLE_TANK);
    }

    public static void registerListeners()
    {
        ClientTickEvents.END_CLIENT_TICK.register(evt ->
        {
            ClientPlayerEntity player = evt.player;

            if(player == null) return;

            if(ComponentUtils.isWearingBackpack(player))
            {
                while(OPEN_BACKPACK.wasPressed())
                {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeByte(Reference.NO_SCREEN_ID).writeByte(Reference.OPEN_SCREEN).writeDouble(0.0D);

                    ClientPlayNetworking.send(ModNetwork.SPECIAL_ACTION_ID, buf);
                }

                while(ABILITY.wasPressed())
                {
                    if(TravelersBackpackConfig.isAbilityAllowed(ComponentUtils.getWearingBackpack(player)))
                    {
                        boolean ability = ComponentUtils.getBackpackInv(player).getAbilityValue();
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeByte(Reference.WEARABLE_SCREEN_ID).writeBoolean(!ability);

                        ClientPlayNetworking.send(ModNetwork.ABILITY_SLIDER_ID, buf);

                        player.sendMessage(Text.translatable(ability ? "screen.camping.ability_disabled" : "screen.camping.ability_enabled"), true);
                    }
                }

                if(player.getMainHandStack().getItem() instanceof HoseItem && player.getMainHandStack().getNbt() != null)
                {
                    while(TOGGLE_TANK.wasPressed())
                    {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeByte(Reference.WEARABLE_SCREEN_ID).writeByte(Reference.TOGGLE_HOSE_TANK).writeDouble(0.0D);

                        ClientPlayNetworking.send(ModNetwork.SPECIAL_ACTION_ID, buf);
                    }
                }

                if(TravelersBackpackConfig.getConfig().client.disableScrollWheel)
                {
                    ItemStack heldItem = player.getMainHandStack();

                    while(SWITCH_TOOL.wasPressed())
                    {
                        if(!heldItem.isEmpty())
                        {
                            if(TravelersBackpackConfig.getConfig().client.enableToolCycling)
                            {
                                if(ToolSlot.isValid(heldItem))
                                {
                                    PacketByteBuf buf = PacketByteBufs.create();
                                    buf.writeByte(Reference.WEARABLE_SCREEN_ID).writeByte(Reference.SWAP_TOOL).writeDouble(1.0D);

                                    ClientPlayNetworking.send(ModNetwork.SPECIAL_ACTION_ID, buf);
                                }
                            }

                            if(heldItem.getItem() instanceof HoseItem)
                            {
                                if(heldItem.getNbt() != null)
                                {
                                    PacketByteBuf buf = PacketByteBufs.create();
                                    buf.writeByte(Reference.WEARABLE_SCREEN_ID).writeByte(Reference.SWITCH_HOSE_MODE).writeDouble(1.0D);

                                    ClientPlayNetworking.send(ModNetwork.SPECIAL_ACTION_ID, buf);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public static boolean onMouseScroll(double scrollDelta)
    {
        MinecraftClient mc = MinecraftClient.getInstance();

        if(!TravelersBackpackConfig.getConfig().client.disableScrollWheel && scrollDelta != 0.0)
        {
            ClientPlayerEntity player = mc.player;

            if(player != null && player.isAlive() && KeybindHandler.SWITCH_TOOL.isPressed())
            {
                ItemStack backpack = ComponentUtils.getWearingBackpack(player);

                if(backpack != null && backpack.getItem() instanceof TravelersBackpackItem)
                {
                    if(!player.getMainHandStack().isEmpty())
                    {
                        ItemStack heldItem = player.getMainHandStack();

                        if(TravelersBackpackConfig.getConfig().client.enableToolCycling)
                        {
                            if(ToolSlot.isValid(heldItem))
                            {
                                PacketByteBuf buf = PacketByteBufs.create();
                                buf.writeByte(Reference.WEARABLE_SCREEN_ID).writeByte(Reference.SWAP_TOOL).writeDouble(scrollDelta);

                                ClientPlayNetworking.send(ModNetwork.SPECIAL_ACTION_ID, buf);
                                return true;
                            }
                        }

                        if(heldItem.getItem() instanceof HoseItem)
                        {
                            if(heldItem.getNbt() != null)
                            {
                                PacketByteBuf buf = PacketByteBufs.create();
                                buf.writeByte(Reference.WEARABLE_SCREEN_ID).writeByte(Reference.SWITCH_HOSE_MODE).writeDouble(scrollDelta);

                                ClientPlayNetworking.send(ModNetwork.SPECIAL_ACTION_ID, buf);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}