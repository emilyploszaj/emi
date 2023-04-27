package dev.emi.emi.registry;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.runtime.EmiReloadLog;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EmiTags {
	private static final Set<Identifier> MODELED_TAGS = Sets.newHashSet();
	public static List<TagKey<Item>> itemTags = List.of();

	public static EmiIngredient getIngredient(List<ItemStack> stacks, long amount) {
		Map<Item, ItemStack> map = Maps.newHashMap();
		for (ItemStack stack : stacks) {
			if (!stack.isEmpty()) {
				map.put(stack.getItem(), stack);
			}
		}
		if (map.size() == 0) {
			return EmiStack.EMPTY;
		} else if (map.size() == 1) {
			return EmiStack.of(map.values().stream().toList().get(0), amount);
		}
		List<TagKey<Item>> keys = Lists.newArrayList();
		for (TagKey<Item> key : EmiTags.itemTags) {
			List<Item> values = EmiUtil.values(key).map(i -> i.value()).toList();
			if (values.size() < 2) {
				continue;
			}
			if (map.keySet().containsAll(values)) {
				map.keySet().removeAll(values);
				keys.add(key);
			}
			if (map.isEmpty()) {
				break;
			}
		}
		if (keys.isEmpty()) {
			return new ListEmiIngredient(stacks.stream().map(EmiStack::of).toList(), amount);
		} else if (map.isEmpty()) {
			if (keys.size() == 1) {
				return new TagEmiIngredient(keys.get(0), amount);
			} else {
				return new ListEmiIngredient(keys.stream().map(k -> new TagEmiIngredient(k, 1)).toList(), amount);
			}
		} else {
			return new ListEmiIngredient(List.of(map.values().stream().map(i -> EmiStack.of(i, 1)).toList(),
					keys.stream().map(k -> new TagEmiIngredient(k, 1)).toList())
				.stream().flatMap(a -> a.stream()).toList(), amount);
		}
	}

	public static Text getTagName(TagKey<Item> key) {
		String s = getTagTranslationKey(key);
		if (s == null) {
			return EmiPort.literal("#" + key.id());
		} else {
			return EmiPort.translatable(s);
		}
	}

	public static boolean hasTranslation(TagKey<Item> key) {
		return getTagTranslationKey(key) != null;
	}

	private static @Nullable String getTagTranslationKey(TagKey<Item> key) {
		String s = EmiUtil.translateId("tag.", key.id());
		if (I18n.hasTranslation(s)) {
			return s;
		}
		if (key.id().getNamespace().equals("forge")) {
			s = EmiUtil.translateId("tag.", new Identifier("c", key.id().getPath()));
			if (I18n.hasTranslation(s)) {
				return s;
			}
		}
		return null;
	}

	public static @Nullable ModelIdentifier getCustomModel(TagKey<Item> key) {
		Identifier rid = key.id();
		if (rid.getNamespace().equals("forge") && !EmiTags.MODELED_TAGS.contains(rid)) {
			rid = new Identifier("c", key.id().getPath());
		}
		if (EmiTags.MODELED_TAGS.contains(rid)) {
			return new ModelIdentifier("emi", "tags/" + rid.getNamespace() + "/" + rid.getPath(), "inventory");
		}
		return null;
	}

	public static boolean hasCustomModel(TagKey<Item> key) {
		return getCustomModel(key) != null;
	}

	public static void registerTagModels(ResourceManager manager, Consumer<Identifier> consumer) {
		EmiTags.MODELED_TAGS.clear();
		for (Identifier id : EmiPort.findResources(manager, "models/item/tags", s -> s.endsWith(".json"))) {
			String path = id.getPath();
			path = path.substring(0, path.length() - 5);
			String[] parts = path.substring(17).split("/");
			if (parts.length > 1) {
				EmiTags.MODELED_TAGS.add(new Identifier(parts[0], path.substring(18 + parts[0].length())));
				if (id.getNamespace().equals("emi")) {
					consumer.accept(new ModelIdentifier(id.getNamespace(), path.substring(12), "inventory"));
				}
			}
		}
	}
	
	public static void reload() {
		itemTags = EmiPort.getItemRegistry().streamTags()
			.filter(key -> !EmiClient.excludedTags.contains(key.id()))
			.toList();
		logUntranslatedTags();
		consolodateTags();
		itemTags = itemTags.stream()
			.sorted((a, b) -> Long.compare(EmiUtil.values(b).count(), EmiUtil.values(a).count()))
			.toList();
	}

	private static void logUntranslatedTags() {
		if (EmiConfig.logUntranslatedTags) {
			List<String> tags = Lists.newArrayList();
			for (TagKey<Item> tag : itemTags) {
				if (!hasTranslation(tag)) {
					tags.add(tag.id().toString());
				}
			}
			if (!tags.isEmpty()) {
				for (String tag : tags.stream().sorted().toList()) {
					EmiReloadLog.warn("Untranslated tag #" + tag);
				}
				EmiReloadLog.info(" Tag warning can be disabled in the config, EMI docs describe how to add a translation or exclude tags.");
			}
		}
	}

	private static void consolodateTags() {
		Map<Set<Item>, TagKey<Item>> map = Maps.newHashMap();
		for (int i = 0; i < itemTags.size(); i++) {
			TagKey<Item> key = itemTags.get(i);
			Set<Item> values = EmiUtil.values(key).map(RegistryEntry::value).collect(Collectors.toSet());
			TagKey<Item> original = map.get(values);
			if (original != null) {
				map.put(values, betterTag(key, original));
			} else {
				map.put(values, key);
			}
		}
		itemTags = map.values().stream().toList();
	}

	private static TagKey<Item> betterTag(TagKey<Item> a, TagKey<Item> b) {
		if (hasTranslation(a) != hasTranslation(b)) {
			return hasTranslation(a) ? a : b;
		}
		if (hasCustomModel(a) != hasCustomModel(b)) {
			return hasCustomModel(a) ? a : b;
		}
		String an = a.id().getNamespace();
		String bn = b.id().getNamespace();
		if (!an.equals(bn)) {
			if (an.equals("minecraft")) {
				return a;
			} else if (bn.equals("minecraft")) {
				return b;
			} else if (an.equals("c")) {
				return a;
			} else if (bn.equals("c")) {
				return b;
			} else if (an.equals("fabric")) {
				return EmiAgnos.isModLoaded("forge") ? b : a;
			} else if (bn.equals("fabric")) {
				return EmiAgnos.isModLoaded("forge") ? a : b;
			} else if (an.equals("forge")) {
				return EmiAgnos.isModLoaded("forge") ? a : b;
			} else if (bn.equals("forge")) {
				return EmiAgnos.isModLoaded("forge") ? b : a;
			}
		}
		return a.id().toString().length() <= b.id().toString().length() ? a : b;
	}
}
