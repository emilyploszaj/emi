package dev.emi.emi.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget.Alignment;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class EmiCompostingRecipe implements EmiRecipe {
	private static final EmiStack BONE_MEAL = EmiStack.of(Items.BONE_MEAL);
	private final EmiIngredient stack;
	private final float chance;
	private final Identifier id;

	public EmiCompostingRecipe(EmiIngredient stack, float chance, Identifier id) {
		this.stack = stack;
		this.chance = chance;
		this.id = id;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.COMPOSTING;
	}

	@Override
	public @Nullable Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(stack);
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(BONE_MEAL);
	}

	@Override
	public int getDisplayWidth() {
		return 108;
	}

	@Override
	public int getDisplayHeight() {
		return 18;
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addSlot(stack, 0, 0);
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 46, 1);
		widgets.addText(EmiPort.literal(EmiIngredient.TEXT_FORMAT.format(chance * 100) + "%"), 32, 5, -1, true).horizontalAlign(Alignment.CENTER);
		widgets.addText(EmiPort.literal("x7"), 74, 5, -1, true);
		widgets.addSlot(BONE_MEAL, 90, 0).recipeContext(this);
	}
}
