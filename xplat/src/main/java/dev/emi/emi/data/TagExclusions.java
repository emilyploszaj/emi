package dev.emi.emi.data;

import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class TagExclusions {
	public final Set<ResourceLocation> globalExclusions = Sets.newHashSet();
	public final Map<ResourceLocation, Set<ResourceLocation>> exclusions = Maps.newHashMap();

	public void add(ResourceLocation id) {
		globalExclusions.add(id);
	}

	public void add(ResourceLocation type, ResourceLocation id) {
		exclusions.computeIfAbsent(type, t -> Sets.newHashSet()).add(id);
	}

	public void clear() {
		globalExclusions.clear();
		exclusions.clear();
	}

	public boolean contains(ResourceLocation type, ResourceLocation id) {
		return globalExclusions.contains(id) || (exclusions.containsKey(type) && exclusions.get(type).contains(id));
	}
}
