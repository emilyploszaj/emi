package dev.emi.emi.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.emi.emi.api.stack.EmiStack;

public class RegexNameQuery extends Query {
	private final Pattern pattern;

	public RegexNameQuery(String name) {
		Pattern p = null;
		try {
			p = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
		}
		pattern = p;
	}

	@Override
	public boolean matches(EmiStack stack) {
		if (pattern == null) {
			return false;
		}
		Matcher m = pattern.matcher(NameQuery.getText(stack).getString());
		return m.find();
	}
}
