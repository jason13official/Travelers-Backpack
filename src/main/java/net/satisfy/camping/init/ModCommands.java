package net.satisfy.camping.init;

import net.satisfy.camping.commands.AccessBackpackCommand;
import net.satisfy.camping.commands.ClearBackpackCommand;
import net.satisfy.camping.commands.RestoreBackpackCommand;
import net.satisfy.camping.commands.UnpackBackpackCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ModCommands
{
    public static void registerCommands()
    {
        CommandRegistrationCallback.EVENT.register(AccessBackpackCommand::register);
        CommandRegistrationCallback.EVENT.register(RestoreBackpackCommand::register);
        CommandRegistrationCallback.EVENT.register(ClearBackpackCommand::register);
        CommandRegistrationCallback.EVENT.register(UnpackBackpackCommand::register);
    }
}