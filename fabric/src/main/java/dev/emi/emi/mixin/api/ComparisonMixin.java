package dev.emi.emi.mixin.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.mixinsupport.MixinPlaceholder;
import dev.emi.emi.mixinsupport.annotation.InvokeTarget;
import dev.emi.emi.mixinsupport.annotation.Transform;
import net.fabricmc.fabric.api.util.TriState;

@Mixin(Comparison.class)
public class ComparisonMixin {
	public TriState amount;
	public TriState nbt;

	@Inject(at = @At("RETURN"), method = "<init>")
	private void constructor(CallbackInfo info) {
		amount = TriState.FALSE;
		nbt = TriState.FALSE;
		newBuilder(MixinPlaceholder.NEW_DUP);
	}

	public TriState resolveAmount(Comparison other) {
		return resolvePair(amount, ((ComparisonMixin) (Object) other).amount);
	}

	public TriState resolveNbt(Comparison other) {
		return resolvePair(nbt, ((ComparisonMixin) (Object) other).nbt);
	}

	private TriState resolvePair(TriState a, TriState b) {
		if (a == TriState.TRUE || b == TriState.TRUE) {
			return TriState.TRUE;
		} else if (a == TriState.FALSE || b == TriState.FALSE) {
			return TriState.FALSE;
		}
		return TriState.DEFAULT;
	}

	@InvokeTarget(owner = "dev/emi/emi/api/stack/Comparison$Builder", name = "<init>",
		desc = "()V", type = "NEW")
	private static Object newBuilder(Object newDup) { throw new AbstractMethodError(); }

	@Transform(desc = "()Ldev/emi/emi/api/stack/Comparison$Builder;")
	public Object copy() {
		return newBuilder(MixinPlaceholder.NEW_DUP);
	}
}
