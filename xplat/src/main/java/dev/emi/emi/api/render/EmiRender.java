package dev.emi.emi.api.render;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class EmiRender {
	
	public static void renderIngredientIcon(EmiIngredient ingredient, MatrixStack matrices, int x, int y) {
		EmiRenderHelper.renderIngredient(ingredient, EmiDrawContext.wrap(matrices), x, y);
	}

	public static void renderTagIcon(EmiIngredient ingredient, MatrixStack matrices, int x, int y) {
		EmiRenderHelper.renderTag(ingredient, EmiDrawContext.wrap(matrices), x, y);
	}

	public static void renderRemainderIcon(EmiIngredient ingredient, MatrixStack matrices, int x, int y) {
		EmiRenderHelper.renderRemainder(ingredient, EmiDrawContext.wrap(matrices), x, y);
	}

	public static void renderCatalystIcon(EmiIngredient ingredient, MatrixStack matrices, int x, int y) {
		EmiRenderHelper.renderCatalyst(ingredient, EmiDrawContext.wrap(matrices), x, y);
	}
}
