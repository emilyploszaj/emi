package dev.emi.emi.search;

import java.util.Set;

import com.google.common.collect.Sets;

import dev.emi.emi.api.stack.EmiStack;

public class ModQuery extends Query {
	private final Set<EmiStack> valid = Sets.newIdentityHashSet();

	public ModQuery(String name) {
		EmiSearch.mods.findAll(name.toLowerCase()).forEach(s -> valid.add(s.stack));
	}

	@Override
	public boolean matches(EmiStack stack) {
		return valid.contains(stack);
	}
}
