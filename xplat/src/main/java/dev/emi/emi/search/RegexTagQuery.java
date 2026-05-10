package dev.emi.emi.search;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiTagKey;

public class RegexTagQuery extends Query {
	private final Set<Object> valid;

	public RegexTagQuery(String name) {
		Pattern p = null;
		try {
			p = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
		} catch (Exception e) {
		}
		if (p == null) {
			valid = Set.of();
		} else {
			final Pattern pat = p;
			valid = Stream.<EmiTagKey<?>>concat(
				EmiTagKey.fromRegistry(EmiPort.getItemRegistry()),
				EmiTagKey.fromRegistry(EmiPort.getBlockRegistry())
			).filter(t -> {
				if (t.hasTranslation()) {
					if (pat.matcher(t.getTagName().getString().toLowerCase()).find()) {
						return true;
					}
				}
				if (pat.matcher(t.id().toString()).find()) {
					return true;
				}
				return false;
			}).flatMap(v -> v.stream()).collect(Collectors.toSet());
		}
	}

	@Override
	public boolean matches(EmiStack stack) {
		return valid.contains(stack.getKey());
	}
}
