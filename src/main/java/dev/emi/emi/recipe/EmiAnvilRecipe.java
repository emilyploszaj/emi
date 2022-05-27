package dev.emi.emi.recipe;

import java.util.List;
import java.util.Random;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class EmiAnvilRecipe implements EmiRecipe {
	private final EmiStack tool;
	private final EmiIngredient resource;
	private final int uniq = EmiUtil.RANDOM.nextInt();
	
	public EmiAnvilRecipe(EmiStack tool, EmiIngredient resource) {
		this.tool = tool;
		this.resource = resource;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.ANVIL_REPAIRING;
	}

	@Override
	public Identifier getId() {
		return null;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(tool, resource);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(tool);
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public int getDisplayWidth() {
		return 125;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiRenderHelper.WIDGETS, 27, 3, 13, 13, 82, 0);
		widgets.addTexture(EmiRenderHelper.WIDGETS, 75, 1, 24, 17, 44, 0);
		widgets.addGeneratedSlot(r -> getTool(r, false), uniq, 0, 0);
		widgets.addSlot(resource, 49, 0);
		widgets.addGeneratedSlot(r -> getTool(r, true), uniq, 107, 0).recipeContext(this);
	}

	private EmiStack getTool(Random r, boolean repaired) {
		ItemStack stack = tool.getItemStack().copy();
		int d = r.nextInt(stack.getMaxDamage());
		if (repaired) {
			d -= stack.getMaxDamage() / 4;
			if (d <= 0) {
				return tool;
			}
		}
		stack.setDamage(d);
		return EmiStack.of(stack);
	}
}
