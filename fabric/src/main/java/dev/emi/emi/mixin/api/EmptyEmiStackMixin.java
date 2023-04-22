package dev.emi.emi.mixin.api;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.api.stack.EmptyEmiStack;
import dev.emi.emi.mixinsupport.MixinPlaceholder;
import dev.emi.emi.mixinsupport.annotation.InvokeTarget;
import dev.emi.emi.mixinsupport.annotation.Transform;

@Mixin(EmptyEmiStack.class)
public class EmptyEmiStackMixin {
	@Transform(visibility = "PUBLIC", desc = "Ldev/emi/emi/api/stack/EmptyEmiStack$EmptyEntry;")
	private static final Object ENTRY = newEntry(MixinPlaceholder.NEW_DUP);

	@InvokeTarget(owner = "dev/emi/emi/api/stack/EmptyEmiStack$EmptyEntry", name = "<init>",
		desc = "()V", type = "NEW")
	private static Object newEntry(Object newDup) { throw new AbstractMethodError(); }

	@Transform(desc = "()Ldev/emi/emi/api/stack/EmiStack$Entry;")
	public Object getEntry() {
		return ENTRY;
	}
}
