package dev.emi.emi.search;

import java.util.Set;
import net.minecraft.network.chat.Component;
import com.google.common.collect.Sets;

import dev.emi.emi.api.stack.EmiStack;

public class NameQuery extends Query {
	private final Set<EmiStack> valid = Sets.newIdentityHashSet();
	private final String name;
	
	public NameQuery(String name) {
		EmiSearch.names.search(name.toLowerCase()).forEach(s -> valid.add(s.stack));
		this.name = name.toLowerCase();
	}

	@Override
	public boolean matches(EmiStack stack) {
		return valid.contains(stack);
	}

	@Override
	public boolean matchesUnbaked(EmiStack stack) {
		return getText(stack).getString().toLowerCase().contains(name);
	}

	public static Component getText(EmiStack stack) {
		return stack.getName();
	}
}
