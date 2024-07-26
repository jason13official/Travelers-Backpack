package net.satisfy.camping.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.satisfy.camping.client.screen.HudOverlay;
import net.satisfy.camping.component.ComponentUtils;
import net.satisfy.camping.inventory.ITravelersBackpackInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class StackPart extends ModelPart
{
    private final PlayerEntity player;
    private final VertexConsumerProvider provider;

    public StackPart(ModelPart parent, PlayerEntity player, VertexConsumerProvider provider)
    {
        super(parent.cuboids, parent.children);
        this.player = player;
        this.provider = provider;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay)
    {
        matrices.push();
        this.rotate(matrices);
        render(this.player, matrices, this.provider, light, overlay);
        matrices.pop();
    }

    public void render(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay)
    {
        ITravelersBackpackInventory inv = ComponentUtils.getBackpackInv(player);

        List<ItemStack> tools = HudOverlay.getTools(inv.getToolSlotsInventory());

        if(tools.isEmpty()) return;

        ItemStack toolUpper = inv.getToolSlotsInventory().getStack(0);
        ItemStack toolLower = ItemStack.EMPTY;

        if(!toolUpper.isEmpty() && tools.size() > 1)
        {
            toolLower = inv.getToolSlotsInventory().getStack(tools.size() - 1);
        }

        if(!toolUpper.isEmpty())
        {
            BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModel(toolUpper, player.getWorld(), player, 0);
            //model = ForgeHooksClient.handleCameraTransforms(matrices, model, ItemCameraTransforms.TransformType.NONE, false);

            matrices.push();

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            matrices.translate(0.05, 0.075, 0.27);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180F));
            matrices.scale(0.65F, 0.65F, 0.65F);

            MinecraftClient.getInstance().getTextureManager().bindTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
            MinecraftClient.getInstance().getItemRenderer()
                    .renderItem(toolUpper, ModelTransformationMode.NONE, false, matrices, vertices, light, overlay, model);

            matrices.pop();
        }

        if(!toolLower.isEmpty())
        {
            BakedModel model = MinecraftClient.getInstance().getItemRenderer().getModel(toolLower, player.getWorld(), player, 0);
            //model = ForgeHooksClient.handleCameraTransforms(matrices, model, ModelTransformation.Mode.NONE, false);

            matrices.push();

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            matrices.translate(-0.35, 0.95, 0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45F));
            matrices.scale(0.65F, 0.65F, 0.65F);

            MinecraftClient.getInstance().getTextureManager().bindTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
            MinecraftClient.getInstance().getItemRenderer()
                    .renderItem(toolLower, ModelTransformationMode.NONE, false, matrices, vertices, light, overlay, model);
            matrices.pop();
        }
    }
}