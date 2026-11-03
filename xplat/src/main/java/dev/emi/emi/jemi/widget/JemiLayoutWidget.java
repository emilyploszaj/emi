package dev.emi.emi.jemi.widget;

import java.util.function.Function;

import com.mojang.blaze3d.platform.InputConstants;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.jemi.impl.JemiUserInput;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.widget.OverlayingWidget;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * Draws a JEI recipe through JEI's own layout, for categories whose slots live inside their widgets
 */
public class JemiLayoutWidget extends Widget implements OverlayingWidget {
	private final IRecipeLayoutDrawable<?> layout;
	private final Bounds bounds;
	private int lastMouseX, lastMouseY;

	public JemiLayoutWidget(int width, int height, IRecipeLayoutDrawable<?> layout) {
		this.layout = layout;
		this.bounds = new Bounds(0, 0, width, height);
		layout.setPosition(0, 0);
	}

	@Override
	public Bounds getBounds() {
		return bounds;
	}

	@Override
	public void render(GuiGraphicsExtractor draw, int mouseX, int mouseY, float delta) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		layout.drawRecipe(draw, mouseX, mouseY);
	}

	@Override
	public void renderOverlay(GuiGraphicsExtractor draw, int mouseX, int mouseY) {
		layout.drawOverlays(draw, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		IJeiInputHandler handler = layout.getInputHandler();
		if (contains(handler.getArea(), mouseX, mouseY)
				&& handler.handleInput(mouseX, mouseY, JemiUserInput.mouse(mouseX, mouseY, button, EmiInput.getCurrentModifiers()))) {
			return true;
		}
		return stackInteraction(mouseX, mouseY, bind -> bind.matchesMouse(button));
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		IJeiInputHandler handler = layout.getInputHandler();
		if (contains(handler.getArea(), lastMouseX, lastMouseY)
				&& handler.handleInput(lastMouseX, lastMouseY, JemiUserInput.key(keyCode, scanCode, modifiers))) {
			return true;
		}
		return stackInteraction(lastMouseX, lastMouseY, bind -> bind.matchesKey(keyCode, scanCode));
	}

	@Override
	public boolean mouseScrolled(int mouseX, int mouseY, double amount) {
		IJeiInputHandler handler = layout.getInputHandler();
		return contains(handler.getArea(), mouseX, mouseY) && handler.handleMouseScrolled(mouseX, mouseY, 0, amount);
	}

	@Override
	public boolean mouseDragged(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
		IJeiInputHandler handler = layout.getInputHandler();
		return contains(handler.getArea(), mouseX, mouseY)
			&& handler.handleMouseDragged(mouseX, mouseY, InputConstants.Type.MOUSE.getOrCreate(button), deltaX, deltaY);
	}

	// Recipe and usage lookups on the slots JEI draws
	private boolean stackInteraction(int mouseX, int mouseY, Function<EmiBind, Boolean> function) {
		try {
			EmiStack stack = layout.getSlotUnderMouse(mouseX, mouseY)
				.flatMap(slot -> slot.slot().getDisplayedIngredient())
				.map(JemiUtil::getStack)
				.orElse(EmiStack.EMPTY);
			if (!stack.isEmpty()) {
				return EmiScreenManager.stackInteraction(new EmiStackInteraction(stack), function);
			}
		} catch (Throwable t) {
			EmiLog.error("Exception looking up JEI recipe slot", t);
		}
		return false;
	}

	private static boolean contains(ScreenRectangle area, int x, int y) {
		return x >= area.left() && x < area.right() && y >= area.top() && y < area.bottom();
	}
}
