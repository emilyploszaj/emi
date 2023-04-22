package dev.emi.emi.bom;

import dev.emi.emi.api.stack.EmiIngredient;

public class FractionalMaterialCost {
	public EmiIngredient ingredient;
	public float amount;

	public FractionalMaterialCost(EmiIngredient ingredient, float amount) {
		this.ingredient = ingredient;
		this.amount = amount;
	}
}
