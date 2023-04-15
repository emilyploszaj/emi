package dev.emi.emi.search;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiTags;
import dev.emi.emi.api.stack.EmiStack;

public class TagQuery extends Query {
	private final Set<Object> valid;

	public TagQuery(String name) {
		String lowerName = name.toLowerCase();
		valid = Stream.concat(
			EmiPort.getItemRegistry().streamTags().filter(t -> {
				if (EmiTags.hasTranslation(t)) {
					if (EmiTags.getTagName(t).getString().toLowerCase().contains(lowerName)) {
						return true;
					}
				}
				if (t.id().toString().contains(lowerName)) {
					return true;
				}
				return false;
			}).map(t -> EmiPort.getItemRegistry().getEntryList(t)), EmiPort.getBlockRegistry().streamTags().filter(t -> {
				if (t.id().toString().contains(lowerName)) {
					return true;
				}
				return false;
			}).map(t -> EmiPort.getBlockRegistry().getEntryList(t))).flatMap(v -> v.get().stream().map(e -> e.value().asItem())).collect(Collectors.toSet());
	}

	@Override
	public boolean matches(EmiStack stack) {
		return valid.contains(stack.getKey());
	}
}
