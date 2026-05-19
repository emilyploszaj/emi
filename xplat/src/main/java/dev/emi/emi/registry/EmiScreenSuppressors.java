package dev.emi.emi.registry;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.api.EmiScreenSuppressor;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.gui.screen.Screen;

public class EmiScreenSuppressors {
	public static Map<Class<?>, List<EmiScreenSuppressor<?>>> fromClass = Maps.newHashMap();
	public static List<EmiScreenSuppressor<?>> generic = Lists.newArrayList();

	public static void clear() {
		fromClass.clear();
		generic.clear();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static boolean isSuppressed(Screen screen) {
		try {
			List<EmiScreenSuppressor<?>> suppressors = fromClass.get(screen.getClass());
			if (suppressors != null) {
				for (EmiScreenSuppressor suppressor : suppressors) {
					if (suppressor.isSuppressed(screen)) {
						return true;
					}
				}
			}
			for (EmiScreenSuppressor suppressor : generic) {
				if (suppressor.isSuppressed(screen)) {
					return true;
				}
			}
		} catch (Exception e) {
			EmiLog.error("Exception thrown when checking screen suppression", e);
		}
		return false;
	}
}
