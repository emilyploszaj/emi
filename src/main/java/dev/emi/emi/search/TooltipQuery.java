package dev.emi.emi.search;

import java.util.List;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.text.Text;

public class TooltipQuery extends Query {
	private final String name;

	public TooltipQuery(String name) {
		this.name = name.toLowerCase();
	}

	@Override
	public boolean matches(EmiStack stack) {
		List<Text> lines = stack.getTooltipText();
		lines.remove(0);
		for (Text text : lines) {
			if (text.getString().toLowerCase().contains(name)) {
				return true;
			}
		}
		return false;
	}
}
