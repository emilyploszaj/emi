package dev.emi.emi.api.widget;

import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import dev.emi.emi.runtime.EmiDrawContext;

public class DrawableWidget extends Widget implements WidgetTooltipHolder<DrawableWidget> {
	protected final DrawableWidgetConsumer consumer;
	protected final Bounds bounds;
	protected final int x, y;
	protected BiFunction<Integer, Integer, List<ClientTooltipComponent>> tooltipSupplier = (mouseX, mouseY) -> List.of();

	public DrawableWidget(int x, int y, int w, int h, DrawableWidgetConsumer consumer) {
		this.x = x;
		this.y = y;
		this.bounds = new Bounds(x, y, w, h);
		this.consumer = consumer;
	}

	@Override
	public DrawableWidget tooltip(BiFunction<Integer, Integer, List<ClientTooltipComponent>> tooltipSupplier) {
		this.tooltipSupplier = tooltipSupplier;
		return this;
	}

	@Override
	public Bounds getBounds() {
		return bounds;
	}

	@Override
	public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
		return tooltipSupplier.apply(mouseX, mouseY);
	}

	@Override
	public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		context.push();
		context.translate(x, y);
		consumer.render(context.raw(), mouseX, mouseY, delta);
		context.pop();
	}

	public static interface DrawableWidgetConsumer {

		void render(GuiGraphics draw, int mouseX, int mouseY, float delta);
	}
}
