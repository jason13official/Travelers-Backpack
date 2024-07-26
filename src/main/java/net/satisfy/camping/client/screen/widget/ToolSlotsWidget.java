package net.satisfy.camping.client.screen.widget;

import net.satisfy.camping.client.screen.TravelersBackpackHandledScreen;
import net.satisfy.camping.init.ModNetwork;
import net.satisfy.camping.inventory.SettingsManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.PacketByteBuf;

public class ToolSlotsWidget extends WidgetBase
{
    public ToolSlotsWidget(TravelersBackpackHandledScreen screen, int x, int y, int width, int height)
    {
        super(screen, x, y, width, height);
        this.isVisible = screen.inventory.getToolSlotsInventory().size() > 0;
        this.isWidgetActive = screen.inventory.getSettingsManager().showToolSlots();
    }

    @Override
    void drawBackground(DrawContext context, MinecraftClient minecraft, int mouseX, int mouseY)
    {
        if(isVisible())
        {
            if(!screen.inventory.getSettingsManager().showToolSlots())
            {
                context.drawTexture(TravelersBackpackHandledScreen.SETTINGS_TRAVELERS_BACKPACK, x, y, 64, 0, width, height);
            }
            else
            {
                context.drawTexture(TravelersBackpackHandledScreen.SETTINGS_TRAVELERS_BACKPACK, x, y, 64, 16, width, height);
            }
        }
    }

    @Override
    void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY)
    {

    }

    @Override
    public boolean isSettingsChild()
    {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(isVisible() && isMouseOver(mouseX, mouseY))
        {
            boolean showToolSlots = screen.inventory.getSettingsManager().showToolSlots();
            screen.inventory.getSettingsManager().set(SettingsManager.TOOL_SLOTS, SettingsManager.SHOW_TOOL_SLOTS, (byte)(showToolSlots ? 0 : 1));

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeByte(screen.inventory.getScreenID()).writeByte(SettingsManager.TOOL_SLOTS).writeInt(SettingsManager.SHOW_TOOL_SLOTS).writeByte((byte)(showToolSlots ? 0 : 1));

            ClientPlayNetworking.send(ModNetwork.SETTINGS_ID, buf);

            setWidgetStatus(!showToolSlots);

            this.screen.playUIClickSound();
            return true;
        }
        return false;
    }

    public boolean isCoveringButton()
    {
        return Math.max(3, this.screen.inventory.getRows()) <= screen.inventory.getToolSlotsInventory().size() && isWidgetActive();
    }

    public boolean isCoveringAbility()
    {
        if(screen.inventory.getRows() <= 4)
        {
            return screen.inventory.getToolSlotsInventory().size() >= 2 && isWidgetActive();
        }
        else
        {
            return screen.inventory.getToolSlotsInventory().size() >= 3 && isWidgetActive();
        }
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }
}