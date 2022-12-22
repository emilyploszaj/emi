package dev.emi.emi.search;

import java.util.Set;

import com.google.common.collect.Sets;

import dev.emi.emi.api.stack.EmiStack;

public class ModQuery extends Query {
	private final Set<EmiStack> valid;

	public ModQuery(String name) {
		valid = Sets.newHashSet(EmiSearch.mods.findAll(name.toLowerCase()));
	}

	@Override
	public boolean matches(EmiStack stack) {
		return valid.contains(stack);
	}
}
