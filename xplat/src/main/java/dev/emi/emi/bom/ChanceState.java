package dev.emi.emi.bom;

public record ChanceState(float chance, boolean chanced) {
	public static ChanceState DEFAULT = new ChanceState(1, false);
	
	public ChanceState produce(float chance) {
		if (chance == 1) {
			return this;
		}
		return new ChanceState(this.chance / chance, true);
	}
	
	public ChanceState consume(float chance) {
		if (chance == 1) {
			return this;
		}
		return new ChanceState(this.chance * chance, true);
	}
}
