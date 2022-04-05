package dev.emi.emi.search;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;

public class ModQuery extends Query {
	private final String name;

	public ModQuery(String name) {
		this.name = name.toLowerCase();
	}

	@Override
	public boolean matches(EmiStack stack) {
		String namespace = stack.getId().getNamespace();
		return EmiUtil.getModName(namespace).toLowerCase().contains(name);
	}
}
