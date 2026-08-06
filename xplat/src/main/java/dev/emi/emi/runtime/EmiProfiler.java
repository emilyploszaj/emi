package dev.emi.emi.runtime;

import net.minecraft.util.profiler.Profilers;

public class EmiProfiler {
    public static void push(String name) {
        Profilers.get().push(name);
	}

	public static void pop() {
        Profilers.get().pop();
	}

	public static void swap(String name) {
        Profilers.get().swap(name);
	}
}
