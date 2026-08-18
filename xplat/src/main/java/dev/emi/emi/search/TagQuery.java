package dev.emi.emi.search;

import net.minecraft.item.BlockItem;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiTagKey;

public class TagQuery extends Query {
	private final Set<Object> valid;

	public TagQuery(String name) {
		String lowerName = name.toLowerCase();
		valid = Stream.<EmiTagKey<?>>concat(
			EmiTags.TAGS.stream(),
			EmiTagKey.fromRegistry(EmiPort.getBlockRegistry())
		).filter(t -> {
			if (t.hasTranslation()) {
				if (t.getTagName().getString().toLowerCase().contains(lowerName)) {
					return true;
				}
			}
			if (t.id().toString().contains(lowerName)) {
				return true;
			}
			return false;
		}).flatMap(v -> v.stream()).collect(Collectors.toSet());
	}

	@Override
	public boolean matches(EmiStack stack) {
		if (stack.getKey() instanceof BlockItem bi && valid.contains(bi.getBlock())) {
			return true;
		}
		return valid.contains(stack.getKey());
	}
}
