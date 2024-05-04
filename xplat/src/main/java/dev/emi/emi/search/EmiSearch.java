package dev.emi.emi.search;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.data.EmiAlias;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.registry.EmiStackList;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadLog;
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
	private static Thread searchThread;
	public static final Executor executor = Executors.newSingleThreadExecutor(task -> {
		Thread t = new Thread(task, "EMI Search Thread");
		t.setDaemon(true);
		searchThread = t;
		return t;
	});
	public static Set<EmiStack> bakedStacks;
	public static SuffixArray<SearchStack> names, tooltips, mods;
	public static SuffixArray<EmiStack> aliases;

	public static boolean isSearchThread() {
		return searchThread == Thread.currentThread();
	}

	public static void bake() {
		SuffixArray<SearchStack> names = new SuffixArray<>();
		SuffixArray<SearchStack> tooltips = new SuffixArray<>();
		SuffixArray<SearchStack> mods = new SuffixArray<>();
		SuffixArray<EmiStack> aliases = new SuffixArray<>();
		Set<EmiStack> bakedStacks = Sets.newIdentityHashSet();
		boolean old = EmiConfig.appendItemModId;
		EmiConfig.appendItemModId = false;
		for (EmiStack stack : EmiStackList.stacks) {
			try {
				SearchStack searchStack = new SearchStack(stack);
				bakedStacks.add(stack);
				Text name = NameQuery.getText(stack);
				if (name != null) {
					names.add(searchStack, name.getString().toLowerCase());
				}
				List<Text> tooltip = stack.getTooltipText();
				if (tooltip != null) {
					for (int i = 1; i < tooltip.size(); i++) {
						Text text = tooltip.get(i);
						if (text != null) {
							tooltips.add(searchStack, text.getString().toLowerCase());
						}
					}
				}
				Identifier id = stack.getId();
				if (id != null) {
					mods.add(searchStack, EmiUtil.getModName(id.getNamespace()).toLowerCase());
					mods.add(searchStack, id.getNamespace().toLowerCase());
					names.add(searchStack, id.getPath().toLowerCase());
				}
				if (stack.getItemStack().getItem() == Items.ENCHANTED_BOOK) {
					for (Enchantment e : EnchantmentHelper.get(stack.getItemStack()).keySet()) {
						Identifier eid = EmiPort.getEnchantmentRegistry().getId(e);
						if (eid != null && !eid.getNamespace().equals("minecraft")) {
							mods.add(searchStack, EmiUtil.getModName(eid.getNamespace()).toLowerCase());
						}
					}
				}
			} catch (Exception e) {
				EmiLog.error("EMI caught an exception while baking search for " + stack);
				e.printStackTrace();
			}
		}
		for (Supplier<EmiAlias> supplier : EmiData.aliases) {
			EmiAlias alias = supplier.get();
			for (String key : alias.keys()) {
				if (!I18n.hasTranslation(key)) {
					EmiReloadLog.warn("Untranslated alias " + key);
				}
				String text = I18n.translate(key).toLowerCase();
				for (EmiIngredient ing : alias.stacks()) {
					for (EmiStack stack : ing.getEmiStacks()) {
						aliases.add(stack.copy().comparison(EmiPort.compareStrict()), text);
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
		EmiSearch.bakedStacks = bakedStacks;
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
			} else if (EmiSearch.bakedStacks.contains(stack)) {
				return fullQuery.matches(stack);
			} else {
				return fullQuery.matchesUnbaked(stack);
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
}
