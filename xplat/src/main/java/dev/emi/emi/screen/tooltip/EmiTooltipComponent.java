package dev.emi.emi.screen.tooltip;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

public interface EmiTooltipComponent extends ClientTooltipComponent {

	int getHeight();

	@Override
	default int getHeight(Font font) {
		return getHeight();
	}

	default void drawTooltip(EmiDrawContext context, TooltipRenderData tooltip) {
	}

	default void drawTooltipText(TextRenderData text) {
	}

	@Override
	default void extractImage(Font textRenderer, int x, int y, int w, int h, GuiGraphicsExtractor raw) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.push();
		context.translate(x, y);
		drawTooltip(context, new TooltipRenderData(textRenderer, x, y));
		context.pop();
	}

	@Override
	default void extractText(GuiGraphicsExtractor raw, Font textRenderer, int x, int y) {
		drawTooltipText(new TextRenderData(textRenderer, x, y, raw));
	}

	public static class TextRenderData {
		private final GuiGraphicsExtractor raw;
		public final Font renderer;
		public final int x, y;

		public TextRenderData(Font renderer, int x, int y, GuiGraphicsExtractor raw) {
			this.renderer = renderer;
			this.x = x;
			this.y = y;
			this.raw = raw;
		}

		public void draw(String text, int x, int y, int color, boolean shadow) {
			draw(EmiPort.literal(text), x, y, color, shadow);
		}

		public void draw(Component text, int x, int y, int color, boolean shadow) {
			raw.text(renderer, text, x + this.x, y + this.y, EmiDrawContext.textColor(color), shadow);
		}
	}

	public static class TooltipRenderData {
		public final Font text;
		public final int x, y;

		public TooltipRenderData(Font text, int x, int y) {
			this.text = text;
			this.x = x;
			this.y = y;
		}
	}
}
