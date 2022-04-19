package dev.emi.emi.recipe;

import java.util.List;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiIngredientList;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiTagIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;

public class EmiTagRecipe implements EmiRecipe {
	private final List<EmiStack> stacks;
	public final TagKey<Item> key;

	public EmiTagRecipe(TagKey<Item> key, List<EmiStack> stacks) {
		this.key = key;
		this.stacks = stacks;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaPlugin.TAG;
	}

	@Override
	public Identifier getId() {
		return new Identifier("emi", "tag/item/" + EmiUtil.subId(key.id()));
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(new EmiIngredientList(stacks));
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of();
	}

	@Override
	public int getDisplayHeight() {
		return Math.min((stacks.size() - 1) / 8 + 1, 7) * 18 + 24;
	}

	@Override
	public int getDisplayWidth() {
		return 144;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		EmiIngredient ingredient = new EmiTagIngredient(key, stacks);
		widgets.addSlot(ingredient, 63, 0);
		for (int i = 0; i < stacks.size() && i < 7 * 8; i++) {
			widgets.addSlot(stacks.get(i), i % 8 * 18, i / 8 * 18 + 24)
				.recipeContext(new EmiResolutionRecipe(ingredient, stacks.get(i)));
		}
	}
}
