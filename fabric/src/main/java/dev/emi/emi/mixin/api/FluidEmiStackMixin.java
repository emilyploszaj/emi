package dev.emi.emi.mixin.api;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.mixinsupport.MixinPlaceholder;
import dev.emi.emi.mixinsupport.annotation.InvokeTarget;
import dev.emi.emi.mixinsupport.annotation.Transform;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;

@Mixin(FluidEmiStack.class)
public abstract class FluidEmiStackMixin {
	@Final
	@Transform(desc = "Ldev/emi/emi/api/stack/EmiStack$Entry;")
	private Object entry;

	@InvokeTarget(owner = "dev/emi/emi/api/stack/FluidEmiStack$FluidEntry", name = "<init>",
		desc = "(Lnet/fabricmc/fabric/api/transfer/v1/fluid/FluidVariant;)V", type = "NEW")
	private static Object newEntry(Object newDup, FluidVariant variant) { throw new AbstractMethodError(); }

	@Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/fluid/Fluid;Lnet/minecraft/nbt/NbtCompound;J)V")
	private void init(Fluid fluid, @Nullable NbtCompound nbt, long amount, CallbackInfo info) {
		entry = newEntry(MixinPlaceholder.NEW_DUP, FluidVariant.of(fluid, nbt));
	}

	@InvokeTarget(name = "<init>", owner = "this")
	abstract void constructor(Fluid fluid, NbtCompound nbt, long amount);

	@Transform(name = "<init>")
	public void constructor(FluidVariant fluid) {
		constructor(fluid.getFluid(), fluid.getNbt(), 0);
	}

	@Transform(name = "<init>")
	public void constructor(FluidVariant fluid, long amount) {
		constructor(fluid.getFluid(), fluid.getNbt(), amount);
	}

	@Transform(desc = "()Ldev/emi/emi/api/stack/EmiStack$Entry;")
	public Object getEntry() {
		return entry;
	}
}
