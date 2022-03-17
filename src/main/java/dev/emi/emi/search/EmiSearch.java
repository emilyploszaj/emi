package dev.emi.emi.search;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiStackList;
import dev.emi.emi.api.stack.EmiStack;

public class EmiSearch {
	public static final Pattern TOKENS = Pattern.compile("([@#]?\\/(\\\\.|[^\\\\\\/])+\\/|[^\\s]+)");
	private static volatile String query;
	private static Thread thread;
	public static volatile List<EmiStack> stacks = EmiStackList.stacks;

	public static synchronized void search(String query) {
		EmiSearch.query = query;
		if (thread == null || !thread.isAlive()) {
			thread = new Thread(new SearchWorker());
			thread.setDaemon(true);
			thread.start();
		}
	}

	public static synchronized void apply(List<EmiStack> stacks) {
		EmiSearch.stacks = stacks;
		thread = null;
	}

	private static class SearchWorker implements Runnable {

		@Override
		public void run() {
			List<EmiStack> stacks;
			String query;
			do {
				query = EmiSearch.query;
				List<Query> queries = Lists.newArrayList();
				List<Query> regexQueries = Lists.newArrayList();
				Matcher matcher = TOKENS.matcher(query);
				while (matcher.find()) {
					String q = matcher.group();
					QueryType type = QueryType.fromString(q);
					addQuery(q.substring(type.prefix.length()), queries, regexQueries, type.queryConstructor, type.regexQueryConstructor);
				}
				queries.addAll(regexQueries);
				if (queries.isEmpty()) {
					stacks = EmiStackList.stacks;
					continue;
				}
				stacks = EmiStackList.stacks.stream().filter(stack -> {
					for (Query q : queries) {
						if (!q.matches(stack)) {
							return false;
						}
					}
					return true;
				}).toList();
			} while (query != EmiSearch.query);
			apply(stacks);
		}

		private static void addQuery(String s, List<Query> queries, List<Query> regexQueries,
				Function<String, Query> normal, Function<String, Query> regex) {
			if (s.length() > 1 && s.startsWith("/") && s.endsWith("/")) {
				regexQueries.add(regex.apply(s.substring(1, s.length() - 1)));
			} else {
				queries.add(normal.apply(s));
			}
		}
	}
}
