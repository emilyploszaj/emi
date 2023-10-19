package dev.emi.emi.search;

import java.util.Set;

import com.google.common.collect.Sets;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;

public class ModQuery extends Query {
	private final Set<EmiStack> valid = Sets.newIdentityHashSet();
	private final String name;

	public ModQuery(String name) {
		this.name = name.toLowerCase();
		EmiSearch.mods.findAll(this.name).forEach(s -> valid.add(s.stack));
	}

	@Override
	public boolean matches(EmiStack stack) {
		return valid.contains(stack);
	}

	@Override
	public boolean matchesUnbaked(EmiStack stack) {
		return EmiUtil.getModName(stack.getId().getNamespace()).toLowerCase().contains(name);
	}
}
