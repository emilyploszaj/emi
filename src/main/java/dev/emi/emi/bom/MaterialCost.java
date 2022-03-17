package dev.emi.emi.bom;

import dev.emi.emi.api.stack.EmiIngredient;

public class MaterialCost {
	public EmiIngredient ingredient;
	public int amount;

	public MaterialCost(EmiIngredient ingredient, int amount) {
		this.ingredient = ingredient;
		this.amount = amount;
	}
}
