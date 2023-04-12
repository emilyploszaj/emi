package dev.emi.emi.api.stack;

import net.minecraft.nbt.NbtCompound;

public class Comparison {
	private static final Comparison COMPARE_NBT = Comparison.of((a, b) -> {
		NbtCompound an = a.getNbt();
		NbtCompound bn = b.getNbt();
		if (an == null || bn == null) {
			return an == bn;
		} else {
			return an.equals(bn);
		}
	}); 
	public static final Comparison DEFAULT_COMPARISON = Comparison.of((a, b) -> true);
	private final Predicate predicate;

	private Comparison(Predicate comparator) {
		this.predicate = comparator;
	}

	public static Comparison of(Predicate comparator) {
		return new Comparison(comparator);
	}

	public static Comparison compareNbt() {
		return COMPARE_NBT;
	}

	public boolean compare(EmiStack a, EmiStack b) {
		return predicate.test(a, b);
	}

	public static interface Predicate {
		public boolean test(EmiStack a, EmiStack b);
	}

	@SuppressWarnings("unused")
	private static class Builder {
	}
}
