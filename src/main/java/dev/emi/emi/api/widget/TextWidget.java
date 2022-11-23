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
	private Alignment horizontalAlignment = Alignment.START;
	private Alignment verticalAlignment = Alignment.START;

	public TextWidget(OrderedText text, int x, int y, int color, boolean shadow) {
		this.text = text;
		this.x = x;
		this.y = y;
		this.color = color;
		this.shadow = shadow;
	}

	public TextWidget horizontalAlign(Alignment alignment) {
		this.horizontalAlignment = alignment;
		return this;
	}

	public TextWidget verticalAlign(Alignment alignment) {
		this.verticalAlignment = alignment;
		return this;
	}

	@Override
	public Bounds getBounds() {
		return new Bounds(x, y, CLIENT.textRenderer.getWidth(text), 10);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		matrices.push();
		int xOff = horizontalAlignment.offset(CLIENT.textRenderer.getWidth(text));
		int yOff = verticalAlignment.offset(CLIENT.textRenderer.fontHeight);
		matrices.translate(xOff, yOff, 300);
		if (shadow) {
			CLIENT.textRenderer.drawWithShadow(matrices, text, x, y, color);
		} else {
			CLIENT.textRenderer.draw(matrices, text, x, y, color);
		}
		matrices.pop();
	}
}
