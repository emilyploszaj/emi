package dev.emi.emi.api.render;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.DrawContext;

public class EmiRender {
	
	public static void renderIngredientIcon(EmiIngredient ingredient, DrawContext draw, int x, int y) {
		EmiRenderHelper.renderIngredient(ingredient, EmiDrawContext.wrap(draw), x, y);
	}

	public static void renderTagIcon(EmiIngredient ingredient, DrawContext draw, int x, int y) {
		EmiRenderHelper.renderTag(ingredient, EmiDrawContext.wrap(draw), x, y);
	}

	public static void renderRemainderIcon(EmiIngredient ingredient, DrawContext draw, int x, int y) {
		EmiRenderHelper.renderRemainder(ingredient, EmiDrawContext.wrap(draw), x, y);
	}

	public static void renderCatalystIcon(EmiIngredient ingredient, DrawContext draw, int x, int y) {
		EmiRenderHelper.renderCatalyst(ingredient, EmiDrawContext.wrap(draw), x, y);
	}
}
