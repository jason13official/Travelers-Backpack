package net.satisfy.camping.client.renderer;

import net.satisfy.camping.component.ComponentUtils;
import net.satisfy.camping.inventory.ITravelersBackpackInventory;
import net.satisfy.camping.util.RenderUtils;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

public class FluidPart extends ModelPart
{
    private final PlayerEntity player;
    private final VertexConsumerProvider vertices;

    public FluidPart(ModelPart parent, PlayerEntity player, VertexConsumerProvider vertices)
    {
        super(parent.cuboids, parent.children);
        this.player = player;
        this.vertices = vertices;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay)
    {
        matrices.push();
        this.rotate(matrices);
        render(this.player, matrices, this.vertices, light);
        matrices.pop();
    }

    public void render(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertices, int light)
    {
        matrices.push();
        matrices.scale(1F, 1.05F, 1F);

        ITravelersBackpackInventory inv = ComponentUtils.getBackpackInv(player);

        RenderUtils.renderFluidInTank(inv.getRightTank(), matrices, vertices, light,0.24F, -0.55F, -0.235F);
        RenderUtils.renderFluidInTank(inv.getLeftTank(), matrices, vertices, light, -0.66F, -0.55F, -0.235F);

        matrices.pop();
    }
}