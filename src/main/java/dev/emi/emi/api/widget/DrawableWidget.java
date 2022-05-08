package dev.emi.emi.api.widget;

import net.minecraft.client.util.math.MatrixStack;

public class DrawableWidget extends Widget {
	private final DrawableWidgetConsumer consumer;
	private final Bounds bounds;
	private final int x, y;

	public DrawableWidget(int x, int y, int w, int h, DrawableWidgetConsumer consumer) {
		this.x = x;
		this.y = y;
		this.bounds = new Bounds(x, y, w, h);
		this.consumer = consumer;
	}

	@Override
	public Bounds getBounds() {
		return bounds;
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
