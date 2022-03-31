package dev.emi.emi.widget;

import java.util.List;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.bom.BoM;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.TranslatableText;

public class RecipeTreeButtonWidget extends RecipeButtonWidget {

	public RecipeTreeButtonWidget(int x, int y, EmiRecipe recipe) {
		super(x, y, 36, 64, recipe);
	}

	@Override
	public int getTextureOffset(int mouseX, int mouseY) {
		int v = super.getTextureOffset(mouseX, mouseY);
		if (BoM.goal != null && BoM.goal.recipe == recipe) {
			v += 36;
		}
		return v;
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		return List.of(TooltipComponent.of(new TranslatableText("tooltip.emi.view_tree").asOrderedText()));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		BoM.setGoal(recipe);
		this.playButtonSound();
		EmiApi.viewRecipeTree();
		return true;
	}
}
