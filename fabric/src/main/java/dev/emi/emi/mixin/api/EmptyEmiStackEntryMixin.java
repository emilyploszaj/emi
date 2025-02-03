package dev.emi.emi.mixin.api;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.mixinsupport.annotation.Extends;
import dev.emi.emi.mixinsupport.annotation.InvokeTarget;
import dev.emi.emi.mixinsupport.annotation.StripConstructors;
import dev.emi.emi.mixinsupport.annotation.Transform;
import net.minecraft.item.ItemStack;

@StripConstructors
@Transform(visibility = "PUBLIC")
@Extends("dev/emi/emi/api/stack/EmiStack$Entry")
@Mixin(targets = "dev/emi/emi/api/stack/EmptyEmiStack$EmptyEntry")
public abstract class EmptyEmiStackEntryMixin {

	@InvokeTarget(name = "<init>", owner = "super")
	abstract void constructor(Object object);

	@Transform(name = "<init>")
	public void constructor() {
		constructor(ItemStack.EMPTY);
	}

	public Class<ItemStack> getType() {
		return ItemStack.class;
	}
}
