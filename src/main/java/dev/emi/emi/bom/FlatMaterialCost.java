package dev.emi.emi.bom;

import dev.emi.emi.api.stack.EmiIngredient;

public class FlatMaterialCost {
	public EmiIngredient ingredient;
	public long amount;

	public FlatMaterialCost(EmiIngredient ingredient, long amount) {
		this.ingredient = ingredient;
		this.amount = amount;
	}
}
