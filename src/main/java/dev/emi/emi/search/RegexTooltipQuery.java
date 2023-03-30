package dev.emi.emi.search;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.text.Text;

public class RegexTooltipQuery extends Query {
	private final Pattern pattern;

	public RegexTooltipQuery(String name) {
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
		List<Text> lines = stack.getTooltipText();
		for (int i = 1; i < lines.size(); i++) {
			Matcher m = pattern.matcher(lines.get(i).getString());
			if (m.find()) {
				return true;
			}
		}
		return false;
	}
}
