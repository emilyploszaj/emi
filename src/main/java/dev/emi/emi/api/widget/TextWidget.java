package dev.emi.emi.api.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;

public class TextWidget extends Widget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private final OrderedText text;
	private final int x, y;
	private final int color;
	private final boolean shadow;

	public TextWidget(OrderedText text, int x, int y, int color, boolean shadow) {
		this.text = text;
		this.x = x;
		this.y = y;
		this.color = color;
		this.shadow = shadow;
	}

	@Override
	public Bounds getBounds() {
		return new Bounds(x, y, CLIENT.textRenderer.getWidth(text), 10);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		matrices.push();
		matrices.translate(0, 0, 300);
		if (shadow) {
			CLIENT.textRenderer.drawWithShadow(matrices, text, x, y, color);
		} else {
			CLIENT.textRenderer.draw(matrices, text, x, y, color);
		}
		matrices.pop();
	}
}
