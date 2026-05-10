package dev.emi.emi.runtime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.registry.EmiTags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList.Named;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

// Wrapper around TagKeys
public class EmiTagKey<T> {
	public static final Map<TagKey<?>, EmiTagKey<?>> CACHE = Maps.newHashMap();
	private final TagKey<T> raw;
	private List<T> cached;
	
	private EmiTagKey(TagKey<T> raw) {
		this.raw = raw;
		recalculate();
	}

	public void recalculate() {
		cached = stream().toList();
	}

	public TagKey<T> raw() {
		return raw;
	}

	public boolean isOf(Registry<?> registry) {
		return raw.isOf(registry.getKey());
	}

	public Identifier id() {
		return raw.id();
	}

	public Registry<T> registry() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client.world.getRegistryManager().getOptional(raw.registry()).orElse(null);
	}

	public Stream<T> stream() {
		Registry<T> registry = registry();
		Optional<Named<T>> opt = registry.getEntryList(raw);
		if (opt.isEmpty()) {
			return Stream.of();
		} else {
			if (registry == EmiPort.getFluidRegistry()) {
				return opt.get().stream().filter(o -> {
					Fluid f = (Fluid) o.value();
					return f.isStill(f.getDefaultState());
				}).map(RegistryEntry::value);
			}
			return opt.get().stream().map(RegistryEntry::value);
		}
	}

	public List<T> getList() {
		return cached;
	}

	public Set<T> getSet() {
		return stream().collect(Collectors.toSet());
	}

	public Text getTagName() {
		String s = getTagTranslationKey();
		if (s == null) {
			return EmiPort.literal("#" + this.id());
		} else {
			return EmiPort.translatable(s);
		}
	}

	public boolean hasTranslation() {
		return getTagTranslationKey() != null;
	}

	private @Nullable String getTagTranslationKey() {
		Identifier registry = raw.registry().getValue();
		if (registry.getNamespace().equals("minecraft")) {
			String s = translatePrefix("tag." + registry.getPath().replace("/", ".") + ".", this.id());
			if (s != null) {
				return s;
			}
		} else {
			String s = translatePrefix("tag." + registry.getNamespace() + "." + registry.getPath().replace("/", ".") + ".", this.id());
			if (s != null) {
				return s;
			}
		}
		return translatePrefix("tag.", this.id());
	}

	private static @Nullable String translatePrefix(String prefix, Identifier id) {
		String s = EmiUtil.translateId(prefix, id);
		if (I18n.hasTranslation(s)) {
			return s;
		}
		if (id.getNamespace().equals("forge")) {
			s = EmiUtil.translateId(prefix, EmiPort.id("c", id.getPath()));
			if (I18n.hasTranslation(s)) {
				return s;
			}
		}
		return null;
	}

	public @Nullable Identifier getCustomModel() {
		Identifier rid = this.id();
		if (rid.getNamespace().equals("forge") && !EmiTags.MODELED_TAGS.containsKey(raw())) {
			return EmiTagKey.of(registry(), EmiPort.id("c", rid.getPath())).getCustomModel();
		}
		return EmiTags.MODELED_TAGS.get(raw());
	}

	public boolean hasCustomModel() {
		return getCustomModel() != null;
	}

	@Override
	public int hashCode() {
		return raw().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof EmiTagKey other && raw().equals(other.raw());
	}

	@SuppressWarnings("unchecked")
	public static <T> EmiTagKey<T> of(TagKey<T> raw) {
		return (EmiTagKey<T>) CACHE.computeIfAbsent(raw, k -> new EmiTagKey<>(k));
	}

	public static <T> EmiTagKey<T> of(Registry<T> registry, Identifier id) {
		return of(TagKey.of(registry.getKey(), id));
	}

	public static <T> Stream<EmiTagKey<T>> fromRegistry(Registry<T> registry) {
		return registry.streamTags().map(EmiTagKey::of);
	}

	public static void reload() {
		for (EmiTagKey<?> key : CACHE.values()) {
			key.recalculate();
		}
	}
}
