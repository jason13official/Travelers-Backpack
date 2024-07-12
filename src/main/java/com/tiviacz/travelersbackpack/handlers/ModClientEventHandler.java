package com.tiviacz.travelersbackpack.handlers;

import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.client.renderer.TravelersBackpackBlockEntityRenderer;
import com.tiviacz.travelersbackpack.client.screens.HudOverlay;
import com.tiviacz.travelersbackpack.client.screens.TravelersBackpackScreen;
import com.tiviacz.travelersbackpack.client.screens.tooltip.BackpackTooltipComponent;
import com.tiviacz.travelersbackpack.client.screens.tooltip.ClientBackpackTooltipComponent;
import com.tiviacz.travelersbackpack.config.TravelersBackpackConfig;
import com.tiviacz.travelersbackpack.init.ModBlockEntityTypes;
import com.tiviacz.travelersbackpack.init.ModItems;
import com.tiviacz.travelersbackpack.init.ModMenuTypes;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import org.lwjgl.glfw.GLFW;

public class ModClientEventHandler
{
    public static final String CATEGORY = "key.travelersbackpack.category";
    public static final KeyMapping OPEN_BACKPACK = new KeyMapping("key.travelersbackpack.inventory", GLFW.GLFW_KEY_B, CATEGORY);
    public static final KeyMapping SORT_BACKPACK = new KeyMapping("key.travelersbackpack.sort", GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    public static final KeyMapping ABILITY = new KeyMapping("key.travelersbackpack.ability", GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    public static final KeyMapping SWAP_TOOL = new KeyMapping("key.travelersbackpack.cycle_tool", GLFW.GLFW_KEY_Z, CATEGORY);
    public static final KeyMapping TOGGLE_TANK = new KeyMapping("key.travelersbackpack.toggle_tank", GLFW.GLFW_KEY_N, CATEGORY);

    public static void registerScreenFactory()
    {
        MenuScreens.register(ModMenuTypes.TRAVELERS_BACKPACK_BLOCK_ENTITY.get(), TravelersBackpackScreen::new);
        MenuScreens.register(ModMenuTypes.TRAVELERS_BACKPACK_ITEM.get(), TravelersBackpackScreen::new);
    }

    public static void registerTooltipComponent()
    {
        MinecraftForgeClient.registerTooltipComponentFactory(BackpackTooltipComponent.class, ClientBackpackTooltipComponent::new);
    }

    public static void bindTileEntityRenderer()
    {
        BlockEntityRenderers.register(ModBlockEntityTypes.TRAVELERS_BACKPACK.get(), TravelersBackpackBlockEntityRenderer::new);
    }

    public static void registerKeybinding()
    {
        ClientRegistry.registerKeyBinding(OPEN_BACKPACK);
        ClientRegistry.registerKeyBinding(SORT_BACKPACK);
        ClientRegistry.registerKeyBinding(ABILITY);
        ClientRegistry.registerKeyBinding(SWAP_TOOL);
        ClientRegistry.registerKeyBinding(TOGGLE_TANK);
    }

    public static void registerOverlay()
    {
        OverlayRegistry.registerOverlayBelow(ForgeIngameGui.HOTBAR_ELEMENT, "Traveler's Backpack HUD", (gui, poseStack, partialTicks, screenWidth, screenHeight) ->
        {
            Minecraft mc = Minecraft.getInstance();

            if(TravelersBackpackConfig.enableOverlay && !mc.options.hideGui && CapabilityUtils.isWearingBackpack(mc.player) && mc.gameMode.getPlayerMode() != GameType.SPECTATOR)
            {
                HudOverlay.renderOverlay(gui, mc, poseStack);
            }
        });
    }

    public static void registerItemModelProperty()
    {
        ItemProperties.register(ModItems.HOSE.get(), new ResourceLocation(TravelersBackpack.MODID,"mode"), (stack, world, entity, p_174638_) -> {
            CompoundTag compound = stack.getTag();
            if(compound == null) return 0;
            else return compound.getInt("Mode");
        });
    }
}