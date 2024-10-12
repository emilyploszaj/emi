package dev.emi.emi.screen;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.DrawableWidget;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.TextWidget.Alignment;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.dev.EmiDev;
import dev.emi.emi.runtime.dev.RecipeError;
import dev.emi.emi.screen.tooltip.EmiTooltip;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.widget.RecipeBackground;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class WidgetGroup implements WidgetHolder {
	public final EmiRecipe recipe;
	public final int x, y, width, height;
	public final List<Widget> widgets = Lists.newArrayList();

	public WidgetGroup(EmiRecipe recipe, int x, int y, int width, int height) {
		this.recipe = recipe;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		if (recipe != null) {
			widgets.add(new RecipeBackground(-4, -4, width + 8, height + 8));
		}
	}

	public void error(Throwable e) {
		widgets.clear();
		widgets.add(new RecipeBackground(-4, -4, width + 8, height + 8));
		widgets.add(new TextWidget(EmiPort.ordered(EmiPort.translatable("emi.error.recipe.render")),
			width / 2, height / 2 - 5, Formatting.RED.getColorValue(), true).horizontalAlign(Alignment.CENTER));
		widgets.add(new DrawableWidget(0, 0, width, height, (raw, mouseX, mouseY, delta) -> {})
			.tooltip((i, j) -> EmiUtil.getStackTrace(e).stream()
				.map(EmiPort::literal).map(EmiPort::ordered).map(TooltipComponent::of).toList()));
	}

	public void decorateDevMode() {
		EmiRecipeCategory category = recipe.getCategory();
		Identifier cid = category == null ? null : category.getId();
		Identifier id = recipe.getId();
		List<RecipeError> errors = Lists.newArrayList();
		if (id == null) {
			errors.add(new RecipeError(RecipeError.Severity.WARNING, EmiTooltip.splitTranslate("emi.dev.null_recipe_id")));
		} else if (EmiDev.duplicateRecipeIds.contains(id)) {
			List<TooltipComponent> tooltip = Lists.newArrayList();
			if (Objects.equals(id.getNamespace(), "minecraft") || Objects.equals(id.getNamespace(), "emi")) {
				tooltip.addAll(EmiTooltip.splitTranslate("emi.dev.duplicate_vanilla_recipe_id", id));
			} else {
				tooltip.addAll(EmiTooltip.splitTranslate("emi.dev.duplicate_recipe_id", id));
			}
			if (cid != null) {
				String suggestedPath = cid.getPath() + "/" + id.getNamespace() + "/" + id.getPath();
				if (EmiApi.getRecipeManager().getRecipe(EmiPort.id(cid.getNamespace(), suggestedPath)) == null) {
					tooltip.addAll(EmiTooltip.splitTranslate("emi.dev.suggest_id", cid.getNamespace(), suggestedPath));
				} else {
					tooltip.addAll(EmiTooltip.splitTranslate("emi.dev.synthetic_id"));
				}
			} else {
				tooltip.addAll(EmiTooltip.splitTranslate("emi.dev.synthetic_id"));
			}
			errors.add(new RecipeError(RecipeError.Severity.ERROR, tooltip));
		} else if (EmiDev.incorrectRecipeIds.contains(id)) {
			List<TooltipComponent> tooltip = Lists.newArrayList();
			tooltip.addAll(EmiTooltip.splitTranslate("emi.dev.synthetic_nag_explanation", id));
			tooltip.addAll(EmiTooltip.splitTranslate("emi.dev.synthetic_id"));
			errors.add(new RecipeError(RecipeError.Severity.ERROR, tooltip));
		}

		if (recipe.getInputs() == null || recipe.getOutputs() == null || recipe.getCatalysts() == null) {
			errors.add(new RecipeError(RecipeError.Severity.ERROR, EmiTooltip.splitTranslate("emi.dev.null_input_or_output")));
		} else {
			int nullStacks = 0;
			for (EmiIngredient stack : recipe.getInputs()) {
				if (stack == null) {
					nullStacks++;
				}
			}
			for (EmiIngredient stack : recipe.getCatalysts()) {
				if (stack == null) {
					nullStacks++;
				}
			}
			for (EmiStack stack : recipe.getOutputs()) {
				if (stack == null) {
					nullStacks++;
				}
			}
			if (nullStacks > 0) {
				errors.add(new RecipeError(RecipeError.Severity.ERROR, EmiTooltip.splitTranslate("emi.dev.null_input_or_output")));
			}

			if (!recipe.getOutputs().isEmpty() && recipe.supportsRecipeTree()) {
				int recipeContexts = 0;
				for (Widget widget : widgets) {
					if (widget instanceof SlotWidget sw) {
						if (sw.getRecipe() != null) {
							recipeContexts++;
						}
					}
				}
				if (recipeContexts == 0) {
					errors.add(new RecipeError(RecipeError.Severity.WARNING, EmiTooltip.splitTranslate("emi.dev.no_output_slots")));
				}
			}
		}

		if (!errors.isEmpty()) {
			List<TooltipComponent> tooltip = Lists.newArrayList();
			RecipeError.Severity severity = RecipeError.Severity.WARNING;
			for (RecipeError error : errors) {
				if (error.severity() == RecipeError.Severity.ERROR) {
					severity = RecipeError.Severity.ERROR;
				}
				tooltip.add(switch (error.severity()) {
					case ERROR -> EmiTooltipComponents.of(EmiPort.translatable("emi.dev.severity.error", Formatting.RED));
					case WARNING -> EmiTooltipComponents.of(EmiPort.translatable("emi.dev.severity.warning", Formatting.YELLOW));
				});
				tooltip.addAll(error.tooltip());
			}
			int errorColor = switch (severity) {
				case ERROR -> 0xCCCC0000;
				case WARNING -> 0xCCCCCC00;
			};
			addDrawable(0, 0, width, height, (raw, mouseX, mouseY, delta) -> {
				EmiDrawContext draw = EmiDrawContext.wrap(raw);
				draw.fill(-2, -3, width, 2, errorColor);
				draw.fill(-2, height + 1, width + 4, 2, errorColor);
			});
			addText(EmiPort.literal("!", Formatting.BOLD), width, -2, 0xFF000000 | errorColor, true);
			addTooltip(tooltip, width - 2, -4, 8, 16);
		}
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public <T extends Widget> T add(T widget) {
		widgets.add(widget);
		return widget;
	}
}
