package dev.emi.emi.widget;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;

public class RecipeTreeButtonWidget extends RecipeButtonWidget {

	public RecipeTreeButtonWidget(int x, int y, EmiRecipe recipe) {
		super(x, y, 36, 0, recipe);
	}

	@Override
	public int getTextureOffset(int mouseX, int mouseY) {
		int v = super.getTextureOffset(mouseX, mouseY);
		if (BoM.getTree() != null && BoM.getTree().goal.recipe == recipe) {
			v += 36;
		}
		return v;
	}

	@Override
	public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
		return List.of(TooltipComponent.of(EmiPort.ordered(EmiPort.translatable("tooltip.emi.view_tree"))));
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (EmiInput.isShiftDown()) {
			BoM.addGoal(recipe);
		} else {
			BoM.setGoal(recipe);
		}
		this.playButtonSound();
		EmiApi.viewRecipeTree();
		return true;
	}

 	@Override
 	public void render(DrawContext raw, int mouseX, int mouseY, float delta) {
		EmiDrawContext context = EmiDrawContext.wrap(raw);
		context.resetColor();
		int u = EmiInput.isShiftDown() ? 24 : this.u;
		int v = this.v + getTextureOffset(mouseX, mouseY);
		context.drawTexture(EmiRenderHelper.BUTTONS, x, y, 12, 12, u, v, 12, 12, 256, 256);
	}
}
