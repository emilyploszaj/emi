package dev.emi.emi.mixin.api;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.mixinsupport.annotation.Extends;
import dev.emi.emi.mixinsupport.annotation.InvokeTarget;
import dev.emi.emi.mixinsupport.annotation.StripConstructors;
import dev.emi.emi.mixinsupport.annotation.Transform;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;

@StripConstructors
@Transform(visibility = "PUBLIC")
@Extends("dev/emi/emi/api/stack/EmiStack$Entry")
@Mixin(targets = "dev/emi/emi/api/stack/FluidEmiStack$FluidEntry")
public abstract class FluidEmiStackEntryMixin {
	private FluidVariant variant;

	@InvokeTarget(name = "<init>", owner = "super")
	abstract void constructor(Object object);

	@Transform(name = "<init>")
	public void constructor(FluidVariant variant) {
		constructor((Object) variant);
		this.variant = variant;
	}

	public Class<ItemVariant> getType() {
		return ItemVariant.class;
	}

	public boolean equals(Object obj) {
		return obj instanceof FluidEmiStackEntryMixin e && variant.getFluid().equals(e.variant.getFluid());
	}
}
