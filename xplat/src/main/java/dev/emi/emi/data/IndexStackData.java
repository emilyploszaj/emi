package dev.emi.emi.data;

import java.util.List;

import dev.emi.emi.api.stack.EmiIngredient;

public record IndexStackData(List<Added> added, List<EmiIngredient> removed) {

	public static record Added(EmiIngredient added, EmiIngredient after) {
	}
}
