package dev.emi.emi.registry;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.runtime.EmiReloadLog;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;

public class EmiTags {
	private static final Map<TagKey<?>, Identifier> MODELED_TAGS = Maps.newHashMap();
	private static final Map<Identifier, List<TagKey<?>>> SORTED_TAGS = Maps.newHashMap();
	public static final List<Registry<?>> REGISTRIES = List.of(EmiPort.getItemRegistry(), EmiPort.getFluidRegistry());
	public static final List<TagKey<?>> TAGS = Lists.newArrayList();

	@SuppressWarnings("unchecked")
	public static <T> EmiIngredient getIngredient(Class<T> clazz, List<EmiStack> stacks, long amount) {
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
				map.put((T) stack.getKey(), stack);
			}
		}
		if (map.size() == 0) {
			return EmiStack.EMPTY;
		} else if (map.size() == 1) {
			return map.values().stream().toList().get(0);
		}
		List<TagKey<T>> keys = Lists.newArrayList();
		for (TagKey<T> key : getTags(registry)) {
			List<T> values = EmiUtil.values(key).map(i -> i.value()).toList();
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
			return new ListEmiIngredient(stacks.stream().toList(), amount);
		} else if (map.isEmpty()) {
			if (keys.size() == 1) {
				return new TagEmiIngredient(keys.get(0), amount);
			} else {
				return new ListEmiIngredient(keys.stream().map(k -> new TagEmiIngredient(k, 1)).toList(), amount);
			}
		} else {
			return new ListEmiIngredient(List.of(map.values().stream().map(i -> i.copy().setAmount(1)).toList(),
					keys.stream().map(k -> new TagEmiIngredient(k, 1)).toList())
				.stream().flatMap(a -> a.stream()).toList(), amount);
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
		for (Registry<?> registry : REGISTRIES) {
			reloadTags(registry);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <T> void reloadTags(Registry<T> registry) {
		List<TagKey<T>> tags = registry.streamTags()
			.filter(key -> !EmiClient.excludedTags.contains(key.id()))
			.toList();
		logUntranslatedTags(tags);
		tags = consolodateTags(tags);
		EmiTags.TAGS.addAll(tags.stream().sorted((a, b) -> a.toString().compareTo(b.toString())).toList());
		tags = tags.stream()
			.sorted((a, b) -> Long.compare(EmiUtil.values(b).count(), EmiUtil.values(a).count()))
			.toList();
		EmiTags.SORTED_TAGS.put(registry.getKey().getValue(), (List) tags);
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
