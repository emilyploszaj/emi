package dev.emi.emi.jemi.widget;

import java.util.List;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.input.EmiInput;
import dev.emi.emi.jemi.impl.JemiUserInput;
import dev.emi.emi.runtime.EmiLog;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * Forwards clicks and key presses to the input handlers and event listeners a JEI recipe registers
 */
public class JemiInputWidget extends Widget {
	private final Bounds bounds;
	private final List<IJeiInputHandler> inputHandlers;
	private final List<IJeiGuiEventListener> eventListeners;
	private int lastMouseX, lastMouseY;

	public JemiInputWidget(int w, int h, List<IJeiInputHandler> inputHandlers, List<IJeiGuiEventListener> eventListeners) {
		this.bounds = new Bounds(0, 0, w, h);
		this.inputHandlers = inputHandlers;
		this.eventListeners = eventListeners;
	}

	@Override
	public Bounds getBounds() {
		return bounds;
	}

	@Override
	public void render(GuiGraphicsExtractor draw, int mouseX, int mouseY, float delta) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		try {
			JemiUserInput input = JemiUserInput.mouse(mouseX, mouseY, button, EmiInput.getCurrentModifiers());
			for (IJeiInputHandler handler : inputHandlers) {
				if (contains(handler.getArea(), mouseX, mouseY) && handler.handleInput(mouseX, mouseY, input)) {
					return true;
				}
			}
			for (IJeiGuiEventListener listener : eventListeners) {
				if (contains(listener.getArea(), mouseX, mouseY) && listener.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
			}
		} catch (Throwable t) {
			EmiLog.error("Exception forwarding click to JEI recipe", t);
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		try {
			// Key presses carry no position, use the recipe relative one from the last render
			int mouseX = lastMouseX, mouseY = lastMouseY;
			JemiUserInput input = JemiUserInput.key(keyCode, scanCode, modifiers);
			for (IJeiInputHandler handler : inputHandlers) {
				if (contains(handler.getArea(), mouseX, mouseY) && handler.handleInput(mouseX, mouseY, input)) {
					return true;
				}
			}
			for (IJeiGuiEventListener listener : eventListeners) {
				if (contains(listener.getArea(), mouseX, mouseY) && listener.keyPressed(mouseX, mouseY, keyCode, scanCode, modifiers)) {
					return true;
				}
			}
		} catch (Throwable t) {
			EmiLog.error("Exception forwarding key press to JEI recipe", t);
		}
		return false;
	}

	private static boolean contains(ScreenRectangle area, int x, int y) {
		return x >= area.left() && x < area.right() && y >= area.top() && y < area.bottom();
	}
}
