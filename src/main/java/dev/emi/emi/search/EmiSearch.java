package dev.emi.emi.search;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.EmiLog;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiReloadLog;
import dev.emi.emi.EmiStackList;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.data.EmiAlias;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.search.SuffixArray;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EmiSearch {
	public static final Pattern TOKENS = Pattern.compile("(-?[@#]?\\/(\\\\.|[^\\\\\\/])+\\/|[^\\s]+)");
	private static volatile String query = "";
	public static Thread thread;
	public static volatile List<? extends EmiIngredient> stacks = EmiStackList.stacks;
	public static volatile EmiPlayerInventory inv;
	public static volatile CompiledQuery compiledQuery;
	public static SuffixArray<EmiStack> names, tooltips, mods, aliases;

	public static void bake() {
		SuffixArray<EmiStack> names = new SuffixArray<EmiStack>();
		SuffixArray<EmiStack> tooltips = new SuffixArray<EmiStack>();
		SuffixArray<EmiStack> mods = new SuffixArray<EmiStack>();
		SuffixArray<EmiStack> aliases = new SuffixArray<EmiStack>();
		boolean old = EmiConfig.appendItemModId;
		EmiConfig.appendItemModId = false;
		for (EmiStack stack : EmiStackList.stacks) {
			Text name = stack.getName();
			if (name != null) {
				names.add(stack, name.getString().toLowerCase());
			}
			List<Text> tooltip = stack.getTooltipText();
			if (tooltip != null) {
				for (int i = 1; i < tooltip.size(); i++) {
					Text text = tooltip.get(i);
					if (text != null) {
						tooltips.add(stack, text.getString().toLowerCase());
					}
				}
			}
			Identifier id = stack.getId();
			if (id != null) {
				mods.add(stack, EmiUtil.getModName(id.getNamespace()).toLowerCase());
			}
			if (stack.getItemStack().getItem() == Items.ENCHANTED_BOOK) {
				for (Enchantment e : EnchantmentHelper.get(stack.getItemStack()).keySet()) {
					Identifier eid = EmiPort.getEnchantmentRegistry().getId(e);
					if (eid != null && !eid.getNamespace().equals("minecraft")) {
						mods.add(stack, EmiUtil.getModName(eid.getNamespace()).toLowerCase());
					}
				}
			}
		}
		for (EmiAlias alias : EmiData.aliases) {
			for (String key : alias.keys()) {
				if (!I18n.hasTranslation(key)) {
					EmiReloadLog.warn("Untranslated alias " + key);
				}
				String text = I18n.translate(key).toLowerCase();
				for (EmiIngredient ing : alias.stacks()) {
					for (EmiStack stack : ing.getEmiStacks()) {
						aliases.add(stack, text);
					}
				}
			}
		}
		EmiConfig.appendItemModId = old;
		names.build();
		tooltips.build();
		mods.build();
		aliases.build();
		EmiSearch.names = names;
		EmiSearch.tooltips = tooltips;
		EmiSearch.mods = mods;
		EmiSearch.aliases = aliases;
	}

	public static void update() {
		search(query);
	}

	public static void search(String query) {
		synchronized (EmiSearch.class) {
			inv = EmiScreenManager.lastPlayerInventory;
			EmiSearch.query = query;
			if (thread == null || !thread.isAlive()) {
				thread = new Thread(new SearchWorker());
				thread.setDaemon(true);
				thread.start();
			}
		}
	}

	public static void apply(List<? extends EmiIngredient> stacks) {
		synchronized (EmiSearch.class) {
			EmiSearch.stacks = stacks;
			thread = null;
		}
	}

	public static class CompiledQuery {
		public final Query fullQuery;

		public CompiledQuery(String query) {
			List<Query> full = Lists.newArrayList();
			List<Query> queries = Lists.newArrayList();
			Matcher matcher = TOKENS.matcher(query);
			while (matcher.find()) {
				String q = matcher.group();
				if (q.equals("|")) {
					if (!queries.isEmpty()) {
						full.add(new LogicalAndQuery(queries));
						queries = Lists.newArrayList();
					}
					continue;
				}
				boolean negated = q.startsWith("-");
				if (negated) {
					q = q.substring(1);
				}
				if (q.isEmpty()) {
					continue;
				}
				QueryType type = QueryType.fromString(q);
				Function<String, Query> constructor = type.queryConstructor;
				Function<String, Query> regexConstructor = type.regexQueryConstructor;
				if (type == QueryType.DEFAULT) {
					List<Function<String, Query>> constructors = Lists.newArrayList();
					List<Function<String, Query>> regexConstructors = Lists.newArrayList();
					constructors.add(constructor);
					regexConstructors.add(regexConstructor);

					if (EmiConfig.searchTooltipByDefault) {
						constructors.add(QueryType.TOOLTIP.queryConstructor);
						regexConstructors.add(QueryType.TOOLTIP.regexQueryConstructor);
					}
					if (EmiConfig.searchModNameByDefault) {
						constructors.add(QueryType.MOD.queryConstructor);
						regexConstructors.add(QueryType.MOD.regexQueryConstructor);
					}
					if (EmiConfig.searchTagsByDefault) {
						constructors.add(QueryType.TAG.queryConstructor);
						regexConstructors.add(QueryType.TAG.regexQueryConstructor);
					}
					// TODO add config
					constructors.add(AliasQuery::new);
					if (constructors.size() > 1) {
						constructor = name -> new LogicalOrQuery(constructors.stream().map(c -> c.apply(name)).toList());
						regexConstructor = name -> new LogicalOrQuery(regexConstructors.stream().map(c -> c.apply(name)).toList());
					}
				}
				addQuery(q.substring(type.prefix.length()), negated, queries, constructor, regexConstructor);
			}
			if (!queries.isEmpty()) {
				full.add(new LogicalAndQuery(queries));
			}
			if (!full.isEmpty()) {
				fullQuery = new LogicalOrQuery(full);
			} else {
				fullQuery = null;
			}
		}

		public boolean isEmpty() {
			return fullQuery == null;
		}

		public boolean test(EmiStack stack) {
			if (fullQuery == null) {
				return true;
			} else {
				return fullQuery.matches(stack);
			}
		}

		private static void addQuery(String s, boolean negated, List<Query> queries, Function<String, Query> normal, Function<String, Query> regex) {
			Query q;
			if (s.length() > 1 && s.startsWith("/") && s.endsWith("/")) {
				q = regex.apply(s.substring(1, s.length() - 1));
			} else {
				q = normal.apply(s);
			}
			q.negated = negated;
			queries.add(q);
		}
	}

	private static class SearchWorker implements Runnable {

		@Override
		public void run() {
			try {
				List<? extends EmiIngredient> stacks;
				String query;
				do {
					query = EmiSearch.query;
					CompiledQuery compiled = new CompiledQuery(query);
					compiledQuery = compiled;
					List<? extends EmiIngredient> source = EmiScreenManager.getSearchSource();
					if (compiled.isEmpty()) {
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
						return compiled.test(es);
					}).toList();
				} while (query != EmiSearch.query);
				apply(stacks);
			} catch (Exception e) {
				EmiLog.error("Error when attempting to search:");
				e.printStackTrace();
			}
		}
	}
}
