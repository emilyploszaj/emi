package dev.emi.emi.runtime;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import com.google.common.collect.Lists;

import dev.emi.emi.api.EmiApi;

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
		Minecraft client = Minecraft.getInstance();
		if (client.screen instanceof AbstractContainerScreen) {
			clear();
			return;
		}
		int i = HISTORIES.size() - 1;
		AbstractContainerScreen<?> screen = EmiApi.getHandledScreen();
		if (i >= 0) {
			Screen popped = HISTORIES.remove(i);
			FORWARD_HISTORIES.add(client.screen);
			client.setScreen(popped);
		} else if (screen != null) {
			client.setScreen(screen);
		}
	}

	public static void popUntil(Predicate<Screen> predicate, Screen otherwise) {
		Minecraft client = Minecraft.getInstance();
		while (!EmiHistory.isEmpty()) {
			EmiHistory.pop();
			if (predicate.test(client.screen)) {
				return;
			}
		}
		client.setScreen(otherwise);
	}

	public static void forward() {
		Minecraft client = Minecraft.getInstance();
		int i = FORWARD_HISTORIES.size() - 1;
		if (i >= 0 && client.screen != null) {
			Screen popped = FORWARD_HISTORIES.remove(i);
			HISTORIES.add(client.screen);
			client.setScreen(popped);
		}
	}

	public static void clear() {
		HISTORIES.clear();
		FORWARD_HISTORIES.clear();
	}
}
