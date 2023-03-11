package dev.emi.emi.bom;

import dev.emi.emi.api.stack.EmiIngredient;

public class ChanceMaterialCost extends FlatMaterialCost {
	public long minBatch = 1;
	public float chance;

	public ChanceMaterialCost(EmiIngredient ingredient, long amount, float chance) {
		super(ingredient, amount);
		this.chance = chance;
	}

	public void merge(long amount, float chance) {
		long sum = this.amount + amount;
		this.chance = (this.chance * this.amount + chance * amount) / sum;
		this.amount += amount;
	}

	public void minBatch(long minBatch) {
		this.minBatch = Math.max(this.minBatch, minBatch);
	}

	public long getEffectiveAmount() {
		return Math.max(minBatch, Math.round(amount * chance));
	}
}
