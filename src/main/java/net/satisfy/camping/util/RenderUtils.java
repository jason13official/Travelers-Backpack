package net.satisfy.camping.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Matrix4f;

public class RenderUtils
{
    public static void renderScreenTank(DrawContext context, SingleVariantStorage<FluidVariant> fluidStorage, double x, double y, double z, double height, double width)
    {
        renderScreenTank(context, fluidStorage.getResource(), fluidStorage.getCapacity(), fluidStorage.getAmount(), x, y, z, height, width);
    }

    public static void renderScreenTank(DrawContext context, FluidVariant fluidVariant, long capacity, long amount, double x, double y, double z, double height, double width)
    {
        if(fluidVariant == null || fluidVariant.getFluid() == null || amount <= 0)
        {
            return;
        }

        Sprite icon = FluidVariantRendering.getSprite(fluidVariant);

        if(icon == null)
        {
            icon = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(MissingSprite.getMissingSpriteId());
        }

        int renderAmount = (int) Math.max(Math.min(height, amount * height / capacity), 1);
        int posY = (int) (y + height - renderAmount);

        int color = FluidVariantRendering.getColor(fluidVariant);

        context.getMatrices().push();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f, 1);
        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        RenderSystem.disableBlend();

        for(int i = 0; i < width; i += 16)
        {
            for(int j = 0; j < renderAmount; j += 16)
            {
                int drawWidth = (int) Math.min(width - i, 16);
                int drawHeight = Math.min(renderAmount - j, 16);

                int drawX = (int) (x + i);
                int drawY = posY + j;

                float minU;
                float minV;

                minU = icon.getMinU();
                minV = icon.getMinV();

                float maxU = icon.getMaxU();
                float maxV = icon.getMaxV();

                BufferBuilder builder = Tessellator.getInstance().getBuffer();
                Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
                builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                builder.vertex(matrix4f, drawX, drawY + drawHeight, (float)z).texture(minU, minV + (maxV - minV) * (float)drawHeight / 16F).next();
                builder.vertex(matrix4f, drawX + drawWidth, drawY + drawHeight, (float)z).texture(minU + (maxU - minU) * (float)drawWidth / 16F, minV + (maxV - minV) * drawHeight / 16F).next();
                builder.vertex(matrix4f, drawX + drawWidth, drawY, (float)z).texture(minU + (maxU - minU) * drawWidth / 16F, minV).next();
                builder.vertex(matrix4f, drawX, drawY, (float)z).texture(minU, minV).next();
                BufferRenderer.drawWithGlobalProgram(builder.end());
            }
        }

        context.getMatrices().pop();
    }

    private static final float OFFSET = 0.01F;
    private static final float MINY = OFFSET;
    private static final float MIN = 0.125F + OFFSET;
    private static final float MAX = 0.3F - OFFSET;
    private static final float[][][] coordinates = {
            { // DOWN
                    {MIN, MINY, MAX},
                    {MIN, MINY, MIN},
                    {MAX, MINY, MIN},
                    {MAX, MINY, MAX}
            },
            { // UP
                    {MAX, MAX, MAX},
                    {MAX, MAX, MIN},
                    {MIN, MAX, MIN},
                    {MIN, MAX, MAX}
            },
            { // NORTH
                    {MIN, MINY, MIN},
                    {MIN, MAX, MIN},
                    {MAX, MAX, MIN},
                    {MAX, MINY, MIN}
            },
            { // SOUTH
                    {MAX, MINY, MAX},
                    {MAX, MAX, MAX},
                    {MIN, MAX, MAX},
                    {MIN, MINY, MAX}
            },
            { // WEST
                    {MIN, MINY, MAX},
                    {MIN, MAX, MAX},
                    {MIN, MAX, MIN},
                    {MIN, MINY, MIN}
            },
            { // EAST
                    {MAX, MINY, MIN},
                    {MAX, MAX, MIN},
                    {MAX, MAX, MAX},
                    {MAX, MINY, MAX}
            }
    };

    public static void renderFluidSides(MatrixStack matrices, VertexConsumerProvider vertexConsumer, float height, FluidVariant fluidVariant, int brightness)
    {
        Triple<Float, Float, Float> colorParts = getFluidVertexBufferColor(fluidVariant);
        float r = colorParts.getLeft();
        float g = colorParts.getMiddle();
        float b = colorParts.getRight();
        float a = 1.0F;

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        for(Direction direction : Direction.values())
        {
            Sprite icon = FluidVariantRendering.getSprite(fluidVariant);

            VertexConsumer renderer = vertexConsumer.getBuffer(RenderLayer.getText(icon.getAtlasId()));

            float[][] c = coordinates[direction.ordinal()];
            float replacedMaxV = (direction == Direction.UP || direction == Direction.DOWN) ? icon.getFrameV(4D) : ((icon.getMaxV() - icon.getMinV()) * height + icon.getMinV());
            float replacedU1 = (direction == Direction.UP || direction == Direction.DOWN) ? icon.getFrameU(4D) : icon.getFrameU(7D);
            float replacedU2 = (direction == Direction.UP || direction == Direction.DOWN) ? icon.getFrameU(8D) : icon.getFrameU(8D);

            renderer.vertex(matrix4f, c[0][0], getHeight(c[0][1], height), c[0][2]).color(r, g, b, a).texture(replacedU1, replacedMaxV).light(brightness).next();
            renderer.vertex(matrix4f, c[1][0], getHeight(c[1][1], height), c[1][2]).color(r, g, b, a).texture(replacedU1, icon.getMinV()).light(brightness).next();
            renderer.vertex(matrix4f, c[2][0], getHeight(c[2][1], height), c[2][2]).color(r, g, b, a).texture(replacedU2, icon.getMinV()).light(brightness).next();
            renderer.vertex(matrix4f, c[3][0], getHeight(c[3][1], height), c[3][2]).color(r, g, b, a).texture(replacedU2, replacedMaxV).light(brightness).next();
        }
    }

    private static float getHeight(float height, float replaceHeight)
    {
        if(height == MAX)
        {
            return replaceHeight;
        }
        return height;
    }

    public static void renderFluidInTank(SingleVariantStorage<FluidVariant> fluidStorage, MatrixStack matrixStackIn, VertexConsumerProvider vertexConsumers, int combinedLightIn, float x, float y, float z)
    {
        matrixStackIn.push();
        matrixStackIn.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180F));

        if(!fluidStorage.isResourceBlank() && !fluidStorage.getResource().isBlank())
        {
            matrixStackIn.translate(x,y,z);
            float height = getTankFillRatio(fluidStorage) * 0.99F;
            RenderUtils.renderFluidSides(matrixStackIn, vertexConsumers, height, fluidStorage.getResource(), combinedLightIn);
        }
        matrixStackIn.pop();
    }

    public static Sprite getBlockIcon(Block block)
    {
        return MinecraftClient.getInstance().getBlockRenderManager().getModels().getModel(block.getDefaultState()).getParticleSprite();
    }

    public static float getTankFillRatio(SingleVariantStorage<FluidVariant> fluidStorage)
    {
        return Math.min(1.0F, ((float)fluidStorage.getAmount()) / (float)fluidStorage.getCapacity()) * 0.5F;
    }

    public static Triple<Float, Float, Float> getFluidVertexBufferColor(FluidVariant fluidVariant)
    {
        int color = FluidVariantRendering.getColor(fluidVariant);
        return intToRGB(color);
    }

    public static Triple<Float, Float, Float> intToRGB(int color)
    {
        float red, green, blue;
        red = (float)(color >> 16 & 255) / 255.0F;
        green = (float)(color >> 8 & 255) / 255.0F;
        blue = (float)(color & 255) / 255.0F;
        return Triple.of(red, green, blue);
    }
}