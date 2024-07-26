package net.satisfy.camping.client.screen.buttons;

import net.satisfy.camping.client.screen.TravelersBackpackHandledScreen;
import net.satisfy.camping.init.ModNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;

public class SleepingBagButton extends Button
{
    public SleepingBagButton(TravelersBackpackHandledScreen screen)
    {
        super(screen, 5, 42 + screen.inventory.getYOffset(), 18, 18);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        if(screen.inventory.hasTileEntity() && !screen.toolSlotsWidget.isCoveringButton())
        {
            this.drawButton(context, mouseX, mouseY, TravelersBackpackHandledScreen.EXTRAS_TRAVELERS_BACKPACK, 19, 0, 0, 0);

            //Fill the bed icon with the color of the sleeping bag
            context.drawTexture(TravelersBackpackHandledScreen.EXTRAS_TRAVELERS_BACKPACK, screen.getX() + x + 1, screen.getY() + y + 1, getBedIconX(screen.inventory.getSleepingBagColor()), getBedIconY(screen.inventory.getSleepingBagColor()), 16, 16);
        }
    }

    @Override
    public void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY) {}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(screen.inventory.hasTileEntity() && !screen.toolSlotsWidget.isCoveringButton())
        {
            if(this.inButton((int) mouseX, (int) mouseY) && !screen.isWidgetVisible(3, screen.leftTankSlotWidget))
            {
                ClientPlayNetworking.send(ModNetwork.DEPLOY_SLEEPING_BAG_ID, PacketByteBufs.create().writeBlockPos(screen.inventory.getPosition()));
                return true;
            }
        }
        return false;
    }

    public int getBedIconX(int colorId)
    {
        return 1 + (colorId <= 7 ? 0 : 19);
    }

    public int getBedIconY(int colorId)
    {
        return 19 + ((colorId % 8) * 17);
    }
}