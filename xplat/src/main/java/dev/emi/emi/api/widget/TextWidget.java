package dev.emi.emi.api.widget;

import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;

public class TextWidget extends Widget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	protected final OrderedText text;
	protected final int x, y;
	protected final int color;
	protected final boolean shadow;
	protected Alignment horizontalAlignment = Alignment.START;
	protected Alignment verticalAlignment = Alignment.START;

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
		int width = CLIENT.textRenderer.getWidth(text);
		int xOff = horizontalAlignment.offset(width);
		int yOff = verticalAlignment.offset(CLIENT.textRenderer.fontHeight);
		return new Bounds(x + xOff, y + yOff, width, CLIENT.textRenderer.fontHeight);
	}

	@Override
	public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		context.push();
		int xOff = horizontalAlignment.offset(CLIENT.textRenderer.getWidth(text));
		int yOff = verticalAlignment.offset(CLIENT.textRenderer.fontHeight);
		context.matrices().translate(xOff, yOff, 300);
		if (shadow) {
			context.drawTextWithShadow(text, x, y, color);
		} else {
			context.drawText(text, x, y, color);
		}
		context.pop();
	}

	public enum Alignment {
		START, CENTER, END;

		public int offset(int length) {
			return switch (this) {
				case START -> 0;
				case CENTER -> -(length / 2);
				case END -> -length;
			};
		}
	}
}
