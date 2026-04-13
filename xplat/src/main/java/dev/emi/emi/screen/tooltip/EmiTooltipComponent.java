package dev.emi.emi.screen.tooltip;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.text.Text;

public interface EmiTooltipComponent extends TooltipComponent {

	default void drawTooltip(EmiDrawContext context, TooltipRenderData tooltip) {
	}

	default void drawTooltipText(TextRenderData text) {
	}

    @Override
    default void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext raw) {
        EmiDrawContext context = EmiDrawContext.wrap(raw);
        context.push();
        context.matrices().translate(x, y/*, 0*/);
        MinecraftClient client = MinecraftClient.getInstance();
        drawTooltip(context, new TooltipRenderData(textRenderer, client.getItemRenderer(), x, y));
        context.pop();
    }

    @Override
    default void drawText(DrawContext raw, TextRenderer textRenderer, int x, int y) {
        EmiDrawContext context = EmiDrawContext.wrap(raw);
        context.push();
        context.matrices().translate(x, y/*, 0*/);
        drawTooltipText(new TextRenderData(context, textRenderer, x, y));
        context.pop();
    }

	public static class TextRenderData {
        private final EmiDrawContext context;
		public final TextRenderer renderer;
		public final int x, y;
		
		public TextRenderData(EmiDrawContext context, TextRenderer renderer, int x, int y) {
            this.context = context;
            this.renderer = renderer;
			this.x = x;
			this.y = y;
		}

		public void draw(String text, int x, int y, int color, boolean shadow) {
			draw(EmiPort.literal(text), x, y, color, shadow);
		}

		public void draw(Text text, int x, int y, int color, boolean shadow) {
            if (shadow) {
                context.drawTextWithShadow(text, x, y, color);
            } else {
                context.drawText(text, x + this.x, y + this.y, color);
            }
		}
	}

	public static class TooltipRenderData {
		public final TextRenderer text;
		public final ItemRenderer item;
		public final int x, y;

		public TooltipRenderData(TextRenderer text, ItemRenderer item, int x, int y) {
			this.text = text;
			this.item = item;
			this.x = x;
			this.y = y;
		}
	}
}
