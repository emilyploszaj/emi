package dev.emi.emi.api.stack;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.screen.tooltip.IngredientTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class EmiIngredientList implements EmiIngredient {
	private final List<? extends EmiIngredient> ingredients;
	private final List<EmiStack> fullList;

	public EmiIngredientList(List<? extends EmiIngredient> ingredients) {
		if (ingredients.isEmpty()) {
			throw new IllegalArgumentException("EmiIngredientList cannot be empty");
		}
		this.ingredients = ingredients;
		this.fullList = ingredients.stream().flatMap(i -> i.getEmiStacks().stream()).toList();
	}

	@Override
	public List<EmiStack> getEmiStacks() {
		return fullList;
	}

	@Override
	public void render(MatrixStack matrices, int x, int y, float delta) {
		int item = (int) (System.currentTimeMillis() / 1000 % ingredients.size());
		ingredients.get(item).render(matrices, x, y, delta);
		EmiRenderHelper.renderIngredient(this, matrices, x, y);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		List<TooltipComponent> tooltip = Lists.newArrayList();
		tooltip.add(TooltipComponent.of(new LiteralText("Accepts:").asOrderedText()));
		tooltip.add(new IngredientTooltipComponent(ingredients));
		int item = (int) (System.currentTimeMillis() / 1000 % ingredients.size());
		tooltip.addAll(ingredients.get(item).getTooltip());
		return tooltip;
	}
}