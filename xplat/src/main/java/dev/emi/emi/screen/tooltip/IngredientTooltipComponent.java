package dev.emi.emi.screen.tooltip;

import java.util.List;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.util.Identifier;

public class IngredientTooltipComponent implements EmiTooltipComponent {
	private static final Identifier TEXTURE = EmiPort.id("emi", "textures/gui/widgets.png");
	private static final int MAX_DISPLAYED = 63;
	private final List<? extends EmiIngredient> ingredients;
	
	public IngredientTooltipComponent(List<? extends EmiIngredient> ingredients) {
		this.ingredients = ingredients;
	}

	public int getStackWidth() {
		if (ingredients.size() < 4) {
			return ingredients.size();
		} else if (ingredients.size() > 16) {
			return 8;
		} else {
			return 4;
		}
	}

	@Override
	public int getHeight() {
		int s = ingredients.size();
		if (s > MAX_DISPLAYED) {
			s = MAX_DISPLAYED;
		}
		return ((s - 1) / getStackWidth() + 1) * 18;
	}

	@Override
	public int getWidth(TextRenderer textRenderer) {
		return 18 * getStackWidth();
	}

	@Override
	public void drawTooltip(EmiDrawContext context, TooltipRenderData render) {
		int sw = getStackWidth();
		for (int i = 0; i < ingredients.size() && i < MAX_DISPLAYED; i++) {
			context.drawStack(ingredients.get(i), i % sw * 18, i / sw * 18);
		}
		if (ingredients.size() > MAX_DISPLAYED) {
			context.resetColor();
			context.drawTexture(TEXTURE, getWidth(render.text) - 14, getHeight() - 8, 0, 192, 9, 3);
		}
	}
}
