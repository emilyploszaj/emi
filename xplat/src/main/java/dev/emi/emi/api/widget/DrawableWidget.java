package dev.emi.emi.api.widget;

import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;

import java.util.List;
import java.util.function.BiFunction;

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
	public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		context.push();
		context.matrices().translate(x, y, 0);
		consumer.render(context.raw(), mouseX, mouseY, delta);
		context.pop();
	}

	public static interface DrawableWidgetConsumer {

		void render(DrawContext draw, int mouseX, int mouseY, float delta);
	}
}
