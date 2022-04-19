package dev.emi.emi.bom;

import dev.emi.emi.api.stack.EmiIngredient;

public class FlatMaterialCost {
	public EmiIngredient ingredient;
	public int amount;

	public FlatMaterialCost(EmiIngredient ingredient, int amount) {
		this.ingredient = ingredient;
		this.amount = amount;
	}
}
