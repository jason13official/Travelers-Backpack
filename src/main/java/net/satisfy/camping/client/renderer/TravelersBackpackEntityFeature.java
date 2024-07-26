package net.satisfy.camping.client.renderer;

import net.satisfy.camping.client.model.TravelersBackpackWearableModel;
import net.satisfy.camping.component.ComponentUtils;
import net.satisfy.camping.config.TravelersBackpackConfig;
import net.satisfy.camping.init.ModItems;
import net.satisfy.camping.inventory.ITravelersBackpackInventory;
import net.satisfy.camping.util.ResourceUtils;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class TravelersBackpackEntityFeature extends FeatureRenderer<LivingEntity, BipedEntityModel<LivingEntity>>
{
    public TravelersBackpackWearableModel<LivingEntity> model;

    public TravelersBackpackEntityFeature(LivingEntityRenderer<LivingEntity, BipedEntityModel<LivingEntity>> entityRendererIn)
    {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch)
    {
        if(TravelersBackpackConfig.getConfig().client.disableBackpackRender) return;

        if(TravelersBackpackConfig.isOverworldEntityTypePossible(entity) || TravelersBackpackConfig.isNetherEntityTypePossible(entity))
        {
            if(ComponentUtils.isWearingBackpack(entity))
            {
                if(!entity.isInvisible())
                {
                    renderLayer(matrices, vertexConsumers, light, entity);
                }
            }
        }
    }

    private void renderLayer(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntity entity)
    {
        ItemStack stack = ComponentUtils.getComponent(entity).getWearable();
        model = new TravelersBackpackWearableModel(entity, vertexConsumers, TravelersBackpackBlockEntityRenderer.createTravelersBackpack(true).createModel());
        boolean flag = stack.getItem() == ModItems.QUARTZ_TRAVELERS_BACKPACK || stack.getItem() == ModItems.SNOW_TRAVELERS_BACKPACK;

        Identifier id = ResourceUtils.getBackpackTexture(ComponentUtils.getComponent(entity).getWearable().getItem());

        boolean isCustomSleepingBag = false;

        if(stack.getNbt() != null)
        {
            if(stack.getNbt().contains(ITravelersBackpackInventory.SLEEPING_BAG_COLOR))
            {
                isCustomSleepingBag = true;
            }
        }

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(flag ? RenderLayer.getEntityTranslucentCull(id) : RenderLayer.getEntitySolid(id));

        matrices.push();

        if(entity.isSneaking())
        {
            matrices.translate(0D, -0.155D, 0.025D);
        }

        if(entity.isBaby())
        {
            matrices.translate(0.0D, 0.65D, 0.0D);
            matrices.scale(0.5F, 0.5F, 0.5F);
        }

        this.getContextModel().copyBipedStateTo(model);
        model.setupAngles(this.getContextModel());

        matrices.translate(0, 0.175, 0.325);
        matrices.scale(0.85F, 0.85F, 0.85F);

        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);

        if(isCustomSleepingBag)
        {
            id = ResourceUtils.getSleepingBagTexture(stack.getOrCreateNbt().getInt(ITravelersBackpackInventory.SLEEPING_BAG_COLOR));
        }
        else
        {
            id = ResourceUtils.getDefaultSleepingBagTexture();
        }

        vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(id));
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 0.25F);

        matrices.pop();
    }
}