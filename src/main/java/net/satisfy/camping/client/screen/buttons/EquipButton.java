package net.satisfy.camping.client.screen.buttons;

import net.satisfy.camping.TravelersBackpack;
import net.satisfy.camping.client.screen.TravelersBackpackHandledScreen;
import net.satisfy.camping.component.ComponentUtils;
import net.satisfy.camping.init.ModNetwork;
import net.satisfy.camping.util.Reference;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class EquipButton extends Button
{
    public EquipButton(TravelersBackpackHandledScreen screen)
    {
        super(screen, 5, 42 + screen.inventory.getYOffset(), 18, 18);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        if(!screen.inventory.hasTileEntity())
        {
            if(!ComponentUtils.isWearingBackpack(screen.getScreenHandler().playerInventory.player) && screen.inventory.getScreenID() == Reference.ITEM_SCREEN_ID && !screen.toolSlotsWidget.isCoveringButton())
            {
                this.drawButton(context, mouseX, mouseY, TravelersBackpackHandledScreen.EXTRAS_TRAVELERS_BACKPACK, 57, 0, 38, 0);
            }
        }
    }

    @Override
    public void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY)
    {
        if(TravelersBackpack.enableTrinkets() && !screen.isWidgetVisible(3, screen.leftTankSlotWidget) && !screen.toolSlotsWidget.isCoveringButton())
        {
            if(!ComponentUtils.isWearingBackpack(screen.getScreenHandler().playerInventory.player) && screen.inventory.getScreenID() == Reference.ITEM_SCREEN_ID)
            {
                if(this.inButton(mouseX, mouseY))
                {
                    context.drawTooltip(screen.getTextRenderer(), Text.translatable("screen.camping.equip_integration"), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(!screen.inventory.hasTileEntity())
        {
            if(!TravelersBackpack.enableTrinkets())
            {
                if(!ComponentUtils.isWearingBackpack(screen.getScreenHandler().playerInventory.player) && screen.inventory.getScreenID() == Reference.ITEM_SCREEN_ID && !screen.isWidgetVisible(3, screen.leftTankSlotWidget) && !screen.toolSlotsWidget.isCoveringButton())
                {
                    if(this.inButton((int) mouseX, (int) mouseY))
                    {
                        PacketByteBuf buf = PacketByteBufs.create();
                        buf.writeBoolean(true);

                        ClientPlayNetworking.send(ModNetwork.EQUIP_BACKPACK_ID, buf);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}