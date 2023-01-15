package dev.emi.emi.search;

import java.util.Set;

import com.google.common.collect.Sets;

import dev.emi.emi.api.stack.EmiStack;

public class AliasQuery extends Query {
	private final Set<EmiStack> valid;

	public AliasQuery(String name) {
		valid = Sets.newHashSet(EmiSearch.aliases.findAll(name.toLowerCase()));
	}

	@Override
	public boolean matches(EmiStack stack) {
		return valid.contains(stack);
	}
}
