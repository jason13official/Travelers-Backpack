package com.tiviacz.travelersbackpack.client.screens;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.config.TravelersBackpackConfig;
import com.tiviacz.travelersbackpack.handlers.ModClientEventHandler;
import com.tiviacz.travelersbackpack.inventory.ITravelersBackpackContainer;
import com.tiviacz.travelersbackpack.items.HoseItem;
import com.tiviacz.travelersbackpack.util.RenderUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HudOverlay
{
    public static final ResourceLocation OVERLAY = new ResourceLocation(TravelersBackpack.MODID, "textures/gui/travelers_backpack_overlay.png");
    private static float animationProgress = 0.0F;

    public static void renderOverlay(ForgeIngameGui gui, Minecraft mc, PoseStack matrixStack)
    {
        Player player = mc.player;
        Window mainWindow = mc.getWindow();

        int scaledWidth = mainWindow.getGuiScaledWidth() - TravelersBackpackConfig.CLIENT.overlay.offsetX.get();
        int scaledHeight = mainWindow.getGuiScaledHeight() - TravelersBackpackConfig.CLIENT.overlay.offsetY.get();

        int textureX = 10;
        int textureY = 0;

        ITravelersBackpackContainer inv = CapabilityUtils.getBackpackInv(player);

        KeyMapping key = ModClientEventHandler.CYCLE_TOOL;
        List<ItemStack> tools = getTools(inv.getToolSlotsHandler());

        if(key.isDown() && tools.size() > 2)
        {
            if(animationProgress < 1.0F)
            {
                animationProgress += 0.02F;
            }
            for(int i = 0; i < getTools(inv.getToolSlotsHandler()).size(); i++)
            {
                drawItemStack(mc.getItemRenderer(), getTools(inv.getToolSlotsHandler()).get(i), scaledWidth - 30, (int)(scaledHeight + 11 - (animationProgress * (i * 15))));
            }
        }
        else if(!tools.isEmpty())
        {
            if(animationProgress > 0.0F)
            {
                for(int i = 0; i < getTools(inv.getToolSlotsHandler()).size(); i++)
                {
                    drawItemStack(mc.getItemRenderer(), getTools(inv.getToolSlotsHandler()).get(i), scaledWidth - 30, (int)(scaledHeight + 11 - (animationProgress * (i * 15))));
                }
                animationProgress -= 0.02F;
            }
            else
            {
                if(!inv.getToolSlotsHandler().getStackInSlot(0).isEmpty())
                {
                    drawItemStack(mc.getItemRenderer(), inv.getToolSlotsHandler().getStackInSlot(0), scaledWidth - 30, scaledHeight - 4);
                }
                if(tools.size() > 1)
                {
                    if(!inv.getToolSlotsHandler().getStackInSlot(tools.size() - 1).isEmpty())
                    {
                        drawItemStack(mc.getItemRenderer(), inv.getToolSlotsHandler().getStackInSlot(tools.size() - 1), scaledWidth - 30, scaledHeight + 11);
                    }
                }
            }
        }

        if(!inv.getRightTank().getFluid().isEmpty())
        {
            drawGuiTank(matrixStack, inv.getRightTank(), scaledWidth + 1, scaledHeight, 21, 8);
        }

        if(!inv.getLeftTank().getFluid().isEmpty())
        {
            drawGuiTank(matrixStack, inv.getLeftTank(), scaledWidth - 11, scaledHeight, 21, 8);
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, OVERLAY);

        if(player.getMainHandItem().getItem() instanceof HoseItem)
        {
            int tank = HoseItem.getHoseTank(player.getMainHandItem());

            int selectedTextureX = 0;
            int selectedTextureY = 0;

            if(tank == 1)
            {
                gui.blit(matrixStack, scaledWidth, scaledHeight, textureX, textureY, 10, 23);
                gui.blit(matrixStack, scaledWidth - 12, scaledHeight, selectedTextureX, selectedTextureY, 10, 23);
            }

            if(tank == 2)
            {
                gui.blit(matrixStack, scaledWidth, scaledHeight, selectedTextureX, selectedTextureY, 10, 23);
                gui.blit(matrixStack, scaledWidth - 12, scaledHeight, textureX, textureY, 10, 23);
            }

            if(tank == 0)
            {
                gui.blit(matrixStack, scaledWidth, scaledHeight, textureX, textureY, 10, 23);
                gui.blit(matrixStack, scaledWidth - 12, scaledHeight, textureX, textureY, 10, 23);
            }
        }
        else
        {
            gui.blit(matrixStack, scaledWidth, scaledHeight, textureX, textureY, 10, 23);
            gui.blit(matrixStack, scaledWidth - 12, scaledHeight, textureX, textureY, 10, 23);
        }
    }

    public static void drawGuiTank(PoseStack matrixStackIn, FluidTank tank, int startX, int startY, int height, int width)
    {
        RenderUtils.renderScreenTank(matrixStackIn, tank, startX, startY, 0, height, width);
    }

    private static void drawItemStack(ItemRenderer itemRenderer, ItemStack stack, int x, int y)
    {
        itemRenderer.renderGuiItem(stack, x, y);
        itemRenderer.renderGuiItemDecorations(Minecraft.getInstance().font, stack, x, y);
    }

    public static List<ItemStack> getTools(ItemStackHandler inventory)
    {
        List<ItemStack> tools = new ArrayList<>();

        for(int i = 0; i < inventory.getSlots(); i++)
        {
            if(!inventory.getStackInSlot(i).isEmpty())
            {
                tools.add(inventory.getStackInSlot(i));
            }
        }

        Collections.reverse(tools);
        return tools;
    }
}