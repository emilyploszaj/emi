package dev.emi.emi.widget;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.runtime.EmiHistory;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;

public class RecipeDefaultButtonWidget extends RecipeButtonWidget {

	public RecipeDefaultButtonWidget(int x, int y, EmiRecipe recipe) {
		super(x, y, 48, 64, recipe);
	}

	@Override
	public int getTextureOffset(int mouseX, int mouseY) {
		int v = super.getTextureOffset(mouseX, mouseY);
		if (BoM.isRecipeEnabled(recipe)) {
			v += 36;
		}
		return v;
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		return List.of(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("tooltip.emi.set_default"))));
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (BoM.isRecipeEnabled(recipe)) {
			BoM.removeRecipe(recipe);
		} else {
			for (EmiStack stack : recipe.getOutputs()) {
				BoM.addRecipe(recipe, stack);
			}
		}
		this.playButtonSound();
		if (RecipeScreen.resolve != null) {
			EmiHistory.pop();
		}
		return true;
	}
}
