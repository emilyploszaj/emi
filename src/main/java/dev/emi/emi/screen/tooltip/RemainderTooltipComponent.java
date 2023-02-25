package dev.emi.emi.screen.tooltip;

import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.joml.Matrix4f;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;

public class RemainderTooltipComponent implements TooltipComponent {
	public List<Remainder> remainders = Lists.newArrayList();

	public RemainderTooltipComponent(EmiIngredient ingredient) {
		outer:
		for (EmiStack stack : ingredient.getEmiStacks()) {
			for (Remainder remainder : remainders) {
				if (remainder.remainder.isEqual(stack.getRemainder())) {
					remainder.inputs.add(stack);
					continue outer;
				}
			}
			if (!stack.getRemainder().isEmpty()) {
				remainders.add(new Remainder(stack, stack.getRemainder()));
			}
		}
	}

	@Override
	public int getHeight() {
		return 18 * remainders.size();
	}

	@Override
	public int getWidth(TextRenderer var1) {
		return 18 * 3;
	}
	
	@Override
	public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
		matrices.push();
		matrices.translate(0, 0, z);
		for (int i = 0; i < remainders.size(); i++) {
			Remainder remainder = remainders.get(i);
			remainder.inputs.get(0).render(matrices, x, y + 18 * i, MinecraftClient.getInstance().getTickDelta(), EmiIngredient.RENDER_ICON);
			remainder.remainder.render(matrices, x + 18 * 2, y + 18 * i, MinecraftClient.getInstance().getTickDelta(), -1);
		}
		matrices.pop();
	}

	@Override
	public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix, Immediate vertexConsumers) {
		for (int i = 0; i < remainders.size(); i++) {
			Remainder remainder = remainders.get(i);
			boolean chanced = remainder.remainder.getChance() != 1;
			textRenderer.draw("->", x + 20, y + 5 + i * 18 - (chanced ? 4 : 0), 0xffffff, true, matrix, vertexConsumers, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
			if (chanced) {
				String text = EmiTooltip.TEXT_FORMAT.format(remainder.remainder.getChance() * 100) + "%";
				int tx = textRenderer.getWidth(text);
				textRenderer.draw(text, x + 27 - tx / 2, y + 9 + i * 18, Formatting.GOLD.getColorValue(),
					true, matrix, vertexConsumers, false, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
			}
		}
	}

	private static class Remainder {
		public final List<EmiStack> inputs = Lists.newArrayList();
		public final EmiStack remainder;

		public Remainder(EmiStack input, EmiStack remainder) {
			inputs.add(input);
			this.remainder = remainder;
		}
	}
}
