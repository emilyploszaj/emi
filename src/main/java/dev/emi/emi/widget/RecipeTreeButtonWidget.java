package dev.emi.emi.widget;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.bom.BoM;
import net.minecraft.client.gui.tooltip.TooltipComponent;

public class RecipeTreeButtonWidget extends RecipeButtonWidget {

	public RecipeTreeButtonWidget(int x, int y, EmiRecipe recipe) {
		super(x, y, 36, 64, recipe);
	}

	@Override
	public int getTextureOffset(int mouseX, int mouseY) {
		int v = super.getTextureOffset(mouseX, mouseY);
		if (BoM.tree != null && BoM.tree.goal.recipe == recipe) {
			v += 36;
		}
		return v;
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		return List.of(TooltipComponent.of(EmiPort.translatable("tooltip.emi.view_tree").asOrderedText()));
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		BoM.setGoal(recipe);
		this.playButtonSound();
		EmiApi.viewRecipeTree();
		return true;
	}
}
