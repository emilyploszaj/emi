package dev.emi.emi.mixin.api;

import java.text.DecimalFormat;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.mixinsupport.annotation.AdditionalField;
import net.minecraft.text.Text;

/**
 * Making EmiIngredients mutable introduced several necessary methods for mutation
 * Chance was also added
 * Can be removed in 1.20
 */
@Mixin(EmiIngredient.class)
public interface EmiIngredientMixin {

	@AdditionalField(value = "TEXT_FORMAT", owner = "dev/emi/emi/EmiRenderHelper", name = "TEXT_FORMAT")
	default DecimalFormat copyTextFormat() { throw new AbstractMethodError(); }

	@AdditionalField(value = "EMPTY_TEXT", owner = "dev/emi/emi/EmiRenderHelper", name = "EMPTY_TEXT")
	default Text copyEmptyText() { throw new AbstractMethodError(); }

	default Text getAmountText(double amount) {
		return EmiRenderHelper.getAmountText((EmiIngredient) (Object) this, amount);
	}

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
