package dev.emi.emi.api.recipe;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.util.Identifier;

public class EmiWorldInteractionRecipe implements EmiRecipe {
	private final Identifier id;
	private final List<WorldIngredient> left, right, outputIngredients;
	private final List<EmiIngredient> inputs, catalysts;
	private final List<EmiStack> outputs;
	private final boolean supportsRecipeTree;
	private final int slotHeight;
	private int width = 125, height;
	private int totalSize, leftSize, rightSize, outputSize;
	private int leftHeight, rightHeight, outputHeight;

	protected EmiWorldInteractionRecipe(Builder builder) {
		this.id = builder.id;
		this.left = builder.left;
		this.right = builder.right;
		this.inputs = Stream.concat(left.stream(), right.stream())
			.filter(i -> !i.catalyst).map(i -> i.stack).toList();
		this.catalysts = Stream.concat(left.stream(), right.stream())
			.filter(i -> i.catalyst).map(i -> i.stack).toList();
		this.outputIngredients = builder.output;
		this.outputs = builder.output.stream().map(i -> (EmiStack) i.stack).toList();
		this.supportsRecipeTree = builder.supportsRecipeTree;
		totalSize = left.size() + right.size() + outputs.size();
		if (totalSize > 5) {
			int[] portions = new int[] {
				left.size(), right.size(), outputs.size()
			};
			int[] sizes = new int[] { 1, 1, 1 };
			for (int i = 0; i < 2; i++) {
				int largest = portions[0];
				int li = 0;
				for (int j = 1; j < 3; j++) {
					if (portions[j] >= largest) {
						largest = portions[j];
						li = j;
					}
				}
				sizes[li]++;
				portions[li] = portions[li] * 2 / 3;
			}
			leftSize = sizes[0];
			rightSize = sizes[1];
			outputSize = sizes[2];
		} else {
			leftSize = left.size();
			rightSize = right.size();
			outputSize = outputs.size();
		}
		leftHeight = (left.size() - 1) / leftSize + 1;
		rightHeight = (right.size() - 1) / rightSize + 1;
		outputHeight = (outputs.size() - 1) / outputSize + 1;
		slotHeight = Math.max(leftHeight, Math.max(rightHeight, outputHeight));
		this.height = slotHeight * 18;
		if (totalSize > 4) {
			width = 134;
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.WORLD_INTERACTION;
	}

	@Override
	public @Nullable Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return inputs;
	}

	@Override
	public List<EmiIngredient> getCatalysts() {
		return catalysts;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return outputs;
	}

	@Override
	public boolean supportsRecipeTree() {
		return this.supportsRecipeTree;
	}

	@Override
	public int getDisplayWidth() {
		return width;
	}

	@Override
	public int getDisplayHeight() {
		return height;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		int lr = leftSize * 18;
		int ol = width - outputSize * 18;
		int rl = (lr + ol) / 2 - rightSize * 9 - 4;
		int rr = rl + rightSize * 18;

		widgets.addTexture(EmiTexture.PLUS, (lr + rl) / 2 - EmiTexture.PLUS.width / 2, -6 + slotHeight * 9);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, (rr + ol) / 2 - EmiTexture.EMPTY_ARROW.width / 2, -8 + slotHeight * 9);

		int yo = (slotHeight - leftHeight) * 9;
		for (int i = 0; i < left.size(); i++) {
			WorldIngredient wi = left.get(i);
			widgets.add(wi.mutator.apply(new SlotWidget(wi.stack, i % leftSize * 18, yo + i / leftSize * 18)));
		}
		
		yo = (slotHeight - rightHeight) * 9;
		for (int i = 0; i < right.size(); i++) {
			WorldIngredient wi = right.get(i);
			widgets.add(wi.mutator.apply(new SlotWidget(wi.stack, rl + i % rightSize * 18, yo + i / rightSize * 18)
				.catalyst(wi.catalyst)));
		}
		
		yo = (slotHeight - outputHeight) * 9;
		for (int i = 0; i < outputIngredients.size(); i++) {
			WorldIngredient wi = outputIngredients.get(i);
			widgets.add(wi.mutator.apply(new SlotWidget(wi.stack, ol + i % outputSize * 18, yo + i / outputSize * 18))
				.recipeContext(this));
		}
	}
	
	public static class Builder {
		private final List<WorldIngredient> left = Lists.newArrayList();
		private final List<WorldIngredient> right = Lists.newArrayList();
		private final List<WorldIngredient> output = Lists.newArrayList();
		private boolean supportsRecipeTree = true;
		private Identifier id = null;

		private Builder() {
		}

		public EmiWorldInteractionRecipe build() {
			if (left.isEmpty()) {
				throw new IllegalStateException("Cannot create a world interaction recipe without a left input");
			} else if (right.isEmpty()) {
				throw new IllegalStateException("Cannot create a world interaction recipe without a right input");
			} else if (output.isEmpty()) {
				throw new IllegalStateException("Cannot create a world interaction recipe without an output");
			} else {
				return new EmiWorldInteractionRecipe(this);
			}
		}

		/**
		 * Assigns an identifier to the recipe.
		 * If not called, the id will be null.
		 */
		public Builder id(Identifier id) {
			this.id = id;
			return this;
		}

		/**
		 * Adds an ingredient to the left side of the plus.
		 * At least one is required.
		 */
		public Builder leftInput(EmiIngredient stack) {
			left.add(new WorldIngredient(stack, false, s -> s));
			return this;
		}

		/**
		 * Adds an ingredient to the left side of the plus.
		 * At least one is required.
		 * @param mutator Provides a way to add attributes to the slot widget, or entirely replace it.
		 */
		public Builder leftInput(EmiIngredient stack, Function<SlotWidget, SlotWidget> mutator) {
			left.add(new WorldIngredient(stack, false, mutator));
			return this;
		}

		/**
		 * Adds an ingredient to the right side of the plus.
		 * At least one is required.
		 * @param catalyst Whether to be not considered a cost in the recipe tree.
		 * 	Will also make the slot have a catalyst symbol.
		 */
		public Builder rightInput(EmiIngredient stack, boolean catalyst) {
			right.add(new WorldIngredient(stack, catalyst, s -> s));
			return this;
		}

		/**
		 * Adds an ingredient to the right side of the plus.
		 * At least one is required.
		 * @param catalyst Whether to be not considered a cost in the recipe tree.
		 * 	Will also make the slot have a catalyst symbol.
		 * @param mutator Provides a way to add attributes to the slot widget, or entirely replace it.
		 */
		public Builder rightInput(EmiIngredient stack, boolean catalyst, Function<SlotWidget, SlotWidget> mutator) {
			right.add(new WorldIngredient(stack, catalyst, mutator));
			return this;
		}

		/**
		 * Adds an output.
		 * At least one is required.
		 */
		public Builder output(EmiStack stack) {
			output.add(new WorldIngredient(stack, false, s -> s));
			return this;
		}

		/**
		 * Adds an output.
		 * At least one is required.
		 * @param mutator Provides a way to add attributes to the slot widget, or entirely replace it.
		 */
		public Builder output(EmiStack stack, Function<SlotWidget, SlotWidget> mutator) {
			output.add(new WorldIngredient(stack, false, mutator));
			return this;
		}

		public Builder supportsRecipeTree(boolean supportsRecipeTree) {
			this.supportsRecipeTree = supportsRecipeTree;
			return this;
		}
	}

	private static record WorldIngredient(EmiIngredient stack, boolean catalyst, Function<SlotWidget, SlotWidget> mutator) {
	}
}
