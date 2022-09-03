package dev.emi.emi.recipe;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.recipe.special.EmiAnvilRepairItemRecipe;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class EmiGrindstoneRecipe extends EmiAnvilRepairItemRecipe {
	private static final Identifier BACKGROUND = new Identifier("minecraft", "textures/gui/container/grindstone.png");
	private final int uniq = EmiUtil.RANDOM.nextInt();
	public EmiGrindstoneRecipe(Item tool) {
		super(tool);
	}
	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.GRINDING;
	}

	@Override
	public int getDisplayWidth() {
		return 130;
	}

	@Override
	public int getDisplayHeight() {
		return 61;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(BACKGROUND, 0, 0, 130, 61, 16, 14);

		int notUniq= uniq;
		widgets.addGeneratedSlot(r -> getItem(r, 0), notUniq, 32, 4).drawBack(false);
		widgets.addGeneratedSlot(r -> getItem(r, 1), notUniq, 32, 24).drawBack(false);
		widgets.addGeneratedSlot(r -> getItem(r, 2), notUniq, 112, 19).drawBack(false).recipeContext(this);

	}
}
