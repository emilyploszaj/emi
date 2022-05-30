package dev.emi.emi.widget;

import java.util.List;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.TranslatableText;

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
		return List.of(TooltipComponent.of(new TranslatableText("tooltip.emi.set_default").asOrderedText()));
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
		return true;
	}
}
