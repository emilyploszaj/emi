package dev.emi.emi.api;

import dev.emi.emi.api.stack.EmiStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

public final class FabricEmiStack {

	public static EmiStack of(ItemVariant variant) {
		return EmiStack.of(variant.toStack());
	}

	public static EmiStack of(ItemVariant variant, long amount) {
		return EmiStack.of(variant.toStack((int) amount));
	}
	
	public static EmiStack of(FluidVariant variant) {
		return EmiStack.of(variant.getFluid(), variant.getComponents());
	}
	
	public static EmiStack of(FluidVariant variant, long amount) {
		return EmiStack.of(variant.getFluid(), variant.getComponents(), amount);
	}
}
