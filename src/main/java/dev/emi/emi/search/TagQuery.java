package dev.emi.emi.search;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TagQuery extends Query {
	private final Set<Object> valid;

	public TagQuery(String name) {
		String lowerName = name.toLowerCase();
		valid = Stream.concat(
			Registry.ITEM.streamTags().filter(t -> {
				Identifier id = t.id();
				String translation = EmiUtil.translateId("tag.", id);
				if (I18n.hasTranslation(translation)) {
					if (EmiPort.translatable(translation).getString().toLowerCase().contains(lowerName)) {
						return true;
					}
				}
				if (id.toString().contains(lowerName)) {
					return true;
				}
				return false;
			}).map(t -> Registry.ITEM.getEntryList(t)), Registry.BLOCK.streamTags().filter(t -> {
				if (t.id().toString().contains(lowerName)) {
					return true;
				}
				return false;
			}).map(t -> Registry.BLOCK.getEntryList(t))).flatMap(v -> v.get().stream().map(e -> e.value().asItem())).collect(Collectors.toSet());
	}

	@Override
	public boolean matches(EmiStack stack) {
		return valid.contains(stack.getKey());
	}
}
