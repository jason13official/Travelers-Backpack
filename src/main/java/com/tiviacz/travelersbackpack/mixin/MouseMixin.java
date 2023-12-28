package com.tiviacz.travelersbackpack.mixin;

import com.tiviacz.travelersbackpack.handlers.KeybindHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin
{
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci)
    {
        boolean bl = this.client.options.getDiscreteMouseScroll().getValue();
        double d = this.client.options.getMouseWheelSensitivity().getValue();
        double e = (bl ? Math.signum(horizontal) : horizontal) * d;
        double f = (bl ? Math.signum(vertical) : vertical) * d;

        if(KeybindHandler.onMouseScroll(e, f)) ci.cancel();
    }
}