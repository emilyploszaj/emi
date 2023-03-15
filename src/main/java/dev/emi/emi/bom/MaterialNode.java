package dev.emi.emi.bom;

import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;

public class MaterialNode {
	public final EmiIngredient ingredient;
	public @Nullable EmiRecipe recipe;
	public @Nullable List<MaterialNode> children;
	public float consumeChance = 1, produceChance = 1;
	public long amount = 1;
	public long divisor = 1;
	// Should these be decoupled from material nodes?
	public FoldState state = FoldState.EXPANDED;
	public ProgressState progress = ProgressState.UNSTARTED;
	public long neededBatches = 0;

	public MaterialNode(EmiIngredient ingredient) {
		this.amount = ingredient.getAmount();
		this.ingredient = ingredient.copy().setAmount(1).setChance(1);
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
		produceChance = 1;
		if (recipe == null) {
			return;
		}
		this.recipe = recipe;
		divisor = 1;
		for (EmiStack stack : recipe.getOutputs()) {
			if (stack.equals(ingredient)) {
				divisor = stack.getAmount();
				produceChance = stack.getChance();
				break;
			}
		}
		this.children = Lists.newArrayList();
		outer:
		for (EmiIngredient i : recipe.getInputs()) {
			for (MaterialNode node : children) {
				if (EmiIngredient.areEqual(i, node.ingredient)) {
					node.amount += i.getAmount();
					continue outer;
				}
			}
			if (!i.isEmpty()) {
				MaterialNode node = new MaterialNode(i);
				node.consumeChance = i.getChance();
				children.add(node);
			}
		}
	}
}
