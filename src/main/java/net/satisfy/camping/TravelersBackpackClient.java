package net.satisfy.camping;

import net.satisfy.camping.client.renderer.RenderData;
import net.satisfy.camping.client.renderer.TravelersBackpackBlockEntityRenderer;
import net.satisfy.camping.client.renderer.TravelersBackpackEntityFeature;
import net.satisfy.camping.client.renderer.TravelersBackpackFeature;
import net.satisfy.camping.client.screen.HudOverlay;
import net.satisfy.camping.client.screen.TravelersBackpackHandledScreen;
import net.satisfy.camping.client.screen.tooltip.BackpackTooltipComponent;
import net.satisfy.camping.client.screen.tooltip.BackpackTooltipData;
import net.satisfy.camping.compat.craftingtweaks.TravelersBackpackCraftingGridProvider;
import net.satisfy.camping.fluids.milk.MilkFluidVariantAttributeHandler;
import net.satisfy.camping.fluids.potion.PotionFluidVariantAttributeHandler;
import net.satisfy.camping.fluids.potion.PotionFluidVariantRenderHandler;
import net.satisfy.camping.handlers.KeybindHandler;
import net.satisfy.camping.init.*;
import net.satisfy.camping.util.Reference;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class TravelersBackpackClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        //Handled Screens
        HandledScreens.register(ModScreenHandlerTypes.TRAVELERS_BACKPACK_BLOCK_ENTITY, TravelersBackpackHandledScreen::new);
        HandledScreens.register(ModScreenHandlerTypes.TRAVELERS_BACKPACK_ITEM, TravelersBackpackHandledScreen::new);

        //BlockEntity renderer
        BlockEntityRendererFactories.register(ModBlockEntityTypes.TRAVELERS_BACKPACK_BLOCK_ENTITY_TYPE, TravelersBackpackBlockEntityRenderer::new);

        //Feature renderers
        registerFeatureRenderers();

        //Builtin Item Renderer
        registerBuiltinItemRenderer();

        //Hud Overlay
        registerHudOverlay();

        //Backpack Tooltip
        registerTooltipComponent();

        //Keybindings
        KeybindHandler.initKeybinds();
        KeybindHandler.registerListeners();

        //Client Network
        ModNetwork.initClient();

        //Hose Model Predicate
        registerModelPredicate();

        //Fluid Rendering
        setupFluidRendering();

        //Crafting Tweaks Integration
        if(TravelersBackpack.craftingTweaksLoaded) TravelersBackpackCraftingGridProvider.registerClient();
    }

    public static void registerFeatureRenderers()
    {
        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) ->
        {
            if(entityRenderer instanceof PlayerEntityRenderer renderer)
            {
                registrationHelper.register(new TravelersBackpackFeature(renderer));
            }
            if(Reference.COMPATIBLE_TYPE_ENTRIES.contains(entityType))
            {
                registrationHelper.register(new TravelersBackpackEntityFeature((LivingEntityRenderer<LivingEntity, BipedEntityModel<LivingEntity>>)entityRenderer));
            }
        });
    }

    public static void registerBuiltinItemRenderer()
    {
        for(Item item : ModItems.BACKPACKS)
        {
            BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, mode, matrices, vertexConsumers, light, overlay)
                    -> TravelersBackpackBlockEntityRenderer.renderByItem(new RenderData(stack, stack.hasNbt()), matrices, vertexConsumers, light, overlay));
        }
    }

    public static void registerHudOverlay()
    {
        HudRenderCallback.EVENT.register(HudOverlay::render);
    }

    public static void setupFluidRendering()
    {
        FluidRenderHandlerRegistry.INSTANCE.register(ModFluids.POTION_STILL, ModFluids.POTION_FLOWING, new SimpleFluidRenderHandler(
                new Identifier(TravelersBackpack.MODID, "block/potion_still"),
                new Identifier(TravelersBackpack.MODID, "block/potion_flow"),
                13458603
        ));

        FluidRenderHandlerRegistry.INSTANCE.register(ModFluids.MILK_STILL, ModFluids.MILK_FLOWING, new SimpleFluidRenderHandler(
                new Identifier(TravelersBackpack.MODID, "block/milk_still"),
                new Identifier(TravelersBackpack.MODID, "block/milk_flow"),
                0xFFFFFFFF
        ));

        FluidVariantAttributes.register(ModFluids.POTION_STILL, new PotionFluidVariantAttributeHandler());
        FluidVariantAttributes.register(ModFluids.POTION_FLOWING, new PotionFluidVariantAttributeHandler());
        FluidVariantRendering.register(ModFluids.POTION_STILL, new PotionFluidVariantRenderHandler());
        FluidVariantRendering.register(ModFluids.POTION_FLOWING, new PotionFluidVariantRenderHandler());

        FluidVariantAttributes.register(ModFluids.MILK_STILL, new MilkFluidVariantAttributeHandler());
        FluidVariantAttributes.register(ModFluids.MILK_FLOWING, new MilkFluidVariantAttributeHandler());

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), ModFluids.POTION_STILL, ModFluids.POTION_FLOWING);
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), ModFluids.MILK_STILL, ModFluids.MILK_FLOWING);
    }

    public static void registerTooltipComponent()
    {
        TooltipComponentCallback.EVENT.register((data ->
        {
            if(data instanceof BackpackTooltipData)
            {
                return new BackpackTooltipComponent((BackpackTooltipData)data);
            }
            return null;
        }));
    }

    public static void registerModelPredicate()
    {
        ModelPredicateProviderRegistry.register(ModItems.HOSE, new Identifier(TravelersBackpack.MODID, "mode"), (itemStack, clientWorld, livingEntity, par) ->
        {
            NbtCompound compound = itemStack.getNbt();
            if(compound == null) return 0.0F;
            else return (float)compound.getInt("Mode") / 10.0F;
        });
    }
}