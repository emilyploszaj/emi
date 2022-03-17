package dev.emi.emi.search;

import dev.emi.emi.api.stack.EmiStack;

public class NameQuery extends Query {
	private final String name;

	public NameQuery(String name) {
		this.name = name.toLowerCase();
	}

	@Override
	public boolean matches(EmiStack stack) {
		return stack.getName().getString().toLowerCase().contains(name);
	}
}
