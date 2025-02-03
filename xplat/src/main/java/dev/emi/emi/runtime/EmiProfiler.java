package dev.emi.emi.runtime;

import net.minecraft.client.MinecraftClient;

public class EmiProfiler {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public static void push(String name) {
		CLIENT.getProfiler().push(name);
	}

	public static void pop() {
		CLIENT.getProfiler().pop();
	}

	public static void swap(String name) {
		CLIENT.getProfiler().swap(name);
	}
}
