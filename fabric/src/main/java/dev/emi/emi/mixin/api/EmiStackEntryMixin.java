package dev.emi.emi.mixin.api;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.mixinsupport.annotation.InvokeTarget;
import dev.emi.emi.mixinsupport.annotation.StripConstructors;
import dev.emi.emi.mixinsupport.annotation.Transform;

@StripConstructors
@Transform(visibility = "PUBLIC", flags = Opcodes.ACC_ABSTRACT)
@Mixin(targets = "dev/emi/emi/api/stack/EmiStack$Entry")
public abstract class EmiStackEntryMixin<T> {
	@Transform(flags = Opcodes.ACC_FINAL)
	private T value;

	@InvokeTarget(owner = "java/lang/Object", name = "<init>")
	public abstract void superConstructor();

	@Transform(name = "<init>")
	public void constructor(T value) {
		superConstructor();
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public abstract Class<? extends T> getType();
}
