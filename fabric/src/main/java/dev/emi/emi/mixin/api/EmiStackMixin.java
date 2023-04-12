package dev.emi.emi.mixin.api;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmptyEmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.mixinsupport.annotation.Transform;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

@SuppressWarnings("unchecked")
@Mixin(EmiStack.class)
public class EmiStackMixin {
	
	@Transform(visibility = "PUBLIC")
	private static EmiStack of(ItemVariant item) {
		return EmiStack.of(item.toStack(), 1);
	}

	@Transform(visibility = "PUBLIC")
	private static EmiStack of(ItemVariant item, long amount) {
		return EmiStack.of(item.toStack(), amount);
	}

	@Transform(visibility = "PUBLIC")
	private static EmiStack of(FluidVariant fluid) {
		return new FluidEmiStack(fluid.getFluid(), fluid.getNbt());
	}

	@Transform(visibility = "PUBLIC")
	private static EmiStack of(FluidVariant fluid, long amount) {
		return new FluidEmiStack(fluid.getFluid(), fluid.getNbt(), amount);
	}

	public EmiStack.Entry<?> getEntry() {
		return EmptyEmiStack.ENTRY;
	}

	public <T> EmiStack.Entry<T> getEntryOfType(Class<T> clazz) {
		EmiStack.Entry<?> entry = getEntry();
		if (entry.getType() == clazz) {
			return (EmiStack.Entry<T>) entry;
		}
		return null;
	}
}
