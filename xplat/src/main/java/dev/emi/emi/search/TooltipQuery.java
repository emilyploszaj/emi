package dev.emi.emi.search;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.text.Text;

public class TooltipQuery extends Query {
	private final Set<EmiStack> valid;
	private final String name;

	public TooltipQuery(String name) {
		valid = Sets.newHashSet(EmiSearch.tooltips.findAll(name.toLowerCase()));
		this.name = name.toLowerCase();
	}

	@Override
	public boolean matches(EmiStack stack) {
		return valid.contains(stack);
	}

	@Override
	public boolean matchesUnbaked(EmiStack stack) {
		for (Text text : getText(stack)) {
			if (text.getString().toLowerCase().contains(name)) {
				return true;
			}
		}
		return false;
	}

	public static List<Text> getText(EmiStack stack) {
		List<Text> lines = stack.getTooltipText();
		if (lines.isEmpty()) {
			return lines;
		} else {
			return lines.subList(1, lines.size());
		}
	}
}
