package dev.emi.emi.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.Maps;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.mixin.accessor.HandledScreenAccessor;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public interface EmiDragDropHandler<T extends Screen> {
	
	/**
	 * Called when a stack is released while being dragged.
	 * @return Whether to consume the event.
	 */
	boolean dropStack(T screen, EmiIngredient stack, int x, int y);

	/**
	 * Called when a stack is being dragged.
	 */
	default void render(T screen, EmiIngredient dragged, DrawContext draw, int mouseX, int mouseY, float delta) {
	}

	/**
	 * A simple, bounds based drag drop handler.
	 * Bounds are rendered while a stack is dragged.
	 */
	public static class BoundsBased<T extends Screen> implements EmiDragDropHandler<T> {
		private final Function<T, Map<Bounds, Consumer<EmiIngredient>>> bounds;

		public BoundsBased(Function<T, Map<Bounds, Consumer<EmiIngredient>>> bounds) {
			this.bounds = bounds;
		}

		public BoundsBased(BiConsumer<T, BiConsumer<Bounds, Consumer<EmiIngredient>>> bounds) {
			this.bounds = screen -> {
				Map<Bounds, Consumer<EmiIngredient>> map = Maps.newHashMap();
				bounds.accept(screen, (b, consumer) -> map.put(b, consumer));
				return map;
			};
		}

		@Override
		public boolean dropStack(T screen, EmiIngredient stack, int x, int y) {
			Map<Bounds, Consumer<EmiIngredient>> bounds = this.bounds.apply(screen);
			for (Bounds b : bounds.keySet()) {
				if (b.contains(x, y)) {
					bounds.get(b).accept(stack);
					return true;
				}
			}
			return false;
		}

		@Override
		public void render(T screen, EmiIngredient dragged, DrawContext draw, int mouseX, int mouseY, float delta) {
			EmiDrawContext context = EmiDrawContext.wrap(draw);
			for (Bounds b : bounds.apply(screen).keySet()) {
				context.fill(b.x(), b.y(), b.width(), b.height(), 0x8822BB33);
			}
		}
	}

	/**
	 * A simple, slot based drag drop handler.
	 * Slots render a highlight while a stack is dragged.
	 */
	public static class SlotBased<T extends HandledScreen<?>> extends BoundsBased<T> {
		
		/**
		 * @param slots A function to get a list of slot targets given a screen
		 * @param consumer A consumer for dropped stacks
		 */
		public SlotBased(Function<T, Collection<Slot>> slots, TriConsumer<T, Slot, EmiIngredient> consumer) {
			super(t -> SlotBased.<T>map(t, slots, consumer));
		}

		/**
		 * @param slotFilter A filter for which slots are valid targets
		 * @param consumer A consumer for dropped stacks
		 */
		public SlotBased(BiPredicate<T, Slot> slotFilter, TriConsumer<T, Slot, EmiIngredient> consumer) {
			super(t -> SlotBased.<T>map(t, screen -> filter(screen, slotFilter), consumer));
		}

		private static <T extends HandledScreen<?>> Collection<Slot> filter(T t, BiPredicate<T, Slot> slotFilter) {
			List<Slot> slots = Lists.newArrayList();
			ScreenHandler handler = t.getScreenHandler();
			for (Slot slot : handler.slots) {
				if (slotFilter.test(t, slot)) {
					slots.add(slot);
				}
			}
			return slots;
		}

		private static <T extends HandledScreen<?>> Map<Bounds, Consumer<EmiIngredient>>
				map(T t, Function<T, Collection<Slot>> slots, TriConsumer<T, Slot, EmiIngredient> consumer) {
			Map<Bounds, Consumer<EmiIngredient>> map = Maps.newHashMap();
			for (Slot slot : slots.apply(t)) {
				map.put(new Bounds(((HandledScreenAccessor) t).getX() + slot.x - 1,
					((HandledScreenAccessor) t).getY() + slot.y - 1, 18, 18), i -> consumer.accept(t, slot, i));
			}
			return map;
		}

		public static interface TriConsumer<A, B, C> {
			void accept(A a, B b, C c);
		}
	}
}
