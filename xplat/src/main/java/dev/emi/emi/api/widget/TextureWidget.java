package dev.emi.emi.api.widget;

import java.util.List;
import java.util.function.BiFunction;

import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.util.Identifier;

public class TextureWidget extends Widget implements WidgetTooltipHolder<TextureWidget> {
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

	@Override
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
	public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		context.resetColor();
		context.drawTexture(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
	}	
}
