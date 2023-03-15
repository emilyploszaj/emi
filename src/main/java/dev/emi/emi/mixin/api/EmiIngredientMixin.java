package dev.emi.emi.mixin.api;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.api.stack.EmiIngredient;

/**
 * Making EmiIngredients mutable introduced several necessary methods for mutation
 * Chance was also added
 * Can be removed in 1.20
 */
@Mixin(EmiIngredient.class)
public interface EmiIngredientMixin {

	default EmiIngredient copy() {
		return (EmiIngredient) this;
	}

	default EmiIngredient setAmount(long amount) {
		return (EmiIngredient) this;
	}
	
	default float getChance() {
		return 1f;
	}

	default EmiIngredient setChance(float chance) {
		return (EmiIngredient) this;
	}
}
