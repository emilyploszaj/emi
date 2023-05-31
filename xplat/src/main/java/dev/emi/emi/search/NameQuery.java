package dev.emi.emi.search;

import java.util.Set;

import com.google.common.collect.Sets;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.text.Text;

public class NameQuery extends Query {
	private final Set<EmiStack> valid;
	private final String name;
	
	public NameQuery(String name) {
		valid = Sets.newHashSet(EmiSearch.names.findAll(name.toLowerCase()));
		this.name = name;
	}

	@Override
	public boolean matches(EmiStack stack) {
		return valid.contains(stack);
	}

	@Override
	public boolean matchesUnbaked(EmiStack stack) {
		return getText(stack).getString().toLowerCase().contains(name);
	}

	public static Text getText(EmiStack stack) {
		return stack.getName();
	}
}
