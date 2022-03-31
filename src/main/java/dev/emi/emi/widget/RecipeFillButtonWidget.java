package dev.emi.emi.widget;

import java.util.List;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.screen.RecipeScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.TranslatableText;

public class RecipeFillButtonWidget extends RecipeButtonWidget {
	private final boolean canFill;

	public RecipeFillButtonWidget(int x, int y, EmiRecipe recipe) {
		super(x, y, 24, 64, recipe);
		MinecraftClient client = MinecraftClient.getInstance();
		HandledScreen<?> hs = null;
		if (client.currentScreen instanceof RecipeScreen rs) {
			hs = rs.old;
		} else if (client.currentScreen instanceof HandledScreen<?> s) {
			hs = s;
		}
		canFill = hs != null && recipe.canFill(hs);
	}

	@Override
	public int getTextureOffset(int mouseX, int mouseY) {
		if (!canFill) {
			return 24;
		}
		return super.getTextureOffset(mouseX, mouseY);
	}

	@Override
	public List<TooltipComponent> getTooltip() {
		return List.of(TooltipComponent.of(new TranslatableText("tooltip.emi.fill_recipe").asOrderedText()));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (canFill) {
			this.playButtonSound();
			EmiApi.performFill(recipe, EmiUtil.isShiftDown());
			return true;
		}
		return false;
	}
}
