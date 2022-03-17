package dev.emi.emi.api.stack.comparison;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStack.Comparison;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.item.ItemStack;

public class ItemStackComparison implements Comparison {
	private TriState nbt = TriState.DEFAULT;
	private TriState amount = TriState.DEFAULT;

	@Override
	public boolean areEqual(EmiStack a, EmiStack b) {
		ItemStack as = a.getItemStack();
		ItemStack bs = b.getItemStack();
		if (!as.isItemEqual(bs)) {
			return false;
		}
		if (nbt.get()) {
			if (as.hasNbt() != bs.hasNbt() || (as.hasNbt() && !as.getNbt().equals(bs.getNbt()))) {
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
		ItemStackComparison comp = new ItemStackComparison();
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
