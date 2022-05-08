package dev.emi.emi;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.commons.compress.utils.Lists;

import dev.emi.emi.api.EmiExclusionArea;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.screen.EmiScreen;
import net.minecraft.client.gui.screen.Screen;

public class EmiExclusionAreas {
	public static Map<Class<?>, List<EmiExclusionArea<?>>> fromClass = Maps.newHashMap();
	public static List<EmiExclusionArea<?>> generic = Lists.newArrayList();

	public static void clear() {
		fromClass.clear();
		generic.clear();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static List<Bounds> getExclusion(Screen screen) {
		List<Bounds> list = Lists.newArrayList();
		if (screen instanceof EmiScreen emi) {
			int left = emi.emi$getLeft();
			int right = emi.emi$getRight();
			list.add(new Bounds(left, 0, right - left, screen.height));
		}
		if (fromClass.containsKey(screen.getClass())) {
			for (EmiExclusionArea exclusion : fromClass.get(screen.getClass())) {
				exclusion.addExclusionArea(screen, rect -> {
					list.add((Bounds) rect);
				});
			}
		}
		for (EmiExclusionArea exclusion : generic) {
			exclusion.addExclusionArea(screen, rect -> {
				list.add((Bounds) rect);
			});
		}
		return list;
	}
}
