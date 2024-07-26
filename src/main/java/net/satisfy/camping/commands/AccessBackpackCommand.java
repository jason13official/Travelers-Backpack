package net.satisfy.camping.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.satisfy.camping.blockentity.TravelersBackpackBlockEntity;
import net.satisfy.camping.component.ComponentUtils;
import net.satisfy.camping.inventory.screen.TravelersBackpackItemScreenHandler;
import net.satisfy.camping.util.Reference;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class AccessBackpackCommand
{
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment)
    {
        LiteralArgumentBuilder<ServerCommandSource> tbCommand = CommandManager.literal("tb").requires(player -> player.hasPermissionLevel(2));

        tbCommand.then(CommandManager.literal("access")
                .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                        .executes(source -> openTargetBlockEntity(source.getSource(), BlockPosArgumentType.getBlockPos(source, "pos"))))
                .then(CommandManager.argument("target", EntityArgumentType.players())
                        .executes(source -> openTargetInventory(source.getSource(), EntityArgumentType.getPlayer(source, "target")))));

        dispatcher.register(tbCommand);
    }

    public static int openTargetBlockEntity(ServerCommandSource source, BlockPos blockPos) throws CommandSyntaxException
    {
        if(source.getWorld().getBlockEntity(blockPos) instanceof TravelersBackpackBlockEntity)
        {
            //NetworkHooks.openGui(source.getPlayerOrException(), (TravelersBackpackTileEntity)source.getLevel().getBlockEntity(blockPos), blockPos);
            ((TravelersBackpackBlockEntity)source.getWorld().getBlockEntity(blockPos)).openHandledScreen(source.getPlayer());
            source.sendFeedback(() -> Text.literal("Accessing backpack of " + blockPos.toShortString()), true);
            return 1;
        }
        else
        {
            source.sendError(Text.literal("There's no backpack at coordinates " + blockPos.toShortString()));
            return -1;
        }
    }

    public static int openTargetInventory(ServerCommandSource source, ServerPlayerEntity serverPlayer) throws CommandSyntaxException
    {
        ServerPlayerEntity self = source.getPlayer();
        boolean hasBackpack = ComponentUtils.isWearingBackpack(serverPlayer);

        if(hasBackpack)
        {
            if(!self.getWorld().isClient)
            {
                self.openHandledScreen(new ExtendedScreenHandlerFactory()
                {
                    @Override
                    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf)
                    {
                        buf.writeByte(Reference.WEARABLE_SCREEN_ID).writeInt(serverPlayer.getId());
                    }

                    @Override
                    public Text getDisplayName()
                    {
                        return Text.translatable("screen.camping.item");
                    }

                    @Nullable
                    @Override
                    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player)
                    {
                        return new TravelersBackpackItemScreenHandler(syncId, inv, ComponentUtils.getBackpackInv(serverPlayer));
                    }
                });
            }
            source.sendFeedback(() -> Text.literal("Accessing backpack of " + serverPlayer.getDisplayName().getString()), true);
            return 1;
        }
        else
        {
            source.sendError(Text.literal("Can't access backpack"));
            return -1;
        }
    }
}