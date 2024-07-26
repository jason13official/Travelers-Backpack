package net.satisfy.camping.client.screen.buttons;

import net.satisfy.camping.client.screen.TravelersBackpackHandledScreen;
import net.satisfy.camping.common.BackpackAbilities;
import net.satisfy.camping.component.ComponentUtils;
import net.satisfy.camping.config.TravelersBackpackConfig;
import net.satisfy.camping.init.ModNetwork;
import net.satisfy.camping.util.BackpackUtils;
import net.satisfy.camping.util.Reference;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class AbilitySliderButton extends Button
{
    public AbilitySliderButton(TravelersBackpackHandledScreen screen)
    {
        super(screen, 5, screen.inventory.getRows() <= 4 ? 26 : 56, 18, 11);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        if(screen.inventory.hasTileEntity())
        {
            if(BackpackAbilities.isOnList(BackpackAbilities.BLOCK_ABILITIES_LIST, screen.inventory.getItemStack()))
            {
                if(!screen.toolSlotsWidget.isCoveringAbility())
                {
                    drawButton(context, mouseX, mouseY, TravelersBackpackHandledScreen.EXTRAS_TRAVELERS_BACKPACK);
                }
            }
        }
        else
        {
            if(ComponentUtils.isWearingBackpack(screen.getScreenHandler().playerInventory.player) && screen.inventory.getScreenID() == Reference.WEARABLE_SCREEN_ID)
            {
                if(BackpackAbilities.isOnList(BackpackAbilities.ITEM_ABILITIES_LIST, screen.inventory.getItemStack()))
                {
                    if(!screen.toolSlotsWidget.isCoveringAbility())
                    {
                        drawButton(context, mouseX, mouseY, TravelersBackpackHandledScreen.EXTRAS_TRAVELERS_BACKPACK);
                    }
                }
            }
        }
    }

    public void drawButton(DrawContext context, int mouseX, int mouseY, Identifier texture)
    {
        if(screen.inventory.getAbilityValue())
        {
            this.drawButton(context, mouseX, mouseY, texture, 114, 0, 95, 0);
        }
        else
        {
            this.drawButton(context, mouseX, mouseY, texture, 114, 12, 95, 12);
        }
    }

    @Override
    public void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY)
    {
        if(screen.inventory.getScreenID() == Reference.BLOCK_ENTITY_SCREEN_ID || screen.inventory.getScreenID() == Reference.WEARABLE_SCREEN_ID)
        {
            if(BackpackAbilities.isOnList(screen.inventory.getScreenID() == Reference.WEARABLE_SCREEN_ID ? BackpackAbilities.ITEM_ABILITIES_LIST : BackpackAbilities.BLOCK_ABILITIES_LIST, screen.inventory.getItemStack()) && this.inButton(mouseX, mouseY) && !screen.isWidgetVisible(3, screen.leftTankSlotWidget) && !screen.isWidgetVisible(4, screen.leftTankSlotWidget))
            {
                if(!screen.toolSlotsWidget.isCoveringAbility())
                {
                    if(screen.inventory.getAbilityValue())
                    {
                        List<Text> list = new ArrayList<>();
                        list.add(Text.translatable("screen.camping.ability_enabled"));
                        if(BackpackAbilities.isOnList(BackpackAbilities.ITEM_TIMER_ABILITIES_LIST, screen.inventory.getItemStack()) || BackpackAbilities.isOnList(BackpackAbilities.BLOCK_TIMER_ABILITIES_LIST, screen.inventory.getItemStack()))
                        {
                            list.add(screen.inventory.getLastTime() == 0 ? Text.translatable("screen.camping.ability_ready") : Text.translatable(BackpackUtils.getConvertedTime(screen.inventory.getLastTime())));
                        }
                        context.drawTooltip(screen.getTextRenderer(), list, mouseX, mouseY);
                    }
                    else
                    {
                        if(!TravelersBackpackConfig.getConfig().backpackAbilities.enableBackpackAbilities || !TravelersBackpackConfig.isAbilityAllowed(screen.inventory.getItemStack()))
                        {
                            context.drawTooltip(screen.getTextRenderer(), Text.translatable("screen.camping.ability_disabled_config"), mouseX, mouseY);
                        }
                        else
                        {
                            context.drawTooltip(screen.getTextRenderer(), Text.translatable("screen.camping.ability_disabled"), mouseX, mouseY);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(screen.inventory.hasTileEntity())
        {
            if(!screen.toolSlotsWidget.isCoveringAbility())
            {
                if(BackpackAbilities.isOnList(BackpackAbilities.BLOCK_ABILITIES_LIST, screen.inventory.getItemStack()) && this.inButton((int)mouseX, (int)mouseY) && !screen.isWidgetVisible(3, screen.leftTankSlotWidget) && !screen.isWidgetVisible(4, screen.leftTankSlotWidget))
                {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeByte(screen.inventory.getScreenID()).writeBoolean(!screen.inventory.getAbilityValue());

                    ClientPlayNetworking.send(ModNetwork.ABILITY_SLIDER_ID, buf);
                    screen.playUIClickSound();
                    return true;
                }
            }
        }
        else if(screen.inventory.getScreenID() == Reference.WEARABLE_SCREEN_ID)
        {
            if(!screen.toolSlotsWidget.isCoveringAbility())
            {
                if(BackpackAbilities.isOnList(BackpackAbilities.ITEM_ABILITIES_LIST, screen.inventory.getItemStack()) && this.inButton((int)mouseX, (int)mouseY) && !screen.isWidgetVisible(3, screen.leftTankSlotWidget) && !screen.isWidgetVisible(4, screen.leftTankSlotWidget))
                {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeByte(screen.inventory.getScreenID()).writeBoolean(!screen.inventory.getAbilityValue());

                    ClientPlayNetworking.send(ModNetwork.ABILITY_SLIDER_ID, buf);
                    screen.playUIClickSound();
                    return true;
                }
            }
        }
        return false;
    }
}