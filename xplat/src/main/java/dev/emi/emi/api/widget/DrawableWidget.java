package dev.emi.emi.api.widget;

import java.util.List;
import java.util.function.BiFunction;

import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

public class DrawableWidget extends Widget implements WidgetTooltipHolder<DrawableWidget> {
	protected final DrawableWidgetConsumer consumer;
	protected final Bounds bounds;
	protected final int x, y;
	protected BiFunction<Integer, Integer, List<TooltipComponent>> tooltipSupplier = (mouseX, mouseY) -> List.of();
	protected MouseClickedHandler mouseClickedHandler = MouseClickedHandler.DEFAULT;
	protected KeyPressedHandler keyPressedHandler = KeyPressedHandler.DEFAULT;

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

	public DrawableWidget mouseClickedHandler(@NotNull DrawableWidget.MouseClickedHandler mouseClickedHandler){
		this.mouseClickedHandler = mouseClickedHandler;
		return this;
	}

	public DrawableWidget keyPressedHandler(@NotNull KeyPressedHandler keyPressedHandler){
		this.keyPressedHandler = keyPressedHandler;
		return this;
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		return mouseClickedHandler.onMouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return keyPressedHandler.onKeyPressed(keyCode, scanCode, modifiers);
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

	@FunctionalInterface
	public interface MouseClickedHandler {

		MouseClickedHandler DEFAULT = (mouseX, mouseY, button) -> false;

		boolean onMouseClicked(int mouseX, int mouseY, int button);
	}

	@FunctionalInterface
	public interface KeyPressedHandler {

		KeyPressedHandler DEFAULT = (keyCode, scanCode, modifiers) -> false;

		boolean onKeyPressed(int keyCode, int scanCode, int modifiers);
	}
}
