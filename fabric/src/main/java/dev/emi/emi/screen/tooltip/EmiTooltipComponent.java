package dev.emi.emi.screen.tooltip;

import org.joml.Matrix4f;

import dev.emi.emi.EmiPort;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public interface EmiTooltipComponent extends TooltipComponent {

	default void drawTooltip(MatrixStack matrices, TooltipRenderData tooltip) {
	}

	default void drawTooltipText(TextRenderData text) {
	}

	@Override
	default void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer) {
		matrices.push();
		matrices.translate(x, y, 0);
		drawTooltip(matrices, new TooltipRenderData(textRenderer, itemRenderer, x, y));
		matrices.pop();
	}

	@Override
	default void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, Immediate vertexConsumers) {
		drawTooltipText(new TextRenderData(textRenderer, x, y, matrix, vertexConsumers));
	}

	public static class TextRenderData {
		private final Matrix4f matrix;
		private final Immediate vertexConsumers;
		public final TextRenderer renderer;
		public final int x, y;
		
		public TextRenderData(TextRenderer renderer, int x, int y, Matrix4f matrix, Immediate vertexConsumers) {
			this.renderer = renderer;
			this.x = x;
			this.y = y;
			this.matrix = matrix;
			this.vertexConsumers = vertexConsumers;
		}

		public void draw(String text, int x, int y, int color, boolean shadow) {
			draw(EmiPort.literal(text), x, y, color, shadow);
		}

		public void draw(Text text, int x, int y, int color, boolean shadow) {
			renderer.draw(text, x + this.x, y + this.y, color, shadow, matrix, vertexConsumers, TextLayerType.NORMAL, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
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
