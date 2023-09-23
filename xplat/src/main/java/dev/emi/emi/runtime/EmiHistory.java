package dev.emi.emi.runtime;

import java.util.List;

import com.google.common.collect.Lists;

import dev.emi.emi.api.EmiApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class EmiHistory {
	private static final List<Screen> HISTORIES = Lists.newArrayList();
	private static final List<Screen> FORWARD_HISTORIES = Lists.newArrayList();
	
	public static boolean isEmpty() {
		return HISTORIES.isEmpty();
	}

	public static boolean isForwardEmpty() {
		return FORWARD_HISTORIES.isEmpty();
	}

	public static void push(Screen history) {
		HISTORIES.add(history);
		FORWARD_HISTORIES.clear();
	}

	public static void pop() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.currentScreen instanceof HandledScreen) {
			clear();
			return;
		}
		int i = HISTORIES.size() - 1;
		HandledScreen<?> screen = EmiApi.getHandledScreen();
		if (i >= 0) {
			Screen popped = HISTORIES.remove(i);
			FORWARD_HISTORIES.add(client.currentScreen);
			client.setScreen(popped);
		} else if (screen != null) {
			client.setScreen(screen);
		}
	}

	public static void forward() {
		MinecraftClient client = MinecraftClient.getInstance();
		int i = FORWARD_HISTORIES.size() - 1;
		if (i >= 0 && client.currentScreen != null) {
			Screen popped = FORWARD_HISTORIES.remove(i);
			HISTORIES.add(client.currentScreen);
			client.setScreen(popped);
		}
	}

	public static void clear() {
		HISTORIES.clear();
		FORWARD_HISTORIES.clear();
	}
}
