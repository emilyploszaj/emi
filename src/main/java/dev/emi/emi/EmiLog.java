package dev.emi.emi;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

public class EmiLog {
	private static List<String> PENDING_WARNINGS = Lists.newArrayList();
	public static List<String> WARNINGS = List.of();

	public static void bake() {
		WARNINGS = PENDING_WARNINGS;
		PENDING_WARNINGS = Lists.newArrayList();
	}

	public static void warn(String warning) {
		PENDING_WARNINGS.add(warning);
		System.err.println("[emi] " + warning);
	}
}
