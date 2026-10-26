package dev.emi.emi.api.widget;

import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.FormattedCharSequence;

public class TextWidget extends Widget {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	protected final FormattedCharSequence text;
	protected final int x, y;
	protected final int color;
	protected final boolean shadow;
	protected Alignment horizontalAlignment = Alignment.START;
	protected Alignment verticalAlignment = Alignment.START;

	public TextWidget(FormattedCharSequence text, int x, int y, int color, boolean shadow) {
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
		int width = CLIENT.font.width(text);
		int xOff = horizontalAlignment.offset(width);
		int yOff = verticalAlignment.offset(CLIENT.font.lineHeight);
		return new Bounds(x + xOff, y + yOff, width, CLIENT.font.lineHeight);
	}

	@Override
	public void render(GuiGraphicsExtractor draw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(draw);
		context.push();
		int xOff = horizontalAlignment.offset(CLIENT.font.width(text));
		int yOff = verticalAlignment.offset(CLIENT.font.lineHeight);
		context.matrices().translate(xOff, yOff);
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
