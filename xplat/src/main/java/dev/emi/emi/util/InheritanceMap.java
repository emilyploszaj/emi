package dev.emi.emi.util;

import java.util.Map;

public record InheritanceMap<V>(Map<Class<?>, V> map) {

	public Class<?> getKey(Class<?> clazz) {
		Class<?> w = clazz;
		while (w != null) {
			if (map.containsKey(w)) {
				return w;
			}
			if (w == Object.class) {
				break;
			}
			w = w.getSuperclass();
		}
		for (Class<?> i : clazz.getInterfaces()) {
			if (map.containsKey(i)) {
				return i;
			}
		}
		return null;
	}

	public V get(Class<?> clazz) {
		clazz = getKey(clazz);
		if (clazz != null) {
			return map.get(clazz);
		}
		return null;
	}
}
