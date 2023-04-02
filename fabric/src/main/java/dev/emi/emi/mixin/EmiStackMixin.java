package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

@SuppressWarnings("unused")
@Mixin(EmiStack.class)
public class EmiStackMixin {
	
	@Deprecated
	private static EmiStack of(ItemVariant item) {
		return EmiStack.of(item.toStack(), 1);
	}

	@Deprecated
	private static EmiStack of(ItemVariant item, long amount) {
		return EmiStack.of(item.toStack(), amount);
	}

	@Deprecated
	private static EmiStack of(FluidVariant fluid) {
		return new FluidEmiStack(fluid);
	}

	@Deprecated
	private static EmiStack of(FluidVariant fluid, long amount) {
		return new FluidEmiStack(fluid, amount);
	}
}
