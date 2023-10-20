package dev.emi.emi.registry;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.data.TagExclusions;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.runtime.EmiHidden;
import dev.emi.emi.runtime.EmiReloadLog;
import net.minecraft.block.Block;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EmiTags {
	public static final Identifier HIDDEN_FROM_RECIPE_VIEWERS = new Identifier("c", "hidden_from_recipe_viewers");
	private static final Map<TagKey<?>, Identifier> MODELED_TAGS = Maps.newHashMap();
	private static final Map<Set<?>, List<TagKey<?>>> CACHED_TAGS = Maps.newHashMap();
	private static final Map<TagKey<?>, List<?>> TAG_CONTENTS = Maps.newHashMap();
	private static final Map<TagKey<?>, List<?>> TAG_VALUES = Maps.newHashMap();
	private static final Map<Identifier, List<TagKey<?>>> SORTED_TAGS = Maps.newHashMap();
	public static final List<Registry<?>> REGISTRIES = List.of(EmiPort.getItemRegistry(), EmiPort.getFluidRegistry());
	public static final List<TagKey<?>> TAGS = Lists.newArrayList();
	public static TagExclusions exclusions = new TagExclusions();

	@SuppressWarnings("unchecked")
	public static <T> List<EmiStack> getValues(TagKey<T> key) {
		if (TAG_VALUES.containsKey(key)) {
			List<T> values = (List<T>) TAG_VALUES.getOrDefault(key, List.of());
			if (key.registry().equals(EmiPort.getItemRegistry().getKey())) {
				return values.stream().map(t -> EmiStack.of((Item) t)).toList();
			} else if (key.registry().equals(EmiPort.getFluidRegistry().getKey())) {
				return values.stream().map(t -> EmiStack.of((Fluid) t)).toList();
			}
		}
		return List.of();
	}

	public static <T> List<EmiStack> getRawValues(TagKey<T> key) {
		if (key.registry().equals(EmiPort.getItemRegistry().getKey())) {
			return EmiUtil.values(key).map(e -> EmiStack.of((Item) e.value())).toList();
		} else if (key.registry().equals(EmiPort.getFluidRegistry().getKey())) {
			return EmiUtil.values(key).map(e -> EmiStack.of((Fluid) e.value())).toList();
		} else if (key.registry().equals(EmiPort.getBlockRegistry().getKey())) {
			return EmiUtil.values(key).map(e -> EmiStack.of((Block) e.value())).toList();
		}
		return List.of();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> EmiIngredient getIngredient(Class<T> clazz, List<? extends EmiStack> stacks, long amount) {
		Registry<T> registry;
		if (clazz == Item.class) {
			registry = (Registry<T>) EmiPort.getItemRegistry();
		} else if (clazz == Fluid.class) {
			registry = (Registry<T>) EmiPort.getFluidRegistry();
		} else {
			return EmiStack.EMPTY;
		}
		Map<T, EmiStack> map = Maps.newHashMap();
		for (EmiStack stack : stacks) {
			if (!stack.isEmpty()) {
				EmiStack existing = map.getOrDefault(stack.getKey(), null);
				if (existing != null && !stack.equals(existing)) {
					return new ListEmiIngredient(stacks, amount);
				}
				map.put((T) stack.getKey(), stack);
			}
		}
		if (map.size() == 0) {
			return EmiStack.EMPTY;
		} else if (map.size() == 1) {
			return map.values().stream().toList().get(0).copy().setAmount(amount);
		}
		List<TagKey<T>> keys = (List<TagKey<T>>) (List) CACHED_TAGS.get(map.keySet());

		if (keys != null) {
			for (TagKey<T> key : keys) {
				List<T> values = (List<T>) TAG_CONTENTS.get(key);
				map.keySet().removeAll(values);
			}
		} else {
			keys = Lists.newArrayList();
			Set<T> original = new HashSet<>(map.keySet());
			for (TagKey<T> key : getTags(registry)) {
				List<T> values = (List<T>) TAG_CONTENTS.get(key);
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
			CACHED_TAGS.put((Set) original, (List) keys);
		}

		if (keys.isEmpty()) {
			return new ListEmiIngredient(stacks.stream().toList(), amount);
		} else if (map.isEmpty()) {
			if (keys.size() == 1) {
				return tagIngredient(keys.get(0), amount);
			} else {
				return new ListEmiIngredient(keys.stream().map(k -> tagIngredient(k, 1)).toList(), amount);
			}
		} else {
			return new ListEmiIngredient(Stream.of(map.values().stream().map(i -> i.copy().setAmount(1)).toList(),
					keys.stream().map(k -> tagIngredient(k, 1)).toList()).flatMap(Collection::stream).toList(), amount);
		}
	}

	private static EmiIngredient tagIngredient(TagKey<?> key, long amount) {
		List<?> list = TAG_VALUES.get(key);
		if (list == null || list.isEmpty()) {
			return EmiStack.EMPTY;
		} else if (list.size() == 1) {
			return new TagEmiIngredient(key, amount).getEmiStacks().get(0).copy().setAmount(amount);
		} else {
			return new TagEmiIngredient(key, amount);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> List<TagKey<T>> getTags(Registry<T> registry) {
		return (List<TagKey<T>>) (List) SORTED_TAGS.get(registry.getKey().getValue());
	}

	public static Text getTagName(TagKey<?> key) {
		String s = getTagTranslationKey(key);
		if (s == null) {
			return EmiPort.literal("#" + key.id());
		} else {
			return EmiPort.translatable(s);
		}
	}

	public static boolean hasTranslation(TagKey<?> key) {
		return getTagTranslationKey(key) != null;
	}

	private static @Nullable String getTagTranslationKey(TagKey<?> key) {
		Identifier registry = key.registry().getValue();
		if (registry.getNamespace().equals("minecraft")) {
			String s = translatePrefix("tag." + registry.getPath() + ".", key.id());
			if (s != null) {
				return s;
			}
		}
		return translatePrefix("tag.", key.id());
	}

	private static @Nullable String translatePrefix(String prefix, Identifier id) {
		String s = EmiUtil.translateId(prefix, id);
		if (I18n.hasTranslation(s)) {
			return s;
		}
		if (id.getNamespace().equals("forge")) {
			s = EmiUtil.translateId(prefix, new Identifier("c", id.getPath()));
			if (I18n.hasTranslation(s)) {
				return s;
			}
		}
		return null;
	}

	public static @Nullable Identifier getCustomModel(TagKey<?> key) {
		Identifier rid = key.id();
		if (rid.getNamespace().equals("forge") && !EmiTags.MODELED_TAGS.containsKey(key)) {
			key = TagKey.of(key.registry(), new Identifier("c", rid.getPath()));
		}
		return EmiTags.MODELED_TAGS.get(key);
	}

	public static boolean hasCustomModel(TagKey<?> key) {
		return getCustomModel(key) != null;
	}

	public static void registerTagModels(ResourceManager manager, Consumer<Identifier> consumer) {
		EmiTags.MODELED_TAGS.clear();
		for (Identifier id : EmiPort.findResources(manager, "models/tag", s -> s.endsWith(".json"))) {
			String path = id.getPath();
			path = path.substring(11, path.length() - 5);
			String[] parts = path.split("/");
			if (parts.length > 1) {
				TagKey<?> key = TagKey.of(RegistryKey.ofRegistry(new Identifier("minecraft", parts[0])), new Identifier(id.getNamespace(), path.substring(1 + parts[0].length())));
				Identifier mid = new Identifier(id.getNamespace(), "tag/" + path);
				EmiTags.MODELED_TAGS.put(key, mid);
				consumer.accept(mid);
			}
		}
		for (Identifier id : EmiPort.findResources(manager, "models/item/tags", s -> s.endsWith(".json"))) {
			String path = id.getPath();
			path = path.substring(0, path.length() - 5);
			String[] parts = path.substring(17).split("/");
			if (id.getNamespace().equals("emi") && parts.length > 1) {
				Identifier mid = new ModelIdentifier(id.getNamespace(), path.substring(12), "inventory");
				EmiTags.MODELED_TAGS.put(TagKey.of(EmiPort.getItemRegistry().getKey(), new Identifier(parts[0], path.substring(18 + parts[0].length()))), mid);
				consumer.accept(mid);
			}
		}
	}
	
	public static void reload() {
		TAGS.clear();
		SORTED_TAGS.clear();
		TAG_CONTENTS.clear();
		TAG_VALUES.clear();
		CACHED_TAGS.clear();
		for (Registry<?> registry : REGISTRIES) {
			reloadTags(registry);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <T> void reloadTags(Registry<T> registry) {
		Set<T> hidden = EmiUtil.values(TagKey.of(registry.getKey(), HIDDEN_FROM_RECIPE_VIEWERS)).map(RegistryEntry::value).collect(Collectors.toSet());
		Identifier rid = registry.getKey().getValue();
		List<TagKey<T>> tags = registry.streamTags()
			.filter(key -> !exclusions.contains(rid, key.id()) && !hidden.containsAll(EmiUtil.values(key).map(RegistryEntry::value).toList()))
			.toList();
		logUntranslatedTags(tags);
		tags = consolodateTags(tags);
		for (TagKey<T> key : tags) {
			List<T> contents = EmiUtil.values(key).map(i -> i.value()).toList();
			TAG_CONTENTS.put(key, contents);
			List<T> values = contents.stream().filter(s -> !EmiHidden.isDisabled(stackFromKey(key, s))).toList();
			if (values.isEmpty()) {
				TAG_VALUES.put(key, contents);
			} else {
				TAG_VALUES.put(key, values);
			}
		}
		EmiTags.TAGS.addAll(tags.stream().sorted((a, b) -> a.toString().compareTo(b.toString())).toList());
		tags = tags.stream()
			.sorted((a, b) -> Long.compare(EmiUtil.values(b).count(), EmiUtil.values(a).count()))
			.toList();
		EmiTags.SORTED_TAGS.put(registry.getKey().getValue(), (List) tags);
	}

	private static <T> EmiStack stackFromKey(TagKey<T> key, T t) {
		if (key.registry().equals(EmiPort.getItemRegistry().getKey())) {
			return EmiStack.of((Item) t);
		} else if (key.registry().equals(EmiPort.getFluidRegistry().getKey())) {
			return EmiStack.of((Fluid) t);
		}
		throw new UnsupportedOperationException("Unsupported tag registry " + key);
	}

	private static <T> void logUntranslatedTags(List<TagKey<T>> tags) {
		if (EmiConfig.logUntranslatedTags) {
			List<String> untranslated = Lists.newArrayList();
			for (TagKey<T> tag : tags) {
				if (!hasTranslation(tag)) {
					untranslated.add(tag.id().toString());
				}
			}
			if (!untranslated.isEmpty()) {
				for (String tag : untranslated.stream().sorted().toList()) {
					EmiReloadLog.warn("Untranslated tag #" + tag);
				}
				EmiReloadLog.info(" Tag warning can be disabled in the config, EMI docs describe how to add a translation or exclude tags.");
			}
		}
	}

	private static <T> List<TagKey<T>> consolodateTags(List<TagKey<T>> tags) {
		Map<Set<T>, TagKey<T>> map = Maps.newHashMap();
		for (int i = 0; i < tags.size(); i++) {
			TagKey<T> key = tags.get(i);
			Set<T> values = EmiUtil.values(key).map(RegistryEntry::value).collect(Collectors.toSet());
			TagKey<T> original = map.get(values);
			if (original != null) {
				map.put(values, betterTag(key, original));
			} else {
				map.put(values, key);
			}
		}
		return map.values().stream().toList();
	}

	private static<T> TagKey<T> betterTag(TagKey<T> a, TagKey<T> b) {
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
