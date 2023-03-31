package dev.emi.emi.api.stack;

import net.fabricmc.fabric.api.util.TriState;

public class Comparison {
	public static final Comparison DEFAULT_COMPARISON = Comparison.builder().build();
	public final TriState amount;
	public final TriState nbt;

	private Comparison(TriState amount, TriState nbt) {
		this.amount = amount;
		this.nbt = nbt;
	}

	public TriState resolveAmount(Comparison other) {
		return resolvePair(amount, other.amount);
	}

	public TriState resolveNbt(Comparison other) {
		return resolvePair(nbt, other.nbt);
	}

	private TriState resolvePair(TriState a, TriState b) {
		if (a == TriState.TRUE || b == TriState.TRUE) {
			return TriState.TRUE;
		} else if (a == TriState.FALSE || b == TriState.FALSE) {
			return TriState.FALSE;
		}
		return TriState.DEFAULT;
	}

	public Builder copy() {
		return new Builder().amount(amount).nbt(nbt);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private TriState amount = TriState.DEFAULT;
		private TriState nbt = TriState.DEFAULT;

		private Builder() {
		}

		public Builder amount(boolean amount) {
			this.amount = TriState.of(amount);
			return this;
		}

		public Builder amount(TriState amount) {
			this.amount = amount;
			return this;
		}

		public Builder nbt(boolean nbt) {
			this.nbt = TriState.of(nbt);
			return this;
		}

		public Builder nbt(TriState nbt) {
			this.nbt = nbt;
			return this;
		}

		public Comparison build() {
			return new Comparison(amount, nbt);
		}
	}
}
