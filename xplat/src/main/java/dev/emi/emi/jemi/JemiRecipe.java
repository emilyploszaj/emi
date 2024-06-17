package dev.emi.emi.jemi;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.jemi.impl.JemiIngredientAcceptor;
import dev.emi.emi.jemi.impl.JemiRecipeLayoutBuilder;
import dev.emi.emi.jemi.impl.JemiRecipeSlot;
import dev.emi.emi.jemi.impl.JemiRecipeSlotBuilder;
import dev.emi.emi.jemi.widget.JemiSlotWidget;
import dev.emi.emi.jemi.widget.JemiTankWidget;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.screen.EmiScreenManager;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.library.focus.FocusGroup;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.InputUtil;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

public class JemiRecipe<T> implements EmiRecipe {
	public List<EmiIngredient> inputs = Lists.newArrayList();
	public List<EmiIngredient> catalysts = Lists.newArrayList();
	public List<EmiStack> outputs = Lists.newArrayList();
	public EmiRecipeCategory recipeCategory;
	public Identifier originalId, id;
	public IRecipeCategory<T> category;
	public T recipe;
	public JemiRecipeLayoutBuilder builder = new JemiRecipeLayoutBuilder();
	public boolean allowTree = true;

	public JemiRecipe(EmiRecipeCategory recipeCategory, IRecipeCategory<T> category, T recipe) {
		this.recipeCategory = recipeCategory;
		this.category = category;
		this.recipe = recipe;
		this.originalId = category.getRegistryName(recipe);
		if (this.originalId != null) {
			this.id = EmiPort.id("jei", "/" + EmiUtil.subId(this.originalId));
		}
		category.setRecipe(builder, recipe, JemiPlugin.runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup());
		for (JemiRecipeSlotBuilder jrsb : builder.slots) {
			jrsb.acceptor.coerceStacks(jrsb.tooltipCallback, jrsb.renderers);
		}
		for (JemiIngredientAcceptor acceptor : builder.ingredients) {
			EmiIngredient stack = acceptor.build();
			if (acceptor.role == RecipeIngredientRole.INPUT) {
				inputs.add(stack);
			} else if (acceptor.role == RecipeIngredientRole.CATALYST) {
				catalysts.add(stack);
			} else if (acceptor.role == RecipeIngredientRole.OUTPUT) {
				if (stack.getEmiStacks().size() > 1) {
					allowTree = false;
				}
				outputs.addAll(stack.getEmiStacks());
			}
		}
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return recipeCategory;
	}

	@Override
	public @Nullable Recipe<?> getBackingRecipe() {
		return EmiPort.getRecipe(originalId);
	}

	@Override
	public @Nullable Identifier getId() {
		return id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return inputs;
	}

	@Override
	public List<EmiIngredient> getCatalysts() {
		return catalysts;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return outputs;
	}

	@Override
	public int getDisplayWidth() {
		return category.getWidth();
	}

	@Override
	public int getDisplayHeight() {
		return category.getHeight();
	}

	@Override
	public boolean supportsRecipeTree() {
		return allowTree && EmiRecipe.super.supportsRecipeTree();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addWidgets(WidgetHolder widgets) {
		Optional<IRecipeLayoutDrawable<T>> opt = JemiPlugin.runtime.getRecipeManager().createRecipeLayoutDrawable(category, recipe, FocusGroup.EMPTY);
		JemiRecipeLayoutBuilder builder = new JemiRecipeLayoutBuilder();
		category.setRecipe(builder, recipe, JemiPlugin.runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup());
		for (JemiRecipeSlotBuilder jrsb : builder.slots) {
			jrsb.acceptor.coerceStacks(jrsb.tooltipCallback, jrsb.renderers);
		}
		if (opt.isPresent()) {
			widgets.add(new JemiWidget(0, 0, getDisplayWidth(), getDisplayHeight(), opt.get()));
			for (JemiRecipeSlotBuilder sb : builder.slots) {
				JemiRecipeSlot slot = new JemiRecipeSlot(sb);
				if (slot.tankInfo != null && !slot.getIngredients(JemiUtil.getFluidType()).toList().isEmpty()) {
					widgets.add(new JemiTankWidget(slot, this));
				} else {
					widgets.add(new JemiSlotWidget(slot, this));
				}
			}
		}
	}

	public class JemiWidget extends Widget {

		private final IRecipeLayoutDrawable<T> recipeLayoutDrawable;
		private final Bounds bounds;
		private final int x, y;

		public JemiWidget(int x, int y, int w, int h, IRecipeLayoutDrawable<T> recipeLayoutDrawable) {
			this.recipeLayoutDrawable = recipeLayoutDrawable;
			this.bounds = new Bounds(x, y, w, h);
			this.x = x;
			this.y = y;
		}

		@Override
		public Bounds getBounds() {
			return bounds;
		}

		@Override
		public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
			EmiDrawContext context = EmiDrawContext.wrap(draw);
			context.push();
			context.matrices().translate(x, y, 0);
			category.getBackground().draw(context.raw());
			category.draw(recipe, recipeLayoutDrawable.getRecipeSlotsView(), context.raw(), mouseX, mouseY);
			context.resetColor();
			context.pop();
		}

		@Override
		public List<TooltipComponent> getTooltip(int mouseX, int mouseY) {
			return category.getTooltipStrings(recipe, recipeLayoutDrawable.getRecipeSlotsView(), mouseX, mouseY)
				.stream()
				.map(t -> TooltipComponent.of(t.asOrderedText()))
				.toList();
		}

		@Override
		public boolean mouseClicked(int mouseX, int mouseY, int button) {
			return category.handleInput(recipe, mouseX, mouseY, InputUtil.Type.MOUSE.createFromCode(button));
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			return category.handleInput(recipe, EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, InputUtil.fromKeyCode(keyCode, scanCode));
		}
	}
}
