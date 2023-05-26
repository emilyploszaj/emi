package dev.emi.emi.data;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.Identifier;

public class TagExclusions {
	public final Set<Identifier> globalExclusions = Sets.newHashSet();
	public final Map<Identifier, Set<Identifier>> exclusions = Maps.newHashMap();

	public void add(Identifier id) {
		globalExclusions.add(id);
	}

	public void add(Identifier type, Identifier id) {
		exclusions.computeIfAbsent(type, t -> Sets.newHashSet()).add(id);
	}

	public void clear() {
		globalExclusions.clear();
		exclusions.clear();
	}

	public boolean contains(Identifier type, Identifier id) {
		return globalExclusions.contains(id) || (exclusions.containsKey(type) && exclusions.get(type).contains(id));
	}
}
