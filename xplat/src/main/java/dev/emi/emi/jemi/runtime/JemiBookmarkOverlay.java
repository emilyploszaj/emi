package dev.emi.emi.jemi.runtime;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.config.SidebarType;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.screen.EmiScreenManager;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IBookmarkOverlay;

public class JemiBookmarkOverlay implements IBookmarkOverlay {

	@Override
	public Optional<ITypedIngredient<?>> getIngredientUnderMouse() {
		EmiStackInteraction stack = EmiScreenManager.getHoveredStack(EmiScreenManager.lastMouseX, EmiScreenManager.lastMouseY, false);
		if (stack instanceof EmiScreenManager.SidebarEmiStackInteraction sesi && sesi.getType() == SidebarType.FAVORITES && sesi.getStack().getEmiStacks().size() == 1) {
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
}
