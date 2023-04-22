package dev.emi.emi.screen.tooltip;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RemainderTooltipComponent implements EmiTooltipComponent {
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
	public void drawTooltip(MatrixStack matrices, TooltipRenderData render) {
		for (int i = 0; i < remainders.size(); i++) {
			Remainder remainder = remainders.get(i);
			remainder.inputs.get(0).render(matrices, 0, 18 * i, MinecraftClient.getInstance().getTickDelta(), EmiIngredient.RENDER_ICON);
			remainder.remainder.render(matrices, 18 * 2, 18 * i, MinecraftClient.getInstance().getTickDelta(), -1);
		}
	}
	
	@Override
	public void drawTooltipText(TextRenderData text) {
		for (int i = 0; i < remainders.size(); i++) {
			Remainder remainder = remainders.get(i);
			boolean chanced = remainder.remainder.getChance() != 1;
			text.draw(EmiPort.literal("->"), 20, 5 + i * 18 - (chanced ? 4 : 0), 0xffffff, true);
			if (chanced) {
				Text t = EmiPort.literal(EmiTooltip.TEXT_FORMAT.format(remainder.remainder.getChance() * 100) + "%");
				int tx = text.renderer.getWidth(t);
				text.draw(t, 27 - tx / 2, 9 + i * 18, Formatting.GOLD.getColorValue(), false);
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
