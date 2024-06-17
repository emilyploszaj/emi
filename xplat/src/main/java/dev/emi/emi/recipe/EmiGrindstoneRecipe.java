package dev.emi.emi.recipe;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.recipe.special.EmiAnvilRepairItemRecipe;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class EmiGrindstoneRecipe extends EmiAnvilRepairItemRecipe {
	private static final Identifier BACKGROUND = EmiPort.id("minecraft", "textures/gui/container/grindstone.png");
	private final int uniq = EmiUtil.RANDOM.nextInt();

	public EmiGrindstoneRecipe(Item tool, Identifier id) {
		super(tool, id);
	}
	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.GRINDING;
	}

	@Override
	public int getDisplayWidth() {
		return 116;
	}

	@Override
	public int getDisplayHeight() {
		return 56;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(BACKGROUND, 0, 0, 116, 56, 30, 15);

		widgets.addGeneratedSlot(r -> getItem(r, 0), uniq, 18, 3).drawBack(false);
		widgets.addGeneratedSlot(r -> getItem(r, 1), uniq, 18, 24).drawBack(false);
		widgets.addGeneratedSlot(r -> getItem(r, 2), uniq, 98, 18).drawBack(false).recipeContext(this);
	}
}
