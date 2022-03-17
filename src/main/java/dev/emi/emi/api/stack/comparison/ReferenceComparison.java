package dev.emi.emi.api.stack.comparison;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStack.Comparison;

public class ReferenceComparison implements Comparison {
	public static final ReferenceComparison INSTANCE = new ReferenceComparison();

	@Override
	public boolean areEqual(EmiStack a, EmiStack b) {
		return a == b;
	}

	@Override
	public Comparison copy() {
		return this;
	}

	@Override
	public Comparison amount(boolean compare) {
		return this;
	}

	@Override
	public Comparison nbt(boolean compare) {
		return this;
	}
}