package dev.emi.emi.search;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiConfig;
import dev.emi.emi.EmiStackList;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.MinecraftClient;

public class EmiSearch {
	public static final Pattern TOKENS = Pattern.compile("(-?[@#]?\\/(\\\\.|[^\\\\\\/])+\\/|[^\\s]+)");
	private static volatile String query;
	public static Thread thread;
	public static volatile List<? extends EmiIngredient> stacks = EmiStackList.stacks;
	public static volatile EmiPlayerInventory inv;

	public static void update() {
		search(query);
	}

	public static synchronized void search(String query) {
		inv = EmiScreenManager.lastPlayerInventory;
		EmiSearch.query = query;
		if (thread == null || !thread.isAlive()) {
			thread = new Thread(new SearchWorker());
			thread.setDaemon(true);
			thread.start();
		}
	}

	public static synchronized void apply(List<? extends EmiIngredient> stacks) {
		EmiSearch.stacks = stacks;
		thread = null;
	}

	private static class SearchWorker implements Runnable {

		@Override
		public void run() {
			try {
				List<? extends EmiIngredient> stacks;
				String query;
				do {
					query = EmiSearch.query;
					List<Query> queries = Lists.newArrayList();
					List<Query> negatedQuerries = Lists.newArrayList();
					Matcher matcher = TOKENS.matcher(query);
					while (matcher.find()) {
						String q = matcher.group();
						boolean negated = q.startsWith("-");
						if (negated) {
							q = q.substring(1);
						}
						QueryType type = QueryType.fromString(q);
						addQuery(q.substring(type.prefix.length()), negated ? negatedQuerries : queries,
							type.queryConstructor, type.regexQueryConstructor);
					}
					List<? extends EmiIngredient> source;
					if (EmiConfig.craftable) {
						if (inv == null) {
							MinecraftClient client = MinecraftClient.getInstance();
							inv = new EmiPlayerInventory(client.player);
						}
						source = inv.getCraftables();
					} else {
						source = EmiStackList.stacks;
					}
					if (queries.isEmpty() && negatedQuerries.isEmpty()) {
						stacks = source;
						continue;
					}
					stacks = source.stream().filter(stack -> {
						List<EmiStack> ess = stack.getEmiStacks();
						// TODO properly support ingredients?
						if (ess.size() != 1) {
							return false;
						}
						EmiStack es = ess.get(0);
						for (int i = 0; i < queries.size(); i++) {
							Query q = queries.get(i);
							if (!q.matches(es)) {
								return false;
							}
						}
						for (int i = 0; i < negatedQuerries.size(); i++) {
							Query q = negatedQuerries.get(i);
							if (q.matches(es)) {
								return false;
							}
						}
						return true;
					}).toList();
				} while (query != EmiSearch.query);
				apply(stacks);
			} catch (Exception e) {
				System.err.println("[emi] Error when attempting to search:");
				e.printStackTrace();
			}
		}

		private static void addQuery(String s, List<Query> queries, Function<String, Query> normal, Function<String, Query> regex) {
			if (s.length() > 1 && s.startsWith("/") && s.endsWith("/")) {
				queries.add(regex.apply(s.substring(1, s.length() - 1)));
			} else {
				queries.add(normal.apply(s));
			}
		}
	}
}
