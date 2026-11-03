package dev.emi.emi.screen;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Vanilla GUI rendering already batches item rendering through its item atlas,
 * so this only preserves the batching call structure and renders directly.
 */
public class StackBatcher {

	public static boolean isEnabled() {
		return false;
	}

	public boolean isPopulated() {
		return false;
	}

	public void repopulate() {
	}

	public void begin(int x, int y, int z) {
	}

	public void render(EmiIngredient stack, GuiGraphicsExtractor draw, int x, int y, float delta) {
		render(stack, draw, x, y, delta, -1 ^ EmiIngredient.RENDER_AMOUNT);
	}

	public void render(EmiIngredient stack, GuiGraphicsExtractor draw, int x, int y, float delta, int flags) {
		stack.render(draw, x, y, delta, flags);
	}

	public void draw() {
	}

	public static class ClaimedCollection {
		private Set<StackBatcher> claimed = Sets.newHashSet();
		private List<StackBatcher> unclaimed = Lists.newArrayList();

		public StackBatcher claim() {
			synchronized (this) {
				StackBatcher batcher;
				if (unclaimed.isEmpty()) {
					batcher = new StackBatcher();
				} else {
					batcher = unclaimed.remove(unclaimed.size() - 1);
				}
				if (batcher == null) {
					batcher = new StackBatcher();
				}
				claimed.add(batcher);
				return batcher;
			}
		}

		public void unclaim(StackBatcher batcher) {
			synchronized (this) {
				claimed.remove(batcher);
				unclaimed.add(batcher);
			}
		}

		public void unclaimAll() {
			synchronized (this) {
				for (StackBatcher batcher : claimed) {
					unclaimed.add(batcher);
				}
				claimed.clear();
			}
		}
	}
}
