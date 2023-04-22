package dev.emi.emi.api.widget;

import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;

public class DrawableWidget extends Widget implements WidgetTooltipHolder<DrawableWidget> {
	protected final DrawableWidgetConsumer consumer;
	protected final Bounds bounds;
	protected final int x, y;
	protected BiFunction<Integer, Integer, List<TooltipComponent>> tooltipSupplier = (mouseX, mouseY) -> List.of();

	public DrawableWidget(int x, int y, int w, int h, DrawableWidgetConsumer consumer) {
		this.x = x;
		this.y = y;
		this.bounds = new Bounds(x, y, w, h);
		this.consumer = consumer;
	}

	@Override
	public DrawableWidget tooltip(BiFunction<Integer, Integer, List<TooltipComponent>> tooltipSupplier) {
		this.tooltipSupplier = tooltipSupplier;
		return this;
	}

	@Override
	public Bounds getBounds() {
		return bounds;
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		return tooltipSupplier.apply(mouseX, mouseY);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		matrices.push();
		matrices.translate(x, y, 0);
		consumer.render(matrices, mouseX, mouseY, delta);
		matrices.pop();
	}

	public static interface DrawableWidgetConsumer {

		void render(MatrixStack matrices, int mouseX, int mouseY, float delta);
	}
}
