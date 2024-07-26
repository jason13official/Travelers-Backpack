package net.satisfy.camping.client.screen.widget;

import com.mojang.datafixers.util.Pair;
import net.satisfy.camping.client.screen.TravelersBackpackHandledScreen;
import net.satisfy.camping.init.ModNetwork;
import net.satisfy.camping.inventory.sorter.InventorySorter;
import net.satisfy.camping.inventory.sorter.SlotManager;
import net.satisfy.camping.util.BackpackUtils;
import net.satisfy.camping.util.TextUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

public class MemoryWidget extends WidgetBase
{
    public MemoryWidget(TravelersBackpackHandledScreen screen, int x, int y, int width, int height)
    {
        super(screen, x, y, width, height);
        this.isVisible = false;
        this.showTooltip = true;
    }

    @Override
    protected void drawBackground(DrawContext context, MinecraftClient minecraft, int mouseX, int mouseY)
    {
        if(isVisible())
        {
            context.drawTexture(TravelersBackpackHandledScreen.SETTINGS_TRAVELERS_BACKPACK, x, y, 16, isWidgetActive ? 19 : 0, width, height);
        }
    }

    @Override
    public void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY)
    {
        if(isMouseOver(mouseX, mouseY) && showTooltip)
        {
            context.drawTooltip(screen.getTextRenderer(), TextUtils.getTranslatedSplittedText("screen.camping.memory", null), mouseX, mouseY);
        }
    }

    @Override
    public void setWidgetStatus(boolean status)
    {
        super.setWidgetStatus(status);
        screen.sortWidget.setTooltipVisible(!status);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton)
    {
        if(!this.screen.settingsWidget.isWidgetActive()) return false;

        if(screen.inventory.getSlotManager().isSelectorActive(SlotManager.UNSORTABLE))
        {
            return false;
        }

        if(isMouseOver(pMouseX, pMouseY))
        {
            setWidgetStatus(!this.isWidgetActive);

            //Turns slot checking on server
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeByte(screen.inventory.getScreenID()).writeByte(InventorySorter.MEMORY).writeBoolean(BackpackUtils.isShiftPressed());

            ClientPlayNetworking.send(ModNetwork.SORTER_ID, buf);

            //Turns slot checking on client
            PacketByteBuf buf2 = PacketByteBufs.copy(PacketByteBufs.create().writeByte(screen.inventory.getScreenID()).writeBoolean(screen.inventory.getSlotManager().isSelectorActive(SlotManager.MEMORY))).writeIntArray(screen.inventory.getSlotManager().getMemorySlots().stream().mapToInt(Pair::getFirst).toArray());

            for(ItemStack stack : screen.inventory.getSlotManager().getMemorySlots().stream().map(Pair::getSecond).toArray(ItemStack[]::new))
            {
                buf2.writeItemStack(stack);
            }

            ClientPlayNetworking.send(ModNetwork.MEMORY_ID, buf2);
            screen.inventory.getSlotManager().setSelectorActive(SlotManager.MEMORY, !screen.inventory.getSlotManager().isSelectorActive(SlotManager.MEMORY));

            screen.playUIClickSound();
            return true;
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }
}