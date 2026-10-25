package dev.emi.emi.screen.tooltip;

import org.joml.Matrix4f;

import dev.emi.emi.EmiPort;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;

public interface EmiTooltipComponent extends ClientTooltipComponent {

	default void drawTooltip(EmiDrawContext context, TooltipRenderData tooltip) {
	}

	default void drawTooltipText(TextRenderData text) {
	}

	@Override
	default void renderImage(Font textRenderer, int x, int y, GuiGraphics raw) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.push();
		context.translate(x, y);
		Minecraft client = Minecraft.getInstance();
		drawTooltip(context, new TooltipRenderData(textRenderer, client.getItemRenderer(), x, y));
		context.pop();
	}

	@Override
	default void renderText(Font textRenderer, int x, int y, Matrix4f matrix, BufferSource vertexConsumers) {
		drawTooltipText(new TextRenderData(textRenderer, x, y, matrix, vertexConsumers));
	}

	public static class TextRenderData {
		private final Matrix4f matrix;
		private final BufferSource vertexConsumers;
		public final Font renderer;
		public final int x, y;
		
		public TextRenderData(Font renderer, int x, int y, Matrix4f matrix, BufferSource vertexConsumers) {
			this.renderer = renderer;
			this.x = x;
			this.y = y;
			this.matrix = matrix;
			this.vertexConsumers = vertexConsumers;
		}

		public void draw(String text, int x, int y, int color, boolean shadow) {
			draw(EmiPort.literal(text), x, y, color, shadow);
		}

		public void draw(Component text, int x, int y, int color, boolean shadow) {
			renderer.drawInBatch(text, x + this.x, y + this.y, color, shadow, matrix, vertexConsumers, DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
		}
	}

	public static class TooltipRenderData {
		public final Font text;
		public final ItemRenderer item;
		public final int x, y;

		public TooltipRenderData(Font text, ItemRenderer item, int x, int y) {
			this.text = text;
			this.item = item;
			this.x = x;
			this.y = y;
		}
	}
}
