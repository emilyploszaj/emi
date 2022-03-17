package dev.emi.emi.search;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.util.registry.Registry;

public class RegexModQuery extends Query {
	private final Pattern pattern;

	public RegexModQuery(String name) {
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
		String namespace = Registry.ITEM.getId(stack.getItemStack().getItem()).getNamespace();
		String mod = EmiUtil.getModName(namespace);
		Matcher m = pattern.matcher(mod);
		return m.find();
	}
}
