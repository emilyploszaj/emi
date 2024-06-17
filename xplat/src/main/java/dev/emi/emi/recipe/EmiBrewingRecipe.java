package dev.emi.emi.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class EmiBrewingRecipe implements EmiRecipe {
	private static final Identifier BACKGROUND = EmiPort.id("minecraft", "textures/gui/container/brewing_stand.png");
	private static final EmiStack BLAZE_POWDER = EmiStack.of(Items.BLAZE_POWDER);
	private final EmiIngredient input, ingredient;
	private final EmiStack output, input3, output3;
	private final Identifier id;

	public EmiBrewingRecipe(EmiStack input, EmiIngredient ingredient, EmiStack output, Identifier id) {
		this.input = input;
		this.ingredient = ingredient;
		this.output = output;
		this.input3 = input.copy().setAmount(3);
		this.output3 = output.copy().setAmount(3);
		this.id = id;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.BREWING;
	}

	@Override
	public @Nullable Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(input3, ingredient);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(output3);
	}

	@Override
	public int getDisplayWidth() {
		return 120;
	}

	@Override
	public int getDisplayHeight() {
		return 61;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(BACKGROUND, 0, 0, 103, 61, 16, 14);
		widgets.addAnimatedTexture(BACKGROUND, 81, 2, 9, 28, 176, 0, 1000 * 20, false, false, false).tooltip((mx, my) -> {
			return List.of(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("emi.cooking.time", 20))));
		});
		widgets.addAnimatedTexture(BACKGROUND, 47, 0, 12, 29, 185, 0, 700, false, true, false);
		widgets.addTexture(BACKGROUND, 44, 30, 18, 4, 176, 29);
		widgets.addSlot(BLAZE_POWDER, 0, 2).drawBack(false);
		widgets.addSlot(input, 39, 36).drawBack(false);
		widgets.addSlot(ingredient, 62, 2).drawBack(false);
		widgets.addSlot(output, 85, 36).drawBack(false).recipeContext(this);
	}
}
