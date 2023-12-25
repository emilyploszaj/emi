package dev.emi.emi.api.neoforge;

import dev.emi.emi.api.stack.EmiStack;
import net.neoforged.neoforge.fluids.FluidStack;

public final class NeoForgeEmiStack {
	
	public static EmiStack of(FluidStack stack) {
		return EmiStack.of(stack.getFluid(), stack.getTag(), stack.getAmount());
	}
}
