package com.tiviacz.travelersbackpack.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.capability.AttachmentUtils;
import com.tiviacz.travelersbackpack.client.model.TravelersBackpackWearableModel;
import com.tiviacz.travelersbackpack.common.recipes.BackpackDyeRecipe;
import com.tiviacz.travelersbackpack.compat.curios.TravelersBackpackCurios;
import com.tiviacz.travelersbackpack.config.TravelersBackpackConfig;
import com.tiviacz.travelersbackpack.init.ModItems;
import com.tiviacz.travelersbackpack.inventory.ITravelersBackpackContainer;
import com.tiviacz.travelersbackpack.util.RenderUtils;
import com.tiviacz.travelersbackpack.util.ResourceUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ElytraItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Triple;

@OnlyIn(Dist.CLIENT)
public class TravelersBackpackLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>
{
    public TravelersBackpackWearableModel model;

    public TravelersBackpackLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer)
    {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer clientPlayer, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        if(TravelersBackpackConfig.CLIENT.disableBackpackRender.get()) return;

        if(AttachmentUtils.isWearingBackpack(clientPlayer))
        {
            ITravelersBackpackContainer inv = AttachmentUtils.getBackpackInv(clientPlayer);

            if(inv != null && !clientPlayer.isInvisible())
            {
                boolean curiosIntegration = TravelersBackpack.enableCurios();

                if(curiosIntegration)
                {
                    if(!TravelersBackpackCurios.renderCurioLayer(clientPlayer))
                    {
                        return;
                    }
                }

                if(!curiosIntegration && !TravelersBackpackConfig.CLIENT.renderBackpackWithElytra.get() && clientPlayer.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ElytraItem) return;

                renderLayer(poseStack, bufferIn, packedLightIn, clientPlayer, inv, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        }
    }

    private void renderLayer(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, AbstractClientPlayer clientPlayer, ITravelersBackpackContainer container, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
    {
        model = new TravelersBackpackWearableModel(clientPlayer, bufferIn, TravelersBackpackBlockEntityRenderer.createTravelersBackpack(true).bakeRoot());
        boolean flag = container.getItemStack().getItem() == ModItems.QUARTZ_TRAVELERS_BACKPACK.get() || container.getItemStack().getItem() == ModItems.SNOW_TRAVELERS_BACKPACK.get();

        if(container.getItemStack().isEmpty()) return;

        ResourceLocation loc = ResourceUtils.getBackpackTexture(container.getItemStack().getItem());

        boolean isColorable = false;
        boolean isCustomSleepingBag = false;

        if(container.getItemStack().getTag() != null && container.getItemStack().getItem() == ModItems.STANDARD_TRAVELERS_BACKPACK.get())
        {
            if(BackpackDyeRecipe.hasColor(container.getItemStack()))
            {
                isColorable = true;
                loc = new ResourceLocation(TravelersBackpack.MODID, "textures/model/dyed.png");
            }
        }

        if(container.getItemStack().getTag() != null)
        {
            if(container.getItemStack().getTag().contains(ITravelersBackpackContainer.SLEEPING_BAG_COLOR))
            {
                isCustomSleepingBag = true;
            }
        }

        VertexConsumer vertexConsumer = bufferIn.getBuffer(flag ? RenderType.entityTranslucentCull(loc) : RenderType.entitySolid(loc));

        poseStack.pushPose();

        if(clientPlayer.isCrouching())
        {
            poseStack.translate(0D, -0.155D, 0.025D);
        }

        this.getParentModel().copyPropertiesTo(model);
        model.setupAngles(this.getParentModel());

        poseStack.translate(0, 0.175, 0.325);
        poseStack.scale(0.85F, 0.85F, 0.85F);

        if(isColorable)
        {
            Triple<Float, Float, Float> rgb = RenderUtils.intToRGB(BackpackDyeRecipe.getColor(container.getItemStack()));
            model.renderToBuffer(poseStack, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, rgb.getLeft(), rgb.getMiddle(), rgb.getRight(), 1.0F);

            loc = new ResourceLocation(TravelersBackpack.MODID, "textures/model/dyed_extras.png");
            vertexConsumer = bufferIn.getBuffer(RenderType.entityCutout(loc));
        }

        model.renderToBuffer(poseStack, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        if(isCustomSleepingBag)
        {
            loc = ResourceUtils.getSleepingBagTexture(container.getSleepingBagColor());
        }
        else
        {
            loc = ResourceUtils.getDefaultSleepingBagTexture();
        }

        vertexConsumer = bufferIn.getBuffer(RenderType.entityCutout(loc));
        model.renderToBuffer(poseStack, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.25F);

        poseStack.popPose();
    }
}