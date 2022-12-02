package dev.emi.emi.api.widget;

import java.util.List;
import java.util.function.BiFunction;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TextureWidget extends Widget {
	protected final Identifier texture;
	protected final int x, y;
	protected final int width, height;
	protected final int u, v;
	protected final int regionWidth, regionHeight;
	protected final int textureWidth, textureHeight;
	private BiFunction<Integer, Integer, List<TooltipComponent>> tooltipSupplier = (mouseX, mouseY) -> List.of();

	public TextureWidget(Identifier texture, int x, int y, int width, int height, int u, int v,
			int regionWidth, int regionHeight, int textureWidth, int textureHeight) {
		this.texture = texture;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.u = u;
		this.v = v;
		this.regionWidth = regionWidth;
		this.regionHeight = regionHeight;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	public TextureWidget(Identifier texture, int x, int y, int width, int height, int u, int v) {
		this(texture, x, y, width, height, u, v, width, height, 256, 256);
	}

	public TextureWidget tooltip(BiFunction<Integer, Integer, List<TooltipComponent>> tooltipSupplier) {
		this.tooltipSupplier = tooltipSupplier;
		return this;
	}

	@Override
	public Bounds getBounds() {
		return new Bounds(x, y, width, height);
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		return tooltipSupplier.apply(mouseX, mouseY);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		EmiPort.setPositionTexShader();
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, this.texture);
		DrawableHelper.drawTexture(matrices, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
	}	
}
