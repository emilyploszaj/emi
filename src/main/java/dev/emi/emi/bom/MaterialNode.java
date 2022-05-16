package dev.emi.emi.bom;

import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

public class MaterialNode {
	public EmiIngredient ingredient;
	public @Nullable EmiRecipe recipe;
	public @Nullable List<MaterialNode> children;
	public int amount = 1;
	public int divisor = 1;

	public MaterialNode(EmiIngredient ingredient) {
		if (ingredient instanceof EmiStack s) {
			this.amount = s.getAmount();
			ingredient = s.copy().setAmount(1);
		}
		this.ingredient = ingredient;
	}

	public MaterialNode(MaterialNode node) {
		this.ingredient = node.ingredient;
		this.recipe = node.recipe;
		this.amount = node.amount;
		this.divisor = node.divisor;
	}

	public void recalculate(MaterialTree tree) {
		recalculate(tree, Lists.newArrayList());
	}

	private void recalculate(MaterialTree tree, List<EmiRecipe> used) {
		EmiRecipe recipe = this.recipe;
		if (!used.isEmpty()) {
			recipe = tree.getRecipe(ingredient);
		}
		if (recipe != null) {
			if (used.contains(recipe)) {
				return;
			}
			used.add(recipe);
			defineRecipe(recipe);
			for (MaterialNode node : children) {
				node.recalculate(tree, used);
			}
			used.remove(used.size() - 1);
		}
	}

	public void defineRecipe(EmiRecipe recipe) {
		this.recipe = recipe;
		divisor = recipe.getOutputs().get(0).getAmount();
		this.children = Lists.newArrayList();
		outer:
		for (EmiIngredient i : recipe.getInputs()) {
			for (MaterialNode node : children) {
				if (EmiIngredient.areEqual(i, node.ingredient)) {
					node.amount++;
					continue outer;
				}
			}
			if (!i.isEmpty()) {
				children.add(new MaterialNode(i));
			}
		}
	}
}
