package dev.emi.emi.widget;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiScreenshotRecorder;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

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

		Identifier id = recipe.getId();
		String path;
		if (id == null) {
			path = "unknown-recipe";
		} else {
			// Note that saveScreenshot treats `/`s as indicating subdirectories.
			// We don't want to keep `/` in paths because we want all recipe images in consistent directory locations.
			path = id.getNamespace() + "/" + id.getPath().replace("/", "_");
		}

		int width = recipe.getDisplayWidth() + 8;
		int height = recipe.getDisplayHeight() + 8;
		EmiScreenshotRecorder.saveScreenshot("emi/recipes/" + path, width, height,
			() -> EmiRenderHelper.renderRecipe(recipe, new MatrixStack(), 0, 0, false, -1));

		return true;
	}
}
