package dev.emi.emi.mixin.api;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.mixinsupport.MixinPlaceholder;
import dev.emi.emi.mixinsupport.annotation.InvokeTarget;
import dev.emi.emi.mixinsupport.annotation.StripConstructors;
import dev.emi.emi.mixinsupport.annotation.Transform;
import net.fabricmc.fabric.api.util.TriState;

@StripConstructors
@Transform(visibility = "PUBLIC")
@Mixin(targets = "dev/emi/emi/api/stack/Comparison$Builder")
public abstract class ComparisonBuilderMixin {
	private TriState amount = TriState.DEFAULT;
	private TriState nbt = TriState.DEFAULT;

	@InvokeTarget(owner = "java/lang/Object", name = "<init>")
	abstract void superConstructor();

	@Transform(name = "<init>")
	private void constructor() {
		superConstructor();
	}

	@Transform(desc = "(Z)Ldev/emi/emi/api/stack/Comparison$Builder;")
	public ComparisonBuilderMixin amount(boolean amount) {
		this.amount = TriState.of(amount);
		return this;
	}

	@Transform(desc = "(Lnet/fabricmc/fabric/api/util/TriState;)Ldev/emi/emi/api/stack/Comparison$Builder;")
	public ComparisonBuilderMixin amount(TriState amount) {
		this.amount = amount;
		return this;
	}

	@Transform(desc = "(Z)Ldev/emi/emi/api/stack/Comparison$Builder;")
	public ComparisonBuilderMixin nbt(boolean nbt) {
		this.nbt = TriState.of(nbt);
		return this;
	}

	@Transform(desc = "(Lnet/fabricmc/fabric/api/util/TriState;)Ldev/emi/emi/api/stack/Comparison$Builder;")
	public ComparisonBuilderMixin nbt(TriState nbt) {
		this.nbt = nbt;
		return this;
	}

	@InvokeTarget(owner = "dev/emi/emi/api/stack/Comparison", name = "<init>",
		desc = "(Ldev/emi/emi/api/stack/Comparison$Predicate;)V", type = "NEW")
	private static Comparison newComparison(Object newDup, Comparison.Predicate predicate) { throw new AbstractMethodError(); }

	public Comparison build() {
		Comparison base;
		if (nbt == TriState.TRUE) {
			base = Comparison.compareNbt();
		} else {
			base = Comparison.DEFAULT_COMPARISON;
		}
		if (amount != TriState.TRUE) {
			return base;
		}
		return newComparison(MixinPlaceholder.NEW_DUP, (a, b) -> a.getAmount() == b.getAmount() && base.compare(a, b));
	}
}
