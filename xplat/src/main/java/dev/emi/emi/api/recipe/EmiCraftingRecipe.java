package dev.emi.emi.api.recipe;

import java.util.List;

import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class EmiCraftingRecipe implements EmiRecipe {
	protected final Identifier id;
	protected final Identifier minecraftRecipeId;
	protected final List<EmiIngredient> input;
	protected final EmiStack output;
	public final boolean shapeless;

	public EmiCraftingRecipe(List<EmiIngredient> input, EmiStack output, Identifier id) {
		this(input, output, id, true);
	}

	public EmiCraftingRecipe(List<EmiIngredient> input, EmiStack output, Identifier id, @Nullable Identifier minecraftRecipeId) {
		this(input, output, id, minecraftRecipeId,true);
	}

	public EmiCraftingRecipe(List<EmiIngredient> input, EmiStack output, Identifier id, boolean shapeless) {
		this(input, output, id, null, shapeless);
	}

	public EmiCraftingRecipe(List<EmiIngredient> input, EmiStack output, Identifier id, @Nullable Identifier minecraftRecipeId, boolean shapeless) {
		this.input = input;
		this.output = output;
		this.id = id;
		this.minecraftRecipeId = minecraftRecipeId;
		this.shapeless = shapeless;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.CRAFTING;
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return input;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(output);
	}

	@Override
	public int getDisplayWidth() {
		return 118;
	}

	@Override
	public int getDisplayHeight() {
		return 54;
	}

	public boolean canFit(int width, int height) {
		if (input.size() > 9) {
			return false;
		}
		for (int i = 0; i < input.size(); i++) {
			int x = i % 3;
			int y = i / 3;
			if (!input.get(i).isEmpty() && (x >= width || y >= height)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18);
		if (shapeless) {
			widgets.addTexture(EmiTexture.SHAPELESS, 97, 0);
		}
		int sOff = 0;
		if (!shapeless) {
			if (canFit(1, 3)) {
				sOff -= 1;
			}
			if (canFit(3, 1)) {
				sOff -= 3;
			}
		}
		for (int i = 0; i < 9; i++) {
			int s = i + sOff;
			if (s >= 0 && s < input.size()) {
				widgets.addSlot(input.get(s), i % 3 * 18, i / 3 * 18);
			} else {
				widgets.addSlot(EmiStack.of(ItemStack.EMPTY), i % 3 * 18, i / 3 * 18);
			}
		}
		widgets.addSlot(output, 92, 14).large(true).recipeContext(this);
	}
}
