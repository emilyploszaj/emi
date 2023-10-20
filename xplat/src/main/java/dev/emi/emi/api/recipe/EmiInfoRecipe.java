package dev.emi.emi.api.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Unmodifiable;

public class EmiInfoRecipe implements EmiRecipe {
	private static final int STACK_WIDTH = 6, MAX_STACKS = STACK_WIDTH * 3;
	private static final int PADDING = 4;
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private final List<? extends EmiIngredient> stacks;
	private final List<OrderedText> text;
	private final Identifier id;

	public EmiInfoRecipe(List<? extends EmiIngredient> stacks, List<? extends Text> text, @Nullable Identifier id) {
		this.stacks = stacks;
		this.text = text.stream().flatMap(t -> CLIENT.textRenderer.wrapLines(t, getDisplayWidth() - 4).stream()).toList();
		this.id = id;
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return VanillaEmiRecipeCategories.INFO;
	}

	@Override
	public @Nullable Identifier getId() {
		return id;
	}

	@Override
	public @Unmodifiable List<? extends EmiIngredient> getInputs() {
		return stacks;
	}

	@Override
	public @Unmodifiable List<? extends EmiStack> getOutputs() {
		return stacks.stream().flatMap(ing -> ing.getEmiStacks().stream()).toList();
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public int getDisplayWidth() {
		return 144;
	}

	@Override
	public int getDisplayHeight() {
		int stackHeight = ((Math.min(stacks.size(), MAX_STACKS) - 1) / STACK_WIDTH + 1) * 18;
		return stackHeight + CLIENT.textRenderer.fontHeight * text.size() + PADDING;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		int stackCount = Math.min(stacks.size(), MAX_STACKS);
		int stackHeight = ((stackCount - 1) / STACK_WIDTH + 1);
		int xOff = 54 - (stackCount % STACK_WIDTH * 9);
		for (int i = 0; i < stackCount; i++) {
			int y = i / STACK_WIDTH * 18;
			int x = (i % STACK_WIDTH) * 18;
			if (y / 18 + 1 == stackHeight && stackCount % STACK_WIDTH != 0) {
				x += xOff;
			}
			if (i + 1 == stackCount && stacks.size() > stackCount) {
				widgets.addSlot(EmiIngredient.of(stacks.subList(i, stacks.size())), x + 18, y);
			} else {
				widgets.addSlot(stacks.get(i), x + 18, y);
			}
		}
		int y = stackHeight * 18 + PADDING;
		int lineCount = (widgets.getHeight() - y) / CLIENT.textRenderer.fontHeight;
		PageManager manager = new PageManager(text, lineCount);
		if (lineCount < text.size()) {
			widgets.addButton(2, 2, 12, 12, 0, 0, () -> true, (mouseX, mouseY, button) -> {
				manager.scroll(-1);
			});
			widgets.addButton(widgets.getWidth() - 14, 2, 12, 12, 12, 0, () -> true, (mouseX, mouseY, button) -> {
				manager.scroll(1);
			});
		}
		widgets.addDrawable(0, y, 0, 0, (raw, mouseX, mouseY, delta) -> {
			EmiDrawContext context = EmiDrawContext.wrap(raw);
			int lo = manager.start();
			for (int i = 0; i < lineCount; i++) {
				int l = lo + i;
				if (l >= manager.lines.size()) {
					return;
				}
				OrderedText text = manager.lines.get(l);
				context.drawText(text, 0, y - y + i * CLIENT.textRenderer.fontHeight, 0);
			}
		});
	}

	private static class PageManager {
		public final List<OrderedText> lines;
		public final int pageSize;
		public int currentPage;

		public PageManager(List<OrderedText> lines, int pageSize) {
			this.lines = lines;
			this.pageSize = pageSize;
		}

		public void scroll(int delta) {
			currentPage += delta;
			int totalPages = (lines.size() - 1) / pageSize + 1;
			if (currentPage < 0) {
				currentPage = totalPages - 1;
			}
			if (currentPage >= totalPages) {
				currentPage = 0;
			}
		}

		public int start() {
			return currentPage * pageSize;
		}
	}
}
