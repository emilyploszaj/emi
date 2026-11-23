package dev.emi.emi.runtime;

import net.minecraft.util.profiling.Profiler;

public class EmiProfiler {

	public static void push(String name) {
		Profiler.get().push(name);
	}

	public static void pop() {
		Profiler.get().pop();
	}

	public static void swap(String name) {
		Profiler.get().popPush(name);
	}
}
