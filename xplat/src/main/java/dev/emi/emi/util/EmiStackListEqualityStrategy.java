package dev.emi.emi.util;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.Hash;

import java.util.List;

public final class EmiStackListEqualityStrategy implements Hash.Strategy<List<EmiStack>> {
    public static final EmiStackListEqualityStrategy INSTANCE = new EmiStackListEqualityStrategy();

    private EmiStackListEqualityStrategy() {}

    @Override
    public int hashCode(List<EmiStack> o) {
        return o.hashCode();
    }

    private boolean stacksIdentical(EmiStack a, EmiStack b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        // Check regular properties like item/components first
        if(!a.isEqual(b, Comparison.compareComponents())) {
            return false;
        }
        // We only need to be more strict when comparing two stacks that are not empty
        if(a.isEmpty()) {
            return true;
        }
        return stacksIdentical(a.getRemainder(), b.getRemainder()) &&
                a.getChance() == b.getChance() &&
                a.getAmount() == b.getAmount();
    }

    @Override
    public boolean equals(List<EmiStack> a, List<EmiStack> b) {
        if(a == b) {
            return true;
        }
        if(a == null || b == null) {
            return false;
        }
        if (a.size() != b.size()) {
            return false;
        }
        int size = a.size();
        for (int i = 0; i < size; i++) {
            if (!stacksIdentical(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }
}
