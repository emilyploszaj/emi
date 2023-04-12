package dev.emi.emi;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.api.stack.FluidEmiStack;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;

public class EmiFabric {

	public static void renderFluidStack(FluidEmiStack stack, MatrixStack matrices, int x, int y, float delta) {
		FluidVariant fluid = FluidVariant.of(stack.getKeyOfType(Fluid.class), stack.getNbt());
		Sprite[] sprites = FluidVariantRendering.getSprites(fluid);
		if (sprites == null || sprites.length < 1 || sprites[0] == null) {
			return;
		}
		Sprite sprite = sprites[0];
		EmiPort.setPositionColorTexShader();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, sprite.getAtlasId());
		
		int color = FluidVariantRendering.getColor(fluid);
		float r = ((color >> 16) & 255) / 256f;
		float g = ((color >> 8) & 255) / 256f;
		float b = (color & 255) / 256f;
		
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
		float xMin = (float) x;
		float yMin = (float) y;
		float xMax = xMin + 16;
		float yMax = yMin + 16;
		float uMin = sprite.getMinU();
		float vMin = sprite.getMinV();
		float uMax = sprite.getMaxU();
		float vMax = sprite.getMaxV();
		Matrix4f model = matrices.peek().getPositionMatrix();
		bufferBuilder.vertex(model, xMin, yMax, 1).color(r, g, b, 1).texture(uMin, vMax).next();
		bufferBuilder.vertex(model, xMax, yMax, 1).color(r, g, b, 1).texture(uMax, vMax).next();
		bufferBuilder.vertex(model, xMax, yMin, 1).color(r, g, b, 1).texture(uMax, vMin).next();
		bufferBuilder.vertex(model, xMin, yMin, 1).color(r, g, b, 1).texture(uMin, vMin).next();
		EmiPort.draw(bufferBuilder);
	}
}
