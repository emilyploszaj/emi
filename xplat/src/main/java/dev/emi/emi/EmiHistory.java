package dev.emi.emi;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.api.EmiApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class EmiHistory {
	private static final List<Screen> HISTORIES = Lists.newArrayList();
	
	public static boolean isEmpty() {
		return HISTORIES.isEmpty();
	}

	public static void push(Screen history) {
		HISTORIES.add(history);
	}

	public static void pop() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.currentScreen instanceof HandledScreen) {
			clear();
			return;
		}
		int i = HISTORIES.size() - 1;
		HandledScreen<?> screen = EmiApi.getHandledScreen();
		if (screen != null) {
			if (i >= 0) {
				client.setScreen(HISTORIES.remove(i));
			} else {
				client.setScreen(screen);
			}
		}
	}

	public static void clear() {
		HISTORIES.clear();
	}
}
