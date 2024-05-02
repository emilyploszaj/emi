package dev.emi.emi.mixin.api;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.mixinsupport.annotation.InvokeTarget;
import dev.emi.emi.mixinsupport.annotation.Transform;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

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

	@InvokeTarget(owner = "dev/emi/emi/api/stack/EmiStack", name = "getEntry",
		desc = "()Ldev/emi/emi/api/stack/EmiStack$Entry;", type = "VIRTUAL")
	private static Object getEmptyEntry(EmiStack stack) { throw new AbstractMethodError(); }

	@Transform(desc = "()Ldev/emi/emi/api/stack/EmiStack$Entry;")
	public Object getEntry() {
		return getEmptyEntry(EmiStack.EMPTY);
	}

	@InvokeTarget(owner = "dev/emi/emi/api/stack/EmiStack$Entry", name = "getType",
		desc = "()Ljava/lang/Class;", type = "VIRTUAL")
	private static Class<?> getType(Object object) { throw new AbstractMethodError(); }

	@Transform(desc = "(Ljava/lang/Class;)Ldev/emi/emi/api/stack/EmiStack$Entry;")
	public Object getEntryOfType(Class<?> clazz) {
		if (getType(getEntry()) == clazz) {
			return getEntry();
		}
		return null;
	}
}
