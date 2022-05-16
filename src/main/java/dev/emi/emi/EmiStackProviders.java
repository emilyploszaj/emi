package dev.emi.emi;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class EmiStackProviders {
	public static Map<Class<?>, List<EmiStackProvider<?>>> fromClass = Maps.newHashMap();
	public static List<EmiStackProvider<?>> generic = Lists.newArrayList();

	public static void clear() {
		fromClass.clear();
		generic.clear();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static EmiIngredient getStackAt(Screen screen, int x, int y) {
		if (fromClass.containsKey(screen.getClass())) {
			for (EmiStackProvider provider : fromClass.get(screen.getClass())) {
				EmiIngredient stack = provider.getStackAt(screen, x, y);
				if (!stack.isEmpty()) {
					return stack;
				}
			}
		}
		for (EmiStackProvider handler : generic) {
			EmiIngredient stack = handler.getStackAt(screen, x, y);
			if (!stack.isEmpty()) {
				return stack;
			}
		}
		if (screen instanceof HandledScreenAccessor handled) {
			Slot s = handled.getFocusedSlot();
			if (s != null) {
				ItemStack stack = s.getStack();
				if (!stack.isEmpty()) {
					return EmiStack.of(stack);
				}
			}
		}
		return EmiStack.EMPTY;
	}
}
