package dev.emi.emi.runtime;

import net.minecraft.client.Minecraft;

public class EmiProfiler {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static void push(String name) {
		CLIENT.getProfiler().push(name);
	}

	public static void pop() {
		CLIENT.getProfiler().pop();
	}

	public static void swap(String name) {
		CLIENT.getProfiler().popPush(name);
	}
}
