package dev.emi.emi.jemi.runtime;

import java.util.List;
import java.util.Optional;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.jemi.JemiPlugin;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.runtime.EmiDrawContext;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Rect2i;

public class JemiDragDropHandler implements EmiDragDropHandler<Screen> {

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public boolean dropStack(Screen screen, EmiIngredient stack, int x, int y) {
		try {
			return this.<Object>drop(screen, (Optional<ITypedIngredient<Object>>) (Optional) JemiUtil.getTyped(stack.getEmiStacks().get(0)), x, y);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void render(Screen screen, EmiIngredient dragged, MatrixStack raw, int mouseX, int mouseY, float delta) {
		try {
			this.<Object>render(screen, EmiDrawContext.wrap(raw), (Optional<ITypedIngredient<Object>>) (Optional) JemiUtil.getTyped(dragged.getEmiStacks().get(0)));
		} catch (Exception e) {
		}
	}

	private <I> boolean drop(Screen screen, Optional<ITypedIngredient<I>> optional, int x, int y) {
		if (optional.isPresent()) {
			for (IGhostIngredientHandler.Target<I> target : getTargets(screen, optional.get())) {
				if (target.getArea().contains(x, y)) {
					target.accept(optional.get().getIngredient());
					return true;
				}
			}
		}
		return false;
	}

	private <I> void render(Screen screen, EmiDrawContext context, Optional<ITypedIngredient<I>> optional) {
		if (optional.isPresent()) {
			for (IGhostIngredientHandler.Target<I> target : getTargets(screen, optional.get())) {
				Rect2i r = target.getArea();
				context.fill(r.getX(), r.getY(), r.getWidth(), r.getHeight(), 0x8822BB33);
			}
		}
	}

	private <I> List<IGhostIngredientHandler.Target<I>> getTargets(Screen screen, ITypedIngredient<I> typed) {
		Optional<IGhostIngredientHandler<Screen>> optGhost = JemiPlugin.runtime.getScreenHelper().getGhostIngredientHandler(screen);
		if (optGhost.isPresent()) {
			IGhostIngredientHandler<Screen> ghost = optGhost.get();
			return ghost.getTargetsTyped(screen, typed, false);
		}
		return List.of();
	}
}
