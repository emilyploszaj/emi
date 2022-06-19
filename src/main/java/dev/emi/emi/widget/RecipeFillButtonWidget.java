package dev.emi.emi.widget;

import java.util.List;

import dev.emi.emi.EmiClient;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRecipeFiller;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiFillAction;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;

public class RecipeFillButtonWidget extends RecipeButtonWidget {
	private final boolean canFill;
	private Text invalid;

	@SuppressWarnings({"unchecked", "rawtypes"})
	public RecipeFillButtonWidget(int x, int y, EmiRecipe recipe) {
		super(x, y, 24, 64, recipe);
		MinecraftClient client = MinecraftClient.getInstance();
		HandledScreen hs = EmiApi.getHandledScreen();
		EmiRecipeHandler handler = EmiRecipeFiller.getFirstValidHandler(recipe, hs);
		EmiPlayerInventory inv = new EmiPlayerInventory(client.player);
		boolean applicable = handler != null && handler.supportsRecipe(recipe);
		canFill = EmiClient.onServer && applicable && handler.canCraft(recipe, inv, hs);
		if (!canFill) {
			if (!applicable) {
				invalid = EmiPort.translatable("emi.inapplicable");
			} else {
				invalid = handler.getInvalidReason(recipe, inv, hs);
			}
		}
	}

	@Override
	public int getTextureOffset(int mouseX, int mouseY) {
		if (!canFill) {
			return 24;
		}
		return super.getTextureOffset(mouseX, mouseY);
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		if (!EmiClient.onServer) {
			return List.of(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("tooltip.emi.fill_recipe_no_server"))));
		}
		if (canFill) {
			return List.of(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("tooltip.emi.fill_recipe"))));
		} else {
			return List.of(TooltipComponent.of(EmiPort.ordered(invalid)));
		}
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (!EmiClient.onServer) {
			return false;
		}
		if (canFill) {
			this.playButtonSound();
			EmiApi.performFill(recipe, EmiFillAction.FILL, EmiUtil.isShiftDown() ? Integer.MAX_VALUE : 1);
			return true;
		}
		return false;
	}
}
