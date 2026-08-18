package dev.emi.emi.registry;

import java.util.Map;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.api.EmiStackPuller;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.screen.ScreenHandler;

public class EmiStackPullers {

	public static Map<Class<? extends ScreenHandler>, EmiStackPuller<?>> fromClass = Maps.newHashMap();
	public static List<EmiStackPuller<?>> generic = Lists.newArrayList();

	public static void clear() {
		fromClass.clear();
		generic.clear();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean attemptPull(ScreenHandler screenHandler, List<EmiStack> stacks, long toPull) {
		if (fromClass.containsKey(screenHandler.getClass())) {
			EmiStackPuller puller = fromClass.get(screenHandler.getClass());
			if (puller.pullStack(screenHandler, stacks, toPull)) return true;
		}

		for (EmiStackPuller puller : generic) {
			if (puller.pullStack(screenHandler, stacks, toPull)) return true;
		}

		return false;
	}

}
