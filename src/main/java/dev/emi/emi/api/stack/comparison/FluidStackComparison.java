package dev.emi.emi.api.stack.comparison;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStack.Comparison;
import dev.emi.emi.api.stack.EmiStack.Entry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.util.TriState;

public class FluidStackComparison implements Comparison {
	private TriState nbt = TriState.DEFAULT;
	private TriState amount = TriState.DEFAULT;

	@Override
	public boolean areEqual(EmiStack a, EmiStack b) {
		Entry<FluidVariant> ae = a.getEntryOfType(FluidVariant.class);
		Entry<FluidVariant> be = b.getEntryOfType(FluidVariant.class);
		if (ae == null || be == null) {
			return false;
		}
		FluidVariant av = ae.getValue();
		FluidVariant bv = be.getValue();
		if (av.getFluid() != bv.getFluid()) {
			return false;
		}
		if (nbt.get()) {
			if (av.hasNbt() != bv.hasNbt() || (av.hasNbt() && !av.getNbt().equals(bv.getNbt()))) {
				return false;
			}
		}
		if (amount.get()) {
			if (a.getAmount() != b.getAmount()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Comparison copy() {
		FluidStackComparison comp = new FluidStackComparison();
		comp.nbt = nbt;
		comp.amount = amount;
		return comp;
	}

	@Override
	public Comparison nbt(boolean compare) {
		nbt = TriState.of(compare);
		return this;
	}

	@Override
	public Comparison amount(boolean compare) {
		nbt = TriState.of(compare);
		return this;
	}
	
}
