package dev.emi.emi.jemi.runtime;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.EmiScreenManager.SidebarPanel;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientListOverlay;

public class JemiIngredientListOverlay implements IIngredientListOverlay {

	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		EmiStackInteraction stack = EmiScreenManager.getHoveredStack(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, false);
		if (stack instanceof EmiScreenManager.SidebarEmiStackInteraction sesi && sesi.getType() == SidebarType.INDEX && sesi.getStack().getEmiStacks().size() == 1) {
			return JemiUtil.getTyped(stack.getStack().getEmiStacks().get(0));
		}
		return Optional.empty();
	}

	@Override
	public <T> @Nullable T getIngredientUnderMouse(IIngredientType<T> ingredientType) {
		Optional<ITypedIngredient<?>> opt = getIngredientUnderMouse();
		if (opt.isPresent()) {
			return opt.get().getIngredient(ingredientType).orElseGet(() -> null);
		}
		return null;
	}

	@Override
	public boolean isListDisplayed() {
		return !EmiScreenManager.isDisabled() && EmiScreenManager.hasSidebarAvailable(SidebarType.INDEX);
	}

	@Override
	public boolean hasKeyboardFocus() {
		return EmiScreenManager.search.isFocused();
	}

	@Override
	public <T> List<T> getVisibleIngredients(IIngredientType<T> ingredientType) {
		SidebarPanel panel = EmiScreenManager.getPanelFor(SidebarType.INDEX);
		if (panel != null) {
			return panel.space.getPage(panel.page).stream()
				.map(i -> JemiUtil.getTyped(i.getEmiStacks().get(0)))
				.filter(Optional::isPresent).map(Optional::get)
				.map(i -> i.getIngredient(ingredientType))
				.filter(Optional::isPresent).map(Optional::get)
				.toList();
		}
		return List.of();
	}
	
}
