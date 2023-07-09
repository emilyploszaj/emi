package dev.emi.emi.screen.tooltip;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class RemainderTooltipComponent implements EmiTooltipComponent {
	public List<Remainder> remainders = Lists.newArrayList();

	public RemainderTooltipComponent(EmiIngredient ingredient) {
		Map<Integer, List<EmiStack>> tools = Maps.newHashMap();
		outer:
		for (EmiStack stack : ingredient.getEmiStacks()) {
			int damage = getDamageDelta(stack, stack.getRemainder());
			if (damage != 0) {
				tools.computeIfAbsent(damage, i -> Lists.newArrayList()).add(stack);
				continue;
			}
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
		for (Map.Entry<Integer, List<EmiStack>> entry : tools.entrySet()) {
			remainders.add(new Remainder(entry.getValue(), entry.getKey()));
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
	public void drawTooltip(EmiDrawContext context, TooltipRenderData render) {
		for (int i = 0; i < remainders.size(); i++) {
			Remainder remainder = remainders.get(i);
			EmiIngredient input = EmiIngredient.of(remainder.inputs);
			context.drawStack(input, 0, 18 * i, -1 ^ (EmiIngredient.RENDER_AMOUNT | EmiIngredient.RENDER_REMAINDER));
			if (remainder.damage == 0) {
				context.drawStack(remainder.remainder, 18 * 2, 18 * i, -1 ^ EmiIngredient.RENDER_REMAINDER);
			} else {
				context.drawStack(input, 18 * 2, 18 * i, EmiIngredient.RENDER_ICON | EmiIngredient.RENDER_AMOUNT);
				ItemStack is = input.getEmiStacks().get(0).getItemStack().copy();
				is.setDamage(is.getDamage() - remainder.damage);
				MatrixStack view = RenderSystem.getModelViewStack();
				view.push();
				view.multiplyPositionMatrix(context.matrices().peek().getPositionMatrix());
				RenderSystem.applyModelViewMatrix();
				render.item.renderGuiItemOverlay(render.text, is, 18 * 2, 18 * i, "");
				view.pop();
				RenderSystem.applyModelViewMatrix();
				context.drawStack(input, 18 * 2, 18 * i, -1 ^ (EmiIngredient.RENDER_ICON | EmiIngredient.RENDER_AMOUNT | EmiIngredient.RENDER_REMAINDER));
				Text t = remainder.damage > 0 ? EmiPort.literal("+" + remainder.damage, Formatting.GREEN) : EmiPort.literal("" + remainder.damage, Formatting.RED);
				int width = render.text.getWidth(t);
				context.push();
				context.matrices().translate(0, 0, 200);
				context.drawText(t, 42 - width, i * 18);
				context.pop();
			}
		}
	}
	
	@Override
	public void drawTooltipText(TextRenderData text) {
		for (int i = 0; i < remainders.size(); i++) {
			Remainder remainder = remainders.get(i);
			boolean chanced = remainder.chance != 1;
			text.draw(EmiPort.literal("->"), 20, 5 + i * 18 - (chanced ? 4 : 0), 0xffffff, true);
			if (chanced) {
				Text t = EmiPort.literal(EmiTooltip.TEXT_FORMAT.format(remainder.chance * 100) + "%");
				int tx = text.renderer.getWidth(t);
				text.draw(t, 27 - tx / 2, 9 + i * 18, Formatting.GOLD.getColorValue(), false);
			}
		}
	}

	private int getDamageDelta(EmiStack stack, EmiStack remainder) {
		if (remainder.isEqual(stack)) {
			return stack.getItemStack().getDamage() - remainder.getItemStack().getDamage();
		}
		return 0;
	}

	private static class Remainder {
		public final List<EmiStack> inputs = Lists.newArrayList();
		public final EmiStack remainder;
		public int damage = 0;
		public float chance = 1;

		public Remainder(EmiStack input, EmiStack remainder) {
			inputs.add(input);
			this.remainder = remainder;
			chance = remainder.getChance();
		}

		public Remainder(List<EmiStack> inputs, int damage) {
			this.inputs.addAll(inputs);
			this.remainder = EmiStack.EMPTY;
			this.damage = damage;
		}
	}
}
