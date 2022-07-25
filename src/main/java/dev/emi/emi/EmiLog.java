package dev.emi.emi;

import java.io.PrintWriter;
import java.io.StringWriter;
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

	public static void error(Exception e) {
		e.printStackTrace();
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer, true));
		String[] strings = writer.getBuffer().toString().split("/");
		for (String s : strings) {
			EmiLog.warn(s);
		}
	}
}
