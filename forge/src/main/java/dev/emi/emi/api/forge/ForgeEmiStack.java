package dev.emi.emi.api.forge;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraftforge.fluids.FluidStack;

public final class ForgeEmiStack {
	
	public static EmiStack of(FluidStack stack) {
		return EmiStack.of(stack.getFluid(), stack.getTag(), stack.getAmount());
	}
}
