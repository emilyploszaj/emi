package dev.emi.emi.api.render;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiRenderHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class EmiTexture implements EmiRenderable {
	public static final EmiTexture SLOT = new EmiTexture(EmiRenderHelper.WIDGETS, 0, 0, 18, 18);
	public static final EmiTexture LARGE_SLOT = new EmiTexture(EmiRenderHelper.WIDGETS, 18, 0, 26, 26);
	public static final EmiTexture EMPTY_ARROW = new EmiTexture(EmiRenderHelper.WIDGETS, 44, 0, 24, 17);
	public static final EmiTexture FULL_ARROW = new EmiTexture(EmiRenderHelper.WIDGETS, 44, 17, 24, 17);
	public static final EmiTexture EMPTY_FLAME = new EmiTexture(EmiRenderHelper.WIDGETS, 68, 0, 14, 14);
	public static final EmiTexture FULL_FLAME = new EmiTexture(EmiRenderHelper.WIDGETS, 68, 14, 14, 14);
	public static final EmiTexture PLUS = new EmiTexture(EmiRenderHelper.WIDGETS, 82, 0, 13, 13);
	public static final EmiTexture SHAPELESS = new EmiTexture(EmiRenderHelper.WIDGETS, 95, 0, 16, 13);

	public final Identifier texture;
	public final int u, v, width, height;
	public final int regionWidth, regionHeight, textureWidth, textureHeight;

	public EmiTexture(Identifier texture, int u, int v, int width, int height) {
		this(texture, u, v, width, height, width, height, 256, 256);
	}
	public EmiTexture(Identifier texture, int u, int v, int width, int height,
			int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		this.texture = texture;
		this.u = u;
		this.v = v;
		this.width = width;
		this.height = height;
		this.regionWidth = regionWidth;
		this.regionHeight = regionHeight;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, texture);
		DrawableHelper.drawTexture(matrices, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
	}
}
