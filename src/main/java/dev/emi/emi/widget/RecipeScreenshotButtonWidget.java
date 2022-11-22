package dev.emi.emi.widget;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiScreenshotRecorder;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.client.gui.tooltip.TooltipComponent;

public class RecipeScreenshotButtonWidget extends RecipeButtonWidget {
	public RecipeScreenshotButtonWidget(int x, int y, EmiRecipe recipe) {
		super(x, y, 60, 64, recipe);
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		return List.of(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("tooltip.emi.recipe_screenshot"))));
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		this.playButtonSound();
		EmiScreenshotRecorder.saveScreenshot(recipe);
		return true;
	}
}
