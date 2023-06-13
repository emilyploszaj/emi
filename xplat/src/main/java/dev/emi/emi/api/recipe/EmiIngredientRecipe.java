package dev.emi.emi.api.recipe;

import java.util.List;

import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.runtime.EmiDrawContext;
import net.minecraft.client.gui.DrawContext;

/**
 * Represents a recipe that disambiguates an ingredient.
 */
public abstract class EmiIngredientRecipe implements EmiRecipe {

	protected abstract EmiIngredient getIngredient();

	protected abstract List<EmiStack> getStacks();

	protected abstract EmiRecipe getRecipeContext(EmiStack stack, int offset);

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(new ListEmiIngredient(getStacks(), 1));
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of();
	}

	@Override
	public int getDisplayHeight() {
		return ((getStacks().size() - 1) / 8 + 1) * 18 + 24;
	}

	@Override
	public int getDisplayWidth() {
		return 144;
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		List<EmiStack> stacks = getStacks();
		widgets.addSlot(getIngredient(), 63, 0);
		int ph = (widgets.getHeight() - 42) / 18;
		int pageSize = (ph + 1) * 8;
		PageManager manager = new PageManager(stacks, pageSize);
		if (pageSize < stacks.size()) {
			widgets.addButton(2, 2, 12, 12, 0, 0, () -> true, (mouseX, mouseY, button) -> {
				manager.scroll(-1);
			});
			widgets.addButton(widgets.getWidth() - 14, 2, 12, 12, 12, 0, () -> true, (mouseX, mouseY, button) -> {
				manager.scroll(1);
			});
		}
		for (int i = 0; i < stacks.size() && i / 8 <= ph; i++) {
			widgets.add(new PageSlotWidget(manager, i, i % 8 * 18, i / 8 * 18 + 24));
		}
	}

	private class PageManager {
		public final List<EmiStack> stacks;
		public final int pageSize;
		public int currentPage;

		public PageManager(List<EmiStack> stacks, int pageSize) {
			this.stacks = stacks;
			this.pageSize = pageSize;
		}

		public void scroll(int delta) {
			currentPage += delta;
			int totalPages = (stacks.size() - 1) / pageSize + 1;
			if (currentPage < 0) {
				currentPage = totalPages - 1;
			}
			if (currentPage >= totalPages) {
				currentPage = 0;
			}
		}

		public EmiStack getStack(int offset) {
			offset += pageSize * currentPage;
			if (offset < stacks.size()) {
				return stacks.get(offset);
			}
			return EmiStack.EMPTY;
		}

		public EmiRecipe getRecipe(int offset) {
			offset += pageSize * currentPage;
			if (offset < stacks.size()) {
				return getRecipeContext(stacks.get(offset), offset);
			}
			return null;
		}
	}

	private class PageSlotWidget extends SlotWidget {
		public final PageManager manager;
		public final int offset;

		public PageSlotWidget(PageManager manager, int offset, int x, int y) {
			super(EmiStack.EMPTY, x, y);
			this.manager = manager;
			this.offset = offset;
		}

		@Override
		public EmiIngredient getStack() {
			return manager.getStack(offset);
		}

		@Override
		public EmiRecipe getRecipe() {
			return manager.getRecipe(offset);
		}

		@Override
		public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
			if (!getStack().isEmpty()) {
				super.render(draw, mouseX, mouseY, delta);
			}
		}
		
		@Override
		public void drawBackground(DrawContext draw, int mouseX, int mouseY, float delta) {
			super.drawBackground(draw, mouseX, mouseY, delta);
			EmiDrawContext context = EmiDrawContext.wrap(draw);
			if (BoM.getRecipe(getIngredient()) instanceof EmiResolutionRecipe err && err.stack.equals(getStack())) {
				context.drawTexture(EmiRenderHelper.WIDGETS, x, y, 36, 128, 18, 18);
			}
		}
	}
}
